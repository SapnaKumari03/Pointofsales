package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesSummaryDTO {

    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private Integer totalUnitsSold;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
    private BigDecimal netSales;
    private BigDecimal averageOrderValue;
    private BigDecimal highestDayRevenue;
    private String highestDayLabel;
    private LocalDate startDate;
    private LocalDate endDate;
}
