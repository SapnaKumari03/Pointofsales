package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellingProductDTO {

    private Integer rank;
    private Long productId;
    private String name;
    private String sku;
    private String category;
    private Integer unitsSold;
    private BigDecimal revenue;
    private BigDecimal cost;
    private BigDecimal grossProfit;
    private Double profitMarginPercent;
    private Integer currentStock;
    private String stockStatus; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    private Double marketSharePercent;
}
