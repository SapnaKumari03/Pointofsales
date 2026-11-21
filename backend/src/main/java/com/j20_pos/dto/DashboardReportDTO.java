package com.j20_pos.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardReportDTO {

    private SalesSummaryDTO salesSummary;
    private List<SalesTrendDTO> weeklyTrend;
    private List<TopSellingProductDTO> topProducts;
    private List<CategorySalesDTO> categoryBreakdown;
    private List<PaymentMethodSummaryDTO> paymentBreakdown;
    private InventoryValuationDTO inventoryOverview;
    private LowStockReportDTO lowStockAlerts;
}
