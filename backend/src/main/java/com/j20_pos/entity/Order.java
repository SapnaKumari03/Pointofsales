package com.j20_pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pos_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(length = 20)
    private String orderTime;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @Column(nullable = false, length = 20)
    private String paymentMethod; // CASH, UPI, CARD

    @Column(precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal changeDue = BigDecimal.ZERO;

    @Column(length = 100)
    private String customerName;

    @Column(length = 20)
    private String customerPhone;

    @Column(length = 100)
    private String cashierName;

    @Column(length = 50)
    private String terminalId;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "COMPLETED"; // COMPLETED, REFUNDED, CANCELLED

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.orderDate == null) {
            this.orderDate = LocalDate.now();
        }
    }
}
