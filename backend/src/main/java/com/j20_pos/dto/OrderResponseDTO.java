package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {

    private Long id;
    private String invoiceNumber;
    private LocalDate orderDate;
    private String orderTime;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal grandTotal;
    private String paymentMethod;
    private BigDecimal amountPaid;
    private BigDecimal changeDue;
    private String customerName;
    private String customerPhone;
    private String cashierName;
    private String terminalId;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDTO> items;
}
