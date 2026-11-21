import { Component, OnInit } from '@angular/core';
import { PosService } from '../../services/pos.service';
import { ProductService } from '../../services/product.service';
import { ReportsService } from '../../services/reports.service';
import { PosOrder } from '../../models/pos.model';
import { Product } from '../../models/product.model';
import {
  DateFilterPreset,
  AggregationPeriod,
  SalesRevenueReportItem,
  SalesReportSummary,
  BestSellingProductItem,
  CategorySalesItem,
  InventoryValuationItem,
  InventoryValuationSummary,
  LowStockReportItem,
  GstSummaryReport,
  PaymentMethodAnalysisItem
} from '../../models/report.model';

@Component({
  selector: 'app-reports-analytics',
  templateUrl: './reports-analytics.component.html',
  styleUrls: ['./reports-analytics.component.css']
})
export class ReportsAnalyticsComponent implements OnInit {
  // Navigation tabs
  activeTab: 'sales' | 'best-sellers' | 'categories' | 'gst' | 'payments' | 'inventory' | 'low-stock' = 'sales';

  // Date filters
  datePreset: DateFilterPreset = 'THIS_MONTH';
  customStartDate = '';
  customEndDate = '';
  aggregationPeriod: AggregationPeriod = 'daily';

  // Raw data from services
  allOrders: PosOrder[] = [];
  allProducts: Product[] = [];
  filteredOrders: PosOrder[] = [];

  // Report results
  salesReportItems: SalesRevenueReportItem[] = [];
  salesSummary: SalesReportSummary = {
    totalRevenue: 0,
    totalOrders: 0,
    totalUnitsSold: 0,
    totalTax: 0,
    totalDiscount: 0,
    averageOrderValue: 0,
    highestDayRevenue: 0,
    highestDayLabel: 'N/A'
  };

  bestSellers: BestSellingProductItem[] = [];
  categorySales: CategorySalesItem[] = [];

  inventoryValuationItems: InventoryValuationItem[] = [];
  inventorySummary: InventoryValuationSummary = {
    totalSkus: 0,
    totalStockUnits: 0,
    totalCostValuation: 0,
    totalRetailValuation: 0,
    totalProjectedProfit: 0,
    overallMarginPercent: 0
  };

  lowStockItems: LowStockReportItem[] = [];

  gstReport: GstSummaryReport = {
    slabs: [],
    totalTaxableAmount: 0,
    totalCgst: 0,
    totalSgst: 0,
    grandTotalGst: 0
  };

  paymentMethodReports: PaymentMethodAnalysisItem[] = [];

  constructor(
    private posService: PosService,
    private productService: ProductService,
    private reportsService: ReportsService
  ) {}

  ngOnInit(): void {
    // Initialize default date inputs (current month)
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    this.customStartDate = this.formatDateInput(firstDay);
    this.customEndDate = this.formatDateInput(today);

    this.loadData();
  }

  loadData(): void {
    // 1. Subscribe to POS completed orders
    this.posService.getCompletedOrders().subscribe(orders => {
      this.allOrders = orders;
      this.recalculateReports();
    });

    // 2. Subscribe to Products
    this.productService.getProducts().subscribe(products => {
      this.allProducts = products;
      this.recalculateInventoryReports();
      this.recalculateReports();
    });
  }

  setTab(tab: 'sales' | 'best-sellers' | 'categories' | 'gst' | 'payments' | 'inventory' | 'low-stock'): void {
    this.activeTab = tab;
  }

  setDatePreset(preset: DateFilterPreset): void {
    this.datePreset = preset;
    this.recalculateReports();
  }

  setAggregation(period: AggregationPeriod): void {
    this.aggregationPeriod = period;
    this.recalculateSalesReportOnly();
  }

  applyCustomDateFilter(): void {
    this.datePreset = 'CUSTOM';
    this.recalculateReports();
  }

  recalculateReports(): void {
    // 1. Filter orders based on active preset
    this.filteredOrders = this.reportsService.filterOrdersByDateRange(
      this.allOrders,
      this.datePreset,
      this.customStartDate,
      this.customEndDate
    );

    // 2. Recalculate Sales and Revenue
    this.recalculateSalesReportOnly();

    // 3. Recalculate Best Sellers
    this.bestSellers = this.reportsService.getBestSellingProducts(this.filteredOrders, this.allProducts);

    // 4. Recalculate Categories
    this.categorySales = this.reportsService.getCategorySalesAnalysis(this.filteredOrders);

    // 5. Recalculate GST
    this.gstReport = this.reportsService.getGstSummary(this.filteredOrders);

    // 6. Recalculate Payment methods
    this.paymentMethodReports = this.reportsService.getPaymentMethodAnalysis(this.filteredOrders);
  }

  recalculateSalesReportOnly(): void {
    const report = this.reportsService.getSalesReport(this.filteredOrders, this.aggregationPeriod);
    this.salesReportItems = report.items;
    this.salesSummary = report.summary;
  }

  recalculateInventoryReports(): void {
    const val = this.reportsService.getInventoryValuation(this.allProducts);
    this.inventoryValuationItems = val.items;
    this.inventorySummary = val.summary;

    this.lowStockItems = this.reportsService.getLowStockReport(this.allProducts);
  }

  // Chart percentage calculation helper
  getChartBarHeight(revenue: number): number {
    if (!this.salesReportItems || this.salesReportItems.length === 0) return 0;
    const max = Math.max(...this.salesReportItems.map(i => i.netRevenue), 1);
    return Math.max(8, Math.round((revenue / max) * 100));
  }

