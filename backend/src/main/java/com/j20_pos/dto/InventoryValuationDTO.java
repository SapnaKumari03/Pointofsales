package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryValuationDTO {

    private Integer totalSkus;
    private Integer totalStockUnits;
    private BigDecimal totalCostValuation;
    private BigDecimal totalRetailValuation;
    private BigDecimal totalProjectedProfit;
    private Double overallMarginPercent;
    private List<ValuationItemDTO> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValuationItemDTO {
        private Long productId;
        private String sku;
        private String name;
        private String category;
        private Integer stockQuantity;
        private String unit;
        private BigDecimal costPrice;
        private BigDecimal sellingPrice;
        private BigDecimal totalCostValue;
        private BigDecimal totalRetailValue;
        private BigDecimal projectedProfit;
        private Double marginPercent;
        private String status;
    }
}
