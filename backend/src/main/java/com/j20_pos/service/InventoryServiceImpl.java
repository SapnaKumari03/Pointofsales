package com.j20_pos.service;

import com.j20_pos.dto.InventoryMovementDTO;
import com.j20_pos.dto.InventoryOverviewDTO;
import com.j20_pos.dto.ProductDTO;
import com.j20_pos.dto.StockAdjustmentDTO;
import com.j20_pos.entity.InventoryMovement;
import com.j20_pos.entity.Product;
import com.j20_pos.exception.BadRequestException;
import com.j20_pos.exception.InsufficientStockException;
import com.j20_pos.exception.ResourceNotFoundException;
import com.j20_pos.repository.InventoryMovementRepository;
import com.j20_pos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;

    @Override
    @Transactional
    public ProductDTO adjustStock(StockAdjustmentDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + dto.getProductId()));

        int previousStock = product.getStockQuantity();
        int newStock;
        String opType = dto.getAdjustmentType().trim().toUpperCase();

        switch (opType) {
            case "STOCK_IN":
                newStock = previousStock + dto.getQuantity();
                break;

            case "STOCK_OUT":
                if (previousStock < dto.getQuantity()) {
                    throw new InsufficientStockException("Insufficient stock for '" + product.getName() +
                            "'. Available: " + previousStock + ", Requested deduction: " + dto.getQuantity());
                }
                newStock = previousStock - dto.getQuantity();
                break;

            case "ADJUSTMENT":
                if (dto.getTargetStock() != null) {
                    if (dto.getTargetStock() < 0) {
                        throw new BadRequestException("Target stock quantity cannot be negative");
                    }
                    newStock = dto.getTargetStock();
                } else {
                    newStock = previousStock + dto.getQuantity();
                }
                break;

            default:
                throw new BadRequestException("Invalid adjustment type: '" + dto.getAdjustmentType() +
                        "'. Supported values: STOCK_IN, STOCK_OUT, ADJUSTMENT");
        }

        // Apply updated stock
        product.setStockQuantity(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        Product updatedProduct = productRepository.save(product);

        // Record inventory audit movement
        InventoryMovement movement = InventoryMovement.builder()
                .product(updatedProduct)
                .movementType(opType)
                .quantity(dto.getQuantity())
                .previousStock(previousStock)
                .newStock(newStock)
                .reason(dto.getReason())
                .referenceNumber(dto.getReferenceNumber() != null ? dto.getReferenceNumber() : "MANUAL-ADJ")
                .performedBy(dto.getPerformedBy() != null ? dto.getPerformedBy() : "Store Staff")
                .timestamp(LocalDateTime.now())
                .build();
        movementRepository.save(movement);

        return mapToProductDTO(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts().stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryOverviewDTO getInventoryOverview() {
        List<Product> allProducts = productRepository.findAll();

        long totalProducts = allProducts.size();
        long lowStockCount = 0;
        long outOfStockCount = 0;
        long totalStockUnits = 0;
        BigDecimal totalCostValue = BigDecimal.ZERO;
        BigDecimal totalRetailValue = BigDecimal.ZERO;

        for (Product p : allProducts) {
            int qty = p.getStockQuantity();
            totalStockUnits += qty;

            if (qty == 0) {
                outOfStockCount++;
                lowStockCount++;
            } else if (qty <= p.getMinStockLevel()) {
                lowStockCount++;
            }

            BigDecimal cost = p.getCostPrice().multiply(BigDecimal.valueOf(qty));
            BigDecimal retail = p.getSellingPrice().multiply(BigDecimal.valueOf(qty));

            totalCostValue = totalCostValue.add(cost);
            totalRetailValue = totalRetailValue.add(retail);
        }

        BigDecimal projectedProfit = totalRetailValue.subtract(totalCostValue);

        return InventoryOverviewDTO.builder()
                .totalProducts(totalProducts)
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .totalStockUnits(totalStockUnits)
                .totalCostValue(totalCostValue)
                .totalRetailValue(totalRetailValue)
                .projectedProfit(projectedProfit)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementDTO> getMovementHistory(Long productId) {
        List<InventoryMovement> movements;
        if (productId != null) {
            movements = movementRepository.findByProductIdOrderByTimestampDesc(productId);
        } else {
            movements = movementRepository.findAllByOrderByTimestampDesc();
        }

        return movements.stream().map(m -> InventoryMovementDTO.builder()
                .id(m.getId())
                .productId(m.getProduct() != null ? m.getProduct().getId() : null)
                .productName(m.getProduct() != null ? m.getProduct().getName() : "N/A")
                .sku(m.getProduct() != null ? m.getProduct().getSku() : "N/A")
                .category(m.getProduct() != null ? m.getProduct().getCategory() : "N/A")
                .movementType(m.getMovementType())
                .quantity(m.getQuantity())
                .previousStock(m.getPreviousStock())
                .newStock(m.getNewStock())
                .reason(m.getReason())
                .referenceNumber(m.getReferenceNumber())
                .performedBy(m.getPerformedBy())
                .timestamp(m.getTimestamp())
                .build()
        ).collect(Collectors.toList());
    }

    private ProductDTO mapToProductDTO(Product p) {
        return ProductDTO.builder()
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
}
