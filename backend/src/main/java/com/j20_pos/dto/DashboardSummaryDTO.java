package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {

    private BigDecimal totalSales;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private Integer lowStockCount;
    private BigDecimal averageOrderValue;
    private BigDecimal todaySales;
    private Integer todayOrders;
    private BigDecimal weeklyRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal cashSales;
    private BigDecimal upiSales;
    private BigDecimal cardSales;
}
