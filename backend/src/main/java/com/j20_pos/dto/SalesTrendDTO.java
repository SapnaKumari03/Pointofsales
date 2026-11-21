package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesTrendDTO {

    private String dateKey;
    private String periodLabel;
    private BigDecimal sales;
    private Integer orders;
    private Integer unitsSold;
    private BigDecimal averageOrderValue;
}
