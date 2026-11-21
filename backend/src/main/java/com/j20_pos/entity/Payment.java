package com.j20_pos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pos_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 20)
    private String paymentMethod; // CASH, UPI, CARD

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 12, scale = 2)
    private BigDecimal amountTendered;

    @Column(precision = 12, scale = 2)
    private BigDecimal changeGiven;

    @Column(length = 100)
    private String transactionReference;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String paymentStatus = "COMPLETED"; // COMPLETED, FAILED, REFUNDED

    @Column(length = 50)
    private String paymentGateway; // CASH_DRAWER, MOCK_UPI, MOCK_CARD

    @Column(nullable = false)
    private LocalDateTime paymentTime;

    @PrePersist
    protected void onCreate() {
        if (this.paymentTime == null) {
            this.paymentTime = LocalDateTime.now();
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = "COMPLETED";
        }
    }
}
