package com.j20_pos.controller;

import com.j20_pos.dto.*;
import com.j20_pos.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardReportDTO>> getDashboardReport() {
        DashboardReportDTO dashboard = reportService.getDashboardReport();
        return ResponseEntity.ok(ApiResponse.ok("Dashboard report retrieved successfully", dashboard));
    }

    @GetMapping("/sales-summary")
    public ResponseEntity<ApiResponse<SalesSummaryDTO>> getSalesSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        SalesSummaryDTO summary = reportService.getSalesSummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Sales summary retrieved successfully", summary));
    }

    @GetMapping("/sales-trend")
    public ResponseEntity<ApiResponse<List<SalesTrendDTO>>> getSalesTrend(
            @RequestParam(required = false, defaultValue = "daily") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<SalesTrendDTO> trend = reportService.getSalesTrend(period, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Sales trend retrieved successfully", trend));
    }

    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<TopSellingProductDTO>>> getTopSellingProducts(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TopSellingProductDTO> topProducts = reportService.getTopSellingProducts(limit, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Top-selling products retrieved successfully", topProducts));
    }

    @GetMapping("/category-sales")
    public ResponseEntity<ApiResponse<List<CategorySalesDTO>>> getCategorySales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CategorySalesDTO> categorySales = reportService.getCategorySales(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Category sales retrieved successfully", categorySales));
    }

    @GetMapping("/payment-summary")
    public ResponseEntity<ApiResponse<List<PaymentMethodSummaryDTO>>> getPaymentSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PaymentMethodSummaryDTO> paymentSummary = reportService.getPaymentSummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Payment method summary retrieved successfully", paymentSummary));
    }

    @GetMapping("/inventory-valuation")
    public ResponseEntity<ApiResponse<InventoryValuationDTO>> getInventoryValuation() {
        InventoryValuationDTO valuation = reportService.getInventoryValuation();
        return ResponseEntity.ok(ApiResponse.ok("Inventory valuation retrieved successfully", valuation));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<LowStockReportDTO>> getLowStockReport() {
        LowStockReportDTO lowStock = reportService.getLowStockReport();
        return ResponseEntity.ok(ApiResponse.ok("Low stock report retrieved successfully", lowStock));
    }
}
