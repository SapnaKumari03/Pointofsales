package com.j20_pos.service;

import com.j20_pos.dto.*;
import com.j20_pos.entity.InventoryMovement;
import com.j20_pos.entity.Order;
import com.j20_pos.entity.OrderItem;
import com.j20_pos.entity.Product;
import com.j20_pos.exception.BadRequestException;
import com.j20_pos.exception.InsufficientStockException;
import com.j20_pos.exception.ResourceNotFoundException;
import com.j20_pos.repository.InventoryMovementRepository;
import com.j20_pos.repository.OrderRepository;
import com.j20_pos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final CashService cashService;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> searchOrders(String query, String paymentMethod, LocalDate startDate, LocalDate endDate) {
        return orderRepository.searchOrders(query, paymentMethod, startDate, endDate).stream()
                .map(this::mapToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        return mapToOrderResponseDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderByInvoiceNumber(String invoiceNumber) {
        Order order = orderRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with invoice number: " + invoiceNumber));
        return mapToOrderResponseDTO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDTO processCheckout(CheckoutRequestDTO request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        LocalDate today = LocalDate.now();
        String orderTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        String invoiceNumber = generateUniqueInvoiceNumber(today);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemDTO itemDTO : request.getItems()) {
            if (itemDTO.getProductId() == null) {
                throw new BadRequestException("Product ID cannot be null");
            }
            if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than zero for product ID: " + itemDTO.getProductId());
            }

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemDTO.getProductId()));

            if ("INACTIVE".equalsIgnoreCase(product.getStatus()) || "DISCONTINUED".equalsIgnoreCase(product.getStatus())) {
                throw new BadRequestException("Product '" + product.getName() + "' is " + product.getStatus() + " and cannot be sold");
            }

            int requestedQty = itemDTO.getQuantity();
            int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;

            if (currentStock < requestedQty) {
                throw new InsufficientStockException("Insufficient stock for product '" + product.getName()
                        + "' (SKU: " + product.getSku() + "). Available: " + currentStock + ", requested: " + requestedQty);
            }

            BigDecimal unitPrice = product.getSellingPrice();
            BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(requestedQty)).setScale(2, RoundingMode.HALF_UP);
            Double taxRate = product.getTaxRate() != null ? product.getTaxRate() : 0.0;
            BigDecimal itemTax = itemSubtotal.multiply(BigDecimal.valueOf(taxRate))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal itemTotal = itemSubtotal.add(itemTax).setScale(2, RoundingMode.HALF_UP);

            subtotal = subtotal.add(itemSubtotal);
            totalTax = totalTax.add(itemTax);

            // Deduct stock from product
            int newStock = currentStock - requestedQty;
            product.setStockQuantity(newStock);
            productRepository.save(product);

            // Record inventory movement for stock deduction
            String cashier = request.getCashierName() != null && !request.getCashierName().isBlank()
                    ? request.getCashierName().trim() : "POS Cashier";

            InventoryMovement movement = InventoryMovement.builder()
                    .product(product)
                    .movementType("STOCK_OUT")
                    .quantity(requestedQty)
                    .previousStock(currentStock)
                    .newStock(newStock)
                    .reason("POS Sale (" + invoiceNumber + ")")
                    .referenceNumber(invoiceNumber)
                    .performedBy(cashier)
                    .timestamp(LocalDateTime.now())
                    .build();
            inventoryMovementRepository.save(movement);

            // Create OrderItem entity
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .sku(product.getSku())
                    .quantity(requestedQty)
                    .unitPrice(unitPrice)
                    .taxRate(taxRate)
                    .taxAmount(itemTax)
                    .subtotal(itemSubtotal)
                    .total(itemTotal)
                    .build();
            orderItems.add(orderItem);
        }

        // Calculate discount and grand total
        BigDecimal discount = request.getDiscountAmount() != null && request.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0
                ? request.getDiscountAmount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal grandTotal = subtotal.add(totalTax).subtract(discount).setScale(2, RoundingMode.HALF_UP);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            grandTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod().trim().toUpperCase() : "CASH";
        BigDecimal amountPaid = request.getAmountPaid() != null ? request.getAmountPaid().setScale(2, RoundingMode.HALF_UP) : null;
        BigDecimal changeDue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if ("CASH".equals(paymentMethod)) {
            if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
                amountPaid = grandTotal;
                changeDue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else {
                if (amountPaid.compareTo(grandTotal) < 0) {
                    throw new BadRequestException("Amount paid (₹" + amountPaid + ") is less than grand total (₹" + grandTotal + ")");
                }
                changeDue = amountPaid.subtract(grandTotal).setScale(2, RoundingMode.HALF_UP);
            }
        } else {
            // UPI, CARD
            amountPaid = grandTotal;
            changeDue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        Order order = Order.builder()
                .invoiceNumber(invoiceNumber)
                .orderDate(today)
                .orderTime(orderTime)
                .subtotal(subtotal)
                .taxAmount(totalTax)
                .discountAmount(discount)
                .grandTotal(grandTotal)
                .paymentMethod(paymentMethod)
                .amountPaid(amountPaid)
                .changeDue(changeDue)
                .customerName(request.getCustomerName() != null && !request.getCustomerName().isBlank() ? request.getCustomerName().trim() : "Walk-in Customer")
                .customerPhone(request.getCustomerPhone() != null ? request.getCustomerPhone().trim() : "")
                .cashierName(request.getCashierName() != null && !request.getCashierName().isBlank() ? request.getCashierName().trim() : "Default Cashier")
                .terminalId(request.getTerminalId() != null && !request.getTerminalId().isBlank() ? request.getTerminalId().trim() : "POS-01")
                .status("COMPLETED")
                .items(new ArrayList<>())
                .build();

        for (OrderItem item : orderItems) {
            order.addItem(item);
        }

        Order savedOrder = orderRepository.save(order);
        cashService.recordSalePayment(savedOrder);
        return mapToOrderResponseDTO(savedOrder);
    }

    private String generateUniqueInvoiceNumber(LocalDate date) {
        long sequence = 1000 + orderRepository.count() + 1;
        String candidate = String.format("INV-%d-%04d", date.getYear(), sequence);
        while (orderRepository.findByInvoiceNumber(candidate).isPresent()) {
            sequence++;
            candidate = String.format("INV-%d-%04d", date.getYear(), sequence);
        }
        return candidate;
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getItems() != null ? order.getItems().stream()
                .map(this::mapToOrderItemResponseDTO)
                .collect(Collectors.toList()) : Collections.emptyList();

        return OrderResponseDTO.builder()
                .id(order.getId())
                .invoiceNumber(order.getInvoiceNumber())
                .orderDate(order.getOrderDate())
                .orderTime(order.getOrderTime())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .grandTotal(order.getGrandTotal())
                .paymentMethod(order.getPaymentMethod())
                .amountPaid(order.getAmountPaid())
                .changeDue(order.getChangeDue())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .cashierName(order.getCashierName())
                .terminalId(order.getTerminalId())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(itemDTOs)
                .build();
    }

    private OrderItemResponseDTO mapToOrderItemResponseDTO(OrderItem item) {
        ProductDTO productDTO = null;
        if (item.getProduct() != null) {
            Product p = item.getProduct();
            productDTO = ProductDTO.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .sku(p.getSku())
                    .barcode(p.getBarcode())
                    .category(p.getCategory())
                    .costPrice(p.getCostPrice())
                    .sellingPrice(p.getSellingPrice())
                    .taxRate(p.getTaxRate())
                    .stockQuantity(p.getStockQuantity())
                    .minStockLevel(p.getMinStockLevel())
                    .unit(p.getUnit())
                    .status(p.getStatus())
                    .description(p.getDescription())
                    .createdAt(p.getCreatedAt())
                    .updatedAt(p.getUpdatedAt())
                    .build();
        }

        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .product(productDTO)
                .productName(item.getProductName())
                .sku(item.getSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .taxRate(item.getTaxRate())
                .taxAmount(item.getTaxAmount())
                .subtotal(item.getSubtotal())
                .total(item.getTotal())
                .build();
    }
}
