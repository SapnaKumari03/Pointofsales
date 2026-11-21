package com.j20_pos.repository;

import com.j20_pos.entity.CashShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashShiftRepository extends JpaRepository<CashShift, Long> {
    Optional<CashShift> findTopByStatusOrderByStartTimeDesc(String status);
    Optional<CashShift> findTopByStatusInOrderByStartTimeDesc(Collection<String> statuses);
    List<CashShift> findAllByOrderByStartTimeDesc();
    Optional<CashShift> findByShiftNumber(String shiftNumber);
}
