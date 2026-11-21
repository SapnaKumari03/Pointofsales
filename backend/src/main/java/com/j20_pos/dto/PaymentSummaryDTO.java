package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryDTO {

    private BigDecimal totalSales;
    private Integer totalOrders;
    private BigDecimal cashTotal;
    private Integer cashTxCount;
    private BigDecimal upiTotal;
    private Integer upiTxCount;
    private BigDecimal cardTotal;
    private Integer cardTxCount;
}
