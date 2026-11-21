package com.j20_pos.service;

import com.j20_pos.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    DashboardReportDTO getDashboardReport();

    SalesSummaryDTO getSalesSummary(LocalDate startDate, LocalDate endDate);

    List<SalesTrendDTO> getSalesTrend(String period, LocalDate startDate, LocalDate endDate);

    List<TopSellingProductDTO> getTopSellingProducts(Integer limit, LocalDate startDate, LocalDate endDate);

    List<CategorySalesDTO> getCategorySales(LocalDate startDate, LocalDate endDate);

    List<PaymentMethodSummaryDTO> getPaymentSummary(LocalDate startDate, LocalDate endDate);

    InventoryValuationDTO getInventoryValuation();

    LowStockReportDTO getLowStockReport();
}
