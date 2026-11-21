package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodSummaryDTO {

    private String method; // CASH, UPI, CARD
    private String label;
    private BigDecimal totalAmount;
    private Integer transactionCount;
    private Double sharePercent;
    private BigDecimal averageTicketSize;
}