  // Export current report view to CSV
  exportCurrentReport(): void {
    const dateStr = new Date().toISOString().split('T')[0];

    switch (this.activeTab) {
      case 'sales': {
        const headers = ['Period / Date', 'Date Key', 'Orders Count', 'Units Sold', 'Gross Sales (INR)', 'Discounts (INR)', 'GST Tax (INR)', 'Net Revenue (INR)', 'Avg Basket Value (INR)'];
        const rows = this.salesReportItems.map(item => [
          item.periodLabel,
          item.dateKey,
          item.orderCount,
          item.unitsSold,
          item.grossSales,
          item.discountAmount,
          item.taxCollected,
          item.netRevenue,
          item.averageOrderValue
        ]);
        this.reportsService.exportToCsv(`J20_Sales_Revenue_Report_${this.aggregationPeriod}_${dateStr}`, headers, rows);
        break;
      }

      case 'best-sellers': {
        const headers = ['Rank', 'Product Name', 'SKU', 'Category', 'Units Sold', 'Revenue (INR)', 'Cost (INR)', 'Gross Profit (INR)', 'Margin %', 'Stock Qty', 'Status', 'Market Share %'];
        const rows = this.bestSellers.map(item => [
          item.rank,
          item.name,
          item.sku,
          item.category,
          item.unitsSold,
          item.revenue,
          item.cost,
          item.grossProfit,
          `${item.profitMarginPercent}%`,
          item.currentStock,
          item.stockStatus,
          `${item.marketSharePercent}%`
        ]);
        this.reportsService.exportToCsv(`J20_Best_Selling_Products_${dateStr}`, headers, rows);
        break;
      }

      case 'categories': {
        const headers = ['Category', 'Units Sold', 'Total Revenue (INR)', 'Orders Count', 'Revenue Share %', 'Avg Unit Price (INR)'];
        const rows = this.categorySales.map(item => [
          item.category,
          item.unitsSold,
          item.totalRevenue,
          item.ordersCount,
          `${item.revenueSharePercent}%`,
          item.averageUnitSellingPrice
        ]);
        this.reportsService.exportToCsv(`J20_Category_Sales_Analysis_${dateStr}`, headers, rows);
        break;
      }

      case 'gst': {
        const headers = ['Tax Slab', 'Tax Rate %', 'Taxable Amount (INR)', 'CGST Rate %', 'CGST Amount (INR)', 'SGST Rate %', 'SGST Amount (INR)', 'Total GST (INR)', 'Items Count', 'Invoices Count'];
        const rows = this.gstReport.slabs.map(slab => [
          slab.rateLabel,
          `${slab.taxRate}%`,
          slab.taxableAmount,
          `${slab.cgstRate}%`,
          slab.cgstAmount,
          `${slab.sgstRate}%`,
          slab.sgstAmount,
          slab.totalGst,
          slab.itemCount,
          slab.invoiceCount
        ]);
        // Add grand total row
        rows.push([
          'GRAND TOTAL',
          '',
          this.gstReport.totalTaxableAmount,
          '',
          this.gstReport.totalCgst,
          '',
          this.gstReport.totalSgst,
          this.gstReport.grandTotalGst,
          '',
          ''
        ]);
        this.reportsService.exportToCsv(`J20_GST_Tax_Report_${dateStr}`, headers, rows);
        break;
      }

      case 'payments': {
        const headers = ['Payment Method', 'Total Amount (INR)', 'Transaction Count', 'Share %', 'Avg Ticket Size (INR)'];
        const rows = this.paymentMethodReports.map(item => [
          item.label,
          item.totalAmount,
          item.transactionCount,
          `${item.sharePercent}%`,
          item.averageTicketSize
        ]);
        this.reportsService.exportToCsv(`J20_Payment_Methods_Report_${dateStr}`, headers, rows);
        break;
      }

      case 'inventory': {
        const headers = ['SKU', 'Product Name', 'Category', 'Stock Qty', 'Unit', 'Unit Cost (INR)', 'Unit Price (INR)', 'Total Cost Value (INR)', 'Total Retail Value (INR)', 'Projected Profit (INR)', 'Margin %', 'Stock Status'];
        const rows = this.inventoryValuationItems.map(item => [
          item.sku,
          item.name,
          item.category,
          item.stockQuantity,
          item.unit,
          item.costPrice,
          item.sellingPrice,
          item.totalCostValue,
          item.totalRetailValue,
          item.projectedProfit,
          `${item.marginPercent}%`,
          item.status
        ]);
        this.reportsService.exportToCsv(`J20_Inventory_Valuation_Report_${dateStr}`, headers, rows);
        break;
      }

      case 'low-stock': {
        const headers = ['SKU', 'Product Name', 'Category', 'Current Stock', 'Min Stock Level', 'Unit', 'Shortfall', 'Recommended Reorder Qty', 'Unit Cost (INR)', 'Estimated Reorder Cost (INR)', 'Urgency', 'Supplier Status'];
        const rows = this.lowStockItems.map(item => [
          item.sku,
          item.name,
          item.category,
          item.currentStock,
          item.minStockLevel,
          item.unit,
          item.shortfall,
          item.recommendedReorderQty,
          item.unitCostPrice,
          item.estimatedReorderCost,
          item.urgency,
          item.supplierStatus
        ]);
        this.reportsService.exportToCsv(`J20_Low_Stock_Reorder_Report_${dateStr}`, headers, rows);
        break;
      }
    }
  }

  private formatDateInput(d: Date): string {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }
}
