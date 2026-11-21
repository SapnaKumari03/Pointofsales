package com.j20_pos.service;

import com.j20_pos.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

    DashboardSummaryDTO getDashboardSummary();

    SalesReportDTO getSalesReport(String period, LocalDate startDate, LocalDate endDate);

    List<BestSellerDTO> getBestSellers(Integer limit, LocalDate startDate, LocalDate endDate);

    List<CategorySalesDTO> getCategorySales(LocalDate startDate, LocalDate endDate);

    GstSummaryDTO getGstSummary(LocalDate startDate, LocalDate endDate);
}
