package com.j20_pos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustmentDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Adjustment type is required (STOCK_IN, STOCK_OUT, or ADJUSTMENT)")
    private String adjustmentType; // STOCK_IN, STOCK_OUT, ADJUSTMENT

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Reason for adjustment is required")
    private String reason;

    private String referenceNumber;

    private String performedBy;

    // Optional direct target stock level for reconciliation adjustments
    private Integer targetStock;
}
