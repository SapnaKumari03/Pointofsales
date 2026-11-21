package com.j20_pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pos_cash_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String shiftNumber;

    @Column(length = 50)
    private String cashierId;

    @Column(nullable = false, length = 100)
    private String cashierName;

    @Column(length = 50)
    private String terminalId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "OPEN"; // OPEN, CLOSED

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal openingFloat;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal cashSales = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal cashPaidOut = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal expectedCash = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal actualCash;

    @Column(precision = 12, scale = 2)
    private BigDecimal variance;

    @Column(length = 500)
    private String closureNotes;

    @Column(length = 1000)
    private String cashCountDetails;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CashTransaction> transactions = new ArrayList<>();

    public void addTransaction(CashTransaction tx) {
        transactions.add(tx);
        tx.setShift(this);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
        }
    }
}
