export interface SalesSummary {
  // 4 Core Required Summary Cards
  totalSales: number;           // e.g. Today's Total Sales in INR
  totalOrders: number;          // Total Orders count
  revenue: number;              // Monthly / Total Revenue in INR
  lowStockCount: number;        // Products below safety threshold

  // Contextual detail metrics
  salesGrowthPercentage: number;
  averageOrderValue: number;
  monthlyRevenueTarget: number;
  revenueTargetPercentage: number;
  criticalStockCount: number;
}

export type PaymentMethod = 'UPI' | 'CASH' | 'CARD';

export interface RecentTransaction {
  invoiceNo: string;
  customerName: string;
  itemsCount: number;
  totalAmount: number;
  paymentMethod: PaymentMethod;
  timestamp: string;
  cashierName: string;
  status: 'COMPLETED' | 'PENDING';
}

export interface LowStockItem {
  id: number;
  sku: string;
  name: string;
  category: string;
  currentStock: number;
  minStockLevel: number;
  unitPrice: number;
  unit: string;
}

export interface SalesChartPoint {
  label: string;
  sales: number;
  orders: number;
}

export interface PaymentBreakdown {
  method: PaymentMethod;
  amount: number;
  percentage: number;
  transactionsCount: number;
}

export interface DashboardData {
  summary: SalesSummary;
  weeklySales: SalesChartPoint[];
  monthlySales: SalesChartPoint[];
  paymentBreakdown: PaymentBreakdown[];
  recentTransactions: RecentTransaction[];
  lowStockItems: LowStockItem[];
}
