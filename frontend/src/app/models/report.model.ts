export type DateFilterPreset = 'TODAY' | 'YESTERDAY' | 'LAST_7_DAYS' | 'THIS_MONTH' | 'LAST_30_DAYS' | 'CUSTOM' | 'ALL';
export type AggregationPeriod = 'daily' | 'weekly' | 'monthly';

export interface SalesRevenueReportItem {
  periodLabel: string;
  dateKey: string;
  orderCount: number;
  unitsSold: number;
  grossSales: number;
  discountAmount: number;
  taxCollected: number;
  netRevenue: number;
  averageOrderValue: number;
}

export interface SalesReportSummary {
  totalRevenue: number;
  totalOrders: number;
  totalUnitsSold: number;
  totalTax: number;
  totalDiscount: number;
  averageOrderValue: number;
  highestDayRevenue: number;
  highestDayLabel: string;
}

export interface BestSellingProductItem {
  rank: number;
  productId: number;
  name: string;
  sku: string;
  category: string;
  unitsSold: number;
  revenue: number;
  cost: number;
  grossProfit: number;
  profitMarginPercent: number;
  currentStock: number;
  stockStatus: string;
  marketSharePercent: number;
}

export interface CategorySalesItem {
  category: string;
  unitsSold: number;
  totalRevenue: number;
  ordersCount: number;
  revenueSharePercent: number;
  averageUnitSellingPrice: number;
}

export interface InventoryValuationItem {
  productId: number;
  sku: string;
  name: string;
  category: string;
  stockQuantity: number;
  unit: string;
  costPrice: number;
  sellingPrice: number;
  totalCostValue: number;
  totalRetailValue: number;
  projectedProfit: number;
  marginPercent: number;
  status: string;
}

export interface InventoryValuationSummary {
  totalSkus: number;
  totalStockUnits: number;
  totalCostValuation: number;
  totalRetailValuation: number;
  totalProjectedProfit: number;
  overallMarginPercent: number;
}

export interface LowStockReportItem {
  productId: number;
  sku: string;
  name: string;
  category: string;
  currentStock: number;
  minStockLevel: number;
  unit: string;
  shortfall: number;
  recommendedReorderQty: number;
  unitCostPrice: number;
  estimatedReorderCost: number;
  urgency: 'OUT_OF_STOCK' | 'CRITICAL' | 'LOW';
  supplierStatus: string;
}

export interface GstSlabItem {
  rateLabel: string;
  taxRate: number;
  taxableAmount: number;
  cgstRate: number;
  cgstAmount: number;
  sgstRate: number;
  sgstAmount: number;
  totalGst: number;
  itemCount: number;
  invoiceCount: number;
}

export interface GstSummaryReport {
  slabs: GstSlabItem[];
  totalTaxableAmount: number;
  totalCgst: number;
  totalSgst: number;
  grandTotalGst: number;
}

export interface PaymentMethodAnalysisItem {
  method: 'CASH' | 'UPI' | 'CARD';
  label: string;
  totalAmount: number;
  transactionCount: number;
  sharePercent: number;
  averageTicketSize: number;
  iconName: string;
}
