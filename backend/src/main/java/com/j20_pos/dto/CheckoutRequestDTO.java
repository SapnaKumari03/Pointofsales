package com.j20_pos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequestDTO {

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<CartItemDTO> items;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CASH, UPI, CARD

    @DecimalMin(value = "0.0", inclusive = true, message = "Amount paid must be non-negative")
    private BigDecimal amountPaid;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;

    private String customerName;

    private String customerPhone;

    private String cashierName;

    private String terminalId;
}
