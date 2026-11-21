package com.j20_pos.controller;

import com.j20_pos.dto.ApiResponse;
import com.j20_pos.dto.InventoryMovementDTO;
import com.j20_pos.dto.InventoryOverviewDTO;
import com.j20_pos.dto.ProductDTO;
import com.j20_pos.dto.StockAdjustmentDTO;
import com.j20_pos.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<InventoryOverviewDTO>> getInventoryOverview() {
        InventoryOverviewDTO overview = inventoryService.getInventoryOverview();
        return ResponseEntity.ok(ApiResponse.ok("Inventory overview retrieved successfully", overview));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getLowStockProducts() {
        List<ProductDTO> lowStock = inventoryService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.ok("Low stock products retrieved successfully", lowStock));
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getOutOfStockProducts() {
        List<ProductDTO> outOfStock = inventoryService.getOutOfStockProducts();
        return ResponseEntity.ok(ApiResponse.ok("Out of stock products retrieved successfully", outOfStock));
    }

    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<ProductDTO>> adjustStock(@Valid @RequestBody StockAdjustmentDTO dto) {
        ProductDTO updatedProduct = inventoryService.adjustStock(dto);
        return ResponseEntity.ok(ApiResponse.ok("Stock adjusted successfully", updatedProduct));
    }

    @GetMapping("/movements")
    public ResponseEntity<ApiResponse<List<InventoryMovementDTO>>> getMovementHistory(
            @RequestParam(required = false) Long productId) {
        List<InventoryMovementDTO> movements = inventoryService.getMovementHistory(productId);
        return ResponseEntity.ok(ApiResponse.ok("Inventory movements retrieved successfully", movements));
    }
}
