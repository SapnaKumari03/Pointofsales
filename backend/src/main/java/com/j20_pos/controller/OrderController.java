package com.j20_pos.controller;

import com.j20_pos.dto.ApiResponse;
import com.j20_pos.dto.CheckoutRequestDTO;
import com.j20_pos.dto.OrderResponseDTO;
import com.j20_pos.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<OrderResponseDTO> orders;
        if ((search != null && !search.trim().isEmpty()) ||
            (paymentMethod != null && !paymentMethod.trim().isEmpty()) ||
            startDate != null || endDate != null) {
            orders = orderService.searchOrders(search, paymentMethod, startDate, endDate);
        } else {
            orders = orderService.getAllOrders();
        }
        return ResponseEntity.ok(ApiResponse.ok("Orders retrieved successfully", orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderById(@PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok("Order retrieved successfully", order));
    }

    @GetMapping("/invoice/{invoiceNumber}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderByInvoiceNumber(@PathVariable String invoiceNumber) {
        OrderResponseDTO order = orderService.getOrderByInvoiceNumber(invoiceNumber);
        return ResponseEntity.ok(ApiResponse.ok("Order retrieved successfully", order));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> checkout(@Valid @RequestBody CheckoutRequestDTO request) {
        OrderResponseDTO order = orderService.processCheckout(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Checkout completed successfully", order));
    }
}
