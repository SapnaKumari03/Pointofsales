package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockReportDTO {

    private Integer totalLowStockItems;
    private Integer outOfStockCount;
    private Integer criticalStockCount;
    private BigDecimal estimatedReorderCost;
    private List<LowStockItemDTO> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LowStockItemDTO {
        private Long productId;
        private String sku;
        private String name;
        private String category;
        private Integer currentStock;
        private Integer minStockLevel;
        private String unit;
        private Integer shortfall;
        private Integer recommendedReorderQty;
        private BigDecimal unitCostPrice;
        private BigDecimal estimatedReorderCost;
        private String urgency; // OUT_OF_STOCK, CRITICAL, LOW
        private String status;
    }
}
