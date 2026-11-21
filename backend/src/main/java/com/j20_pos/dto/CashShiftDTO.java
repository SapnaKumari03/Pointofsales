package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashShiftDTO {

    private Long id;
    private String shiftNumber;
    private String shiftCode; // alias for shiftNumber for frontend compatibility
    private String cashierId;
    private String cashierName;
    private String terminalId;
    private LocalDateTime startTime;
    private String openedAt;
    private LocalDateTime endTime;
    private String closedAt;
    private String status; // OPEN / ACTIVE / CLOSED
    private BigDecimal openingFloat;
    private BigDecimal cashSales;
    private BigDecimal cashPaidOut;
    private BigDecimal expectedCash;
    private BigDecimal actualCash;
    private BigDecimal variance;
    private String varianceType; // BALANCED, SURPLUS, SHORTAGE
    private String closureNotes;
    private String notes; // alias for closureNotes
    private String cashCountDetails;
    private LocalDateTime createdAt;
    private List<CashTransactionDTO> transactions;
}
