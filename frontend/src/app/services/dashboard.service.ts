import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { DashboardData } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  // Configured to easily swap to HttpClient: this.http.get<DashboardData>('/api/reports/dashboard-summary')

  private mockData: DashboardData = {
    summary: {
      totalSales: 86450,
      totalOrders: 146,
      revenue: 742800,
      lowStockCount: 6,
      salesGrowthPercentage: 16.5,
      averageOrderValue: 592,
      monthlyRevenueTarget: 850000,
      revenueTargetPercentage: 87.4,
      criticalStockCount: 2
    },
    weeklySales: [
      { label: 'Mon', sales: 62400, orders: 112 },
      { label: 'Tue', sales: 68900, orders: 124 },
      { label: 'Wed', sales: 71200, orders: 128 },
      { label: 'Thu', sales: 69500, orders: 119 },
      { label: 'Fri', sales: 82400, orders: 139 },
      { label: 'Sat', sales: 98600, orders: 172 },
      { label: 'Sun', sales: 86450, orders: 146 }
    ],
    monthlySales: [
      { label: 'May', sales: 580000, orders: 980 },
      { label: 'Jun', sales: 620000, orders: 1045 },
      { label: 'Jul', sales: 645000, orders: 1090 },
      { label: 'Aug', sales: 690000, orders: 1150 },
      { label: 'Sep', sales: 715000, orders: 1210 },
      { label: 'Oct', sales: 785000, orders: 1320 },
      { label: 'Nov', sales: 742800, orders: 1280 }
    ],
    paymentBreakdown: [
      { method: 'UPI', amount: 46680, percentage: 54, transactionsCount: 79 },
      { method: 'CASH', amount: 26250, percentage: 30, transactionsCount: 45 },
      { method: 'CARD', amount: 13520, percentage: 16, transactionsCount: 22 }
    ],
    recentTransactions: [
      {
        invoiceNo: 'INV-2026-1048',
        customerName: 'Ananya Rao',
        itemsCount: 5,
        totalAmount: 1850,
        paymentMethod: 'UPI',
        timestamp: '12:42 PM',
        cashierName: 'Ramesh K.',
        status: 'COMPLETED'
      },
      {
        invoiceNo: 'INV-2026-1047',
        customerName: 'Rajesh Kumar',
        itemsCount: 2,
        totalAmount: 420,
        paymentMethod: 'CASH',
        timestamp: '12:35 PM',
        cashierName: 'Ramesh K.',
        status: 'COMPLETED'
      },
      {
        invoiceNo: 'INV-2026-1046',
        customerName: 'Walk-in Customer',
        itemsCount: 8,
        totalAmount: 3240,
        paymentMethod: 'CARD',
        timestamp: '12:20 PM',
        cashierName: 'Pooja S.',
        status: 'COMPLETED'
      },
      {
        invoiceNo: 'INV-2026-1045',
        customerName: 'Priya Sharma',
        itemsCount: 1,
        totalAmount: 160,
        paymentMethod: 'UPI',
        timestamp: '12:08 PM',
        cashierName: 'Ramesh K.',
        status: 'COMPLETED'
      },
      {
        invoiceNo: 'INV-2026-1044',
        customerName: 'Walk-in Customer',
        itemsCount: 4,
        totalAmount: 980,
        paymentMethod: 'CASH',
        timestamp: '11:55 AM',
        cashierName: 'Pooja S.',
        status: 'COMPLETED'
      },
      {
        invoiceNo: 'INV-2026-1043',
        customerName: 'Dr. Suresh Patel',
        itemsCount: 12,
        totalAmount: 5410,
        paymentMethod: 'UPI',
        timestamp: '11:40 AM',
        cashierName: 'Ramesh K.',
        status: 'COMPLETED'
      },
      {
        invoiceNo: 'INV-2026-1042',
        customerName: 'Meenakshi S.',
        itemsCount: 3,
        totalAmount: 750,
        paymentMethod: 'CASH',
        timestamp: '11:15 AM',
        cashierName: 'Pooja S.',
        status: 'COMPLETED'
      }
    ],
    lowStockItems: [
      {
        id: 101,
        sku: 'GRO-ATT-001',
        name: 'Aashirvaad Shudh Chakki Atta 5kg',
        category: 'Flour & Grains',
        currentStock: 4,
        minStockLevel: 15,
        unitPrice: 245,
        unit: 'Bag'
      },
      {
        id: 102,
        sku: 'OIL-SUN-002',
        name: 'Fortune Sunlite Refined Sunflower Oil 1L',
        category: 'Edible Oils',
        currentStock: 6,
        minStockLevel: 20,
        unitPrice: 155,
        unit: 'Pouch'
      },
      {
        id: 103,
        sku: 'DAI-BUT-003',
        name: 'Amul Butter Pasteurised 500g',
        category: 'Dairy',
        currentStock: 3,
        minStockLevel: 12,
        unitPrice: 285,
        unit: 'Pack'
      },
      {
        id: 104,
        sku: 'RIC-BAS-004',
        name: 'India Gate Feast Rozzana Basmati 5kg',
        category: 'Rice',
        currentStock: 2,
        minStockLevel: 10,
        unitPrice: 490,
        unit: 'Bag'
      },
      {
        id: 105,
        sku: 'MAS-GAR-005',
        name: 'MDH Garam Masala 100g',
        category: 'Spices',
        currentStock: 5,
        minStockLevel: 15,
        unitPrice: 88,
        unit: 'Box'
      },
      {
        id: 106,
        sku: 'GRO-SLT-006',
        name: 'Tata Salt Vacuum Evaporated 1kg',
        category: 'Groceries',
        currentStock: 8,
        minStockLevel: 25,
        unitPrice: 28,
        unit: 'Pack'
      }
    ]
  };

  getDashboardData(): Observable<DashboardData> {
    return of(this.mockData);
  }
}
