package com.j20_pos.controller;

import com.j20_pos.dto.*;
import com.j20_pos.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/api/dashboard/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary() {
        DashboardSummaryDTO summary = analyticsService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.ok("Dashboard summary retrieved successfully", summary));
    }

    @GetMapping("/api/reports/sales")
    public ResponseEntity<ApiResponse<SalesReportDTO>> getSalesReport(
            @RequestParam(required = false, defaultValue = "daily") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        SalesReportDTO report = analyticsService.getSalesReport(period, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Sales report retrieved successfully", report));
    }

    @GetMapping("/api/reports/best-sellers")
    public ResponseEntity<ApiResponse<List<BestSellerDTO>>> getBestSellers(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<BestSellerDTO> bestSellers = analyticsService.getBestSellers(limit, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Best-selling products retrieved successfully", bestSellers));
    }

    @GetMapping("/api/analytics/category-sales")
    public ResponseEntity<ApiResponse<List<CategorySalesDTO>>> getCategorySales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CategorySalesDTO> categorySales = analyticsService.getCategorySales(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Category sales report retrieved successfully", categorySales));
    }

    @GetMapping("/api/reports/gst-summary")
    public ResponseEntity<ApiResponse<GstSummaryDTO>> getGstSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        GstSummaryDTO gstSummary = analyticsService.getGstSummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("GST summary report retrieved successfully", gstSummary));
    }
}
