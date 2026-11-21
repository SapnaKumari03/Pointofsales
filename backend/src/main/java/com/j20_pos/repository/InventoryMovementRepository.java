package com.j20_pos.repository;

import com.j20_pos.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByProductIdOrderByTimestampDesc(Long productId);
    List<InventoryMovement> findAllByOrderByTimestampDesc();
}
