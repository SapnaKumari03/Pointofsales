package com.j20_pos.repository;

import com.j20_pos.entity.CashTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {
    List<CashTransaction> findByShiftIdOrderByTimestampDesc(Long shiftId);
    List<CashTransaction> findAllByOrderByTimestampDesc();
}
