package com.j20_pos.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryOverviewDTO {
    private Long totalProducts;
    private Long lowStockCount;
    private Long outOfStockCount;
    private Long totalStockUnits;
    private BigDecimal totalCostValue;
    private BigDecimal totalRetailValue;
    private BigDecimal projectedProfit;
}
