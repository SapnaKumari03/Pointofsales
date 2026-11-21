package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySalesDTO {

    private String category;
    private Integer unitsSold;
    private BigDecimal totalRevenue;
    private Integer ordersCount;
    private Double revenueSharePercent;
    private BigDecimal averageUnitSellingPrice;
}
