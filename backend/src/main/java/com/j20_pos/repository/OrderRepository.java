package com.j20_pos.repository;

import com.j20_pos.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByInvoiceNumber(String invoiceNumber);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByOrderDateBetweenOrderByCreatedAtDesc(LocalDate startDate, LocalDate endDate);
    List<Order> findByPaymentMethodIgnoreCase(String paymentMethod);

    @Query("SELECT o FROM Order o WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(o.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(o.customerPhone) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:paymentMethod IS NULL OR :paymentMethod = '' OR :paymentMethod = 'ALL' OR UPPER(o.paymentMethod) = UPPER(:paymentMethod)) AND " +
           "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
           "(:endDate IS NULL OR o.orderDate <= :endDate) " +
           "ORDER BY o.createdAt DESC")
    List<Order> searchOrders(
        @Param("query") String query,
        @Param("paymentMethod") String paymentMethod,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
