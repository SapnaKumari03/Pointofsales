package com.j20_pos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenShiftDTO {

    @NotNull(message = "Opening float is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Opening float must be non-negative")
    private BigDecimal openingFloat;

    private String cashierName;

    private String cashierId;

    private String terminalId;

    private String notes;
}
