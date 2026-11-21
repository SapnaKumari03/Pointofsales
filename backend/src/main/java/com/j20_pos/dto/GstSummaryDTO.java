package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstSummaryDTO {

    private BigDecimal totalTaxableAmount;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal grandTotalGst;
    private List<GstSlabDTO> slabs;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GstSlabDTO {
        private String rateLabel;
        private Double taxRate;
        private BigDecimal taxableAmount;
        private Double cgstRate;
        private BigDecimal cgstAmount;
        private Double sgstRate;
        private BigDecimal sgstAmount;
        private BigDecimal totalGst;
        private Integer itemCount;
        private Integer invoiceCount;
    }
}
