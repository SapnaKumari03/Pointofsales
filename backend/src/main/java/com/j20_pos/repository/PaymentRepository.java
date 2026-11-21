package com.j20_pos.repository;

import com.j20_pos.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByPaymentMethodIgnoreCase(String paymentMethod);
    List<Payment> findByPaymentTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Payment> findAllByOrderByPaymentTimeDesc();
}
