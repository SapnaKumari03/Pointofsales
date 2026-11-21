package com.j20_pos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashPayoutDTO {

    @NotNull(message = "Payout amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Payout amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Reason for cash payout is required")
    private String reason;

    @NotBlank(message = "Recipient (paidTo) is required")
    private String paidTo;

    private String approvedBy;
}
