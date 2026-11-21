package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReportDTO {

    private String period;
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private Integer totalUnitsSold;
    private BigDecimal totalTax;
    private BigDecimal totalDiscount;
    private BigDecimal averageOrderValue;
    private BigDecimal highestDayRevenue;
    private String highestDayLabel;
    private List<SalesRevenueItemDTO> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesRevenueItemDTO {
        private String periodLabel;
        private String dateKey;
        private Integer orderCount;
        private Integer unitsSold;
        private BigDecimal grossSales;
        private BigDecimal discountAmount;
        private BigDecimal taxCollected;
        private BigDecimal netRevenue;
        private BigDecimal averageOrderValue;
    }
}
