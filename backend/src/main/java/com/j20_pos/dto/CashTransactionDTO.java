package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashTransactionDTO {

    private Long id;
    private Long shiftId;
    private String shiftCode;
    private String type; // FLOAT, SALE, PAYOUT, ADJUSTMENT
    private BigDecimal amount;
    private String referenceNumber;
    private String reason;
    private String paidTo;
    private String performedBy;
    private String approvedBy;
    private LocalDateTime timestamp;
    private String formattedTime;
}
