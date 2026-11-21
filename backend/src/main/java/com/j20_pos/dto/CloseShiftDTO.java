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
public class CloseShiftDTO {

    @NotNull(message = "Actual cash count is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Actual cash must be non-negative")
    private BigDecimal actualCash;

    private String closureNotes;

    private String cashCountDetails;
}
