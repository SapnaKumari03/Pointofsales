package com.j20_pos.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private String category;
    private String movementType;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private String reason;
    private String referenceNumber;
    private String performedBy;
    private LocalDateTime timestamp;
}
