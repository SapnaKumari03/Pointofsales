package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDTO {

    private Long id;
    private Long productId;
    private ProductDTO product;
    private String productName;
    private String sku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Double taxRate;
    private BigDecimal taxAmount;
    private BigDecimal subtotal;
    private BigDecimal total;
}
