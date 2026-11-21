package com.j20_pos.service;

import com.j20_pos.dto.InventoryMovementDTO;
import com.j20_pos.dto.InventoryOverviewDTO;
import com.j20_pos.dto.ProductDTO;
import com.j20_pos.dto.StockAdjustmentDTO;

import java.util.List;

public interface InventoryService {
    ProductDTO adjustStock(StockAdjustmentDTO dto);
    List<ProductDTO> getLowStockProducts();
    List<ProductDTO> getOutOfStockProducts();
    InventoryOverviewDTO getInventoryOverview();
    List<InventoryMovementDTO> getMovementHistory(Long productId);
}
