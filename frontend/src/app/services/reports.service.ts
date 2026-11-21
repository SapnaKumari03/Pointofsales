import { Injectable } from '@angular/core';
import { PosOrder } from '../models/pos.model';
import { Product } from '../models/product.model';
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
  GstSlabItem,
  PaymentMethodAnalysisItem
} from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportsService {
  constructor() {}

  /**
   * Filter orders based on preset or custom date range
   */
  filterOrdersByDateRange(
    orders: PosOrder[],
    preset: DateFilterPreset,
    customStart?: string,
    customEnd?: string
  ): PosOrder[] {
    const todayStr = this.formatDate(new Date());

    return orders.filter(order => {
      const orderDate = order.orderDate;

      switch (preset) {
        case 'TODAY':
          return orderDate === todayStr;

        case 'YESTERDAY': {
          const y = new Date();
          y.setDate(y.getDate() - 1);
          return orderDate === this.formatDate(y);
        }

        case 'LAST_7_DAYS': {
          const sevenDaysAgo = new Date();
          sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 6);
          const startStr = this.formatDate(sevenDaysAgo);
          return orderDate >= startStr && orderDate <= todayStr;
        }

        case 'THIS_MONTH': {
          const currentMonthPrefix = todayStr.substring(0, 7); // YYYY-MM
          return orderDate.startsWith(currentMonthPrefix);
        }

        case 'LAST_30_DAYS': {
          const thirtyDaysAgo = new Date();
          thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 29);
          const startStr = this.formatDate(thirtyDaysAgo);
          return orderDate >= startStr && orderDate <= todayStr;
        }

        case 'CUSTOM': {
          if (!customStart && !customEnd) return true;
          if (customStart && customEnd) {
            return orderDate >= customStart && orderDate <= customEnd;
          }
          if (customStart) return orderDate >= customStart;
          if (customEnd) return orderDate <= customEnd;
          return true;
        }

        case 'ALL':
        default:
          return true;
      }
    });
  }

  /**
   * Generate Sales and Revenue Report with period aggregation (Daily, Weekly, Monthly)
   */
  getSalesReport(
    orders: PosOrder[],
    period: AggregationPeriod = 'daily'
  ): { items: SalesRevenueReportItem[]; summary: SalesReportSummary } {
    const groups = new Map<string, { label: string; orders: PosOrder[] }>();

    orders.forEach(order => {
      const groupKey = this.getGroupKey(order.orderDate, period);
      if (!groups.has(groupKey.key)) {
        groups.set(groupKey.key, { label: groupKey.label, orders: [] });
      }
      groups.get(groupKey.key)!.orders.push(order);
    });

    // Sort group keys chronological ascending
    const sortedKeys = Array.from(groups.keys()).sort();

    const items: SalesRevenueReportItem[] = sortedKeys.map(key => {
      const group = groups.get(key)!;
      const groupOrders = group.orders;

      let unitsSold = 0;
      let grossSales = 0;
      let discountAmount = 0;
      let taxCollected = 0;
      let netRevenue = 0;

      groupOrders.forEach(o => {
        netRevenue += o.grandTotal;
        discountAmount += o.discountAmount || 0;
        taxCollected += o.taxAmount || 0;
        grossSales += (o.subtotal || 0) + (o.taxAmount || 0);

        if (o.items) {
          o.items.forEach(item => {
            unitsSold += item.quantity;
          });
        }
      });

      const orderCount = groupOrders.length;
      const averageOrderValue = orderCount > 0 ? Math.round(netRevenue / orderCount) : 0;

      return {
        periodLabel: group.label,
        dateKey: key,
        orderCount,
        unitsSold,
        grossSales: Math.round(grossSales * 100) / 100,
        discountAmount: Math.round(discountAmount * 100) / 100,
        taxCollected: Math.round(taxCollected * 100) / 100,
        netRevenue: Math.round(netRevenue * 100) / 100,
        averageOrderValue
      };
    });

    // Calculate overall summary
    let totalRevenue = 0;
    let totalOrders = 0;
    let totalUnitsSold = 0;
    let totalTax = 0;
    let totalDiscount = 0;
    let highestDayRevenue = 0;
    let highestDayLabel = 'N/A';

    items.forEach(item => {
      totalRevenue += item.netRevenue;
      totalOrders += item.orderCount;
      totalUnitsSold += item.unitsSold;
      totalTax += item.taxCollected;
      totalDiscount += item.discountAmount;

      if (item.netRevenue > highestDayRevenue) {
        highestDayRevenue = item.netRevenue;
        highestDayLabel = item.periodLabel;
      }
    });

    const averageOrderValue = totalOrders > 0 ? Math.round(totalRevenue / totalOrders) : 0;

    const summary: SalesReportSummary = {
      totalRevenue: Math.round(totalRevenue * 100) / 100,
      totalOrders,
      totalUnitsSold,
      totalTax: Math.round(totalTax * 100) / 100,
      totalDiscount: Math.round(totalDiscount * 100) / 100,
      averageOrderValue,
      highestDayRevenue: Math.round(highestDayRevenue * 100) / 100,
      highestDayLabel
    };

    return { items, summary };
  }

  /**
   * Best-Selling Products Report
   */
  getBestSellingProducts(orders: PosOrder[], allProducts: Product[]): BestSellingProductItem[] {
    const productMap = new Map<number, {
      name: string;
      sku: string;
      category: string;
      unitsSold: number;
      revenue: number;
      costPrice: number;
      stockQuantity: number;
      status: string;
    }>();

    // Map all products first
    allProducts.forEach(p => {
      productMap.set(p.id, {
        name: p.name,
        sku: p.sku,
        category: p.category,
        unitsSold: 0,
        revenue: 0,
        costPrice: p.costPrice,
        stockQuantity: p.stockQuantity,
        status: p.status
      });
    });

    // Aggregate from orders
    let totalSalesVolume = 0;
    orders.forEach(order => {
      order.items.forEach(item => {
        const pId = item.product.id;
        if (!productMap.has(pId)) {
          productMap.set(pId, {
            name: item.product.name,
            sku: item.product.sku,
            category: item.product.category,
            unitsSold: 0,
            revenue: 0,
            costPrice: item.product.costPrice || 0,
            stockQuantity: item.product.stockQuantity || 0,
            status: item.product.status || 'ACTIVE'
          });
        }
        const record = productMap.get(pId)!;
        record.unitsSold += item.quantity;
        record.revenue += item.total;
        totalSalesVolume += item.total;
      });
    });

    // Filter to items that have sales, sort descending
    const result: BestSellingProductItem[] = Array.from(productMap.entries())
      .filter(([_, data]) => data.unitsSold > 0)
      .map(([id, data]) => {
        const cost = Math.round(data.unitsSold * data.costPrice * 100) / 100;
        const grossProfit = Math.round((data.revenue - cost) * 100) / 100;
        const profitMarginPercent = data.revenue > 0 ? Math.round((grossProfit / data.revenue) * 1000) / 10 : 0;
        const marketSharePercent = totalSalesVolume > 0 ? Math.round((data.revenue / totalSalesVolume) * 1000) / 10 : 0;

        return {
          rank: 0,
          productId: id,
          name: data.name,
          sku: data.sku,
          category: data.category,
          unitsSold: data.unitsSold,
          revenue: Math.round(data.revenue * 100) / 100,
          cost,
          grossProfit,
          profitMarginPercent,
          currentStock: data.stockQuantity,
          stockStatus: data.stockQuantity === 0 ? 'Out of Stock' : (data.stockQuantity <= 10 ? 'Low Stock' : 'In Stock'),
          marketSharePercent
        };
      })
      .sort((a, b) => b.unitsSold - a.unitsSold);

    // Assign sequential ranks
    result.forEach((item, index) => {
      item.rank = index + 1;
    });

    return result;
  }

  /**
   * Category-wise Sales Analysis
   */
  getCategorySalesAnalysis(orders: PosOrder[]): CategorySalesItem[] {
    const categoryMap = new Map<string, {
      unitsSold: number;
      revenue: number;
      ordersSet: Set<string>;
    }>();

    let grandTotalRevenue = 0;

    orders.forEach(order => {
      order.items.forEach(item => {
        const cat = item.product.category || 'General';
        if (!categoryMap.has(cat)) {
          categoryMap.set(cat, {
            unitsSold: 0,
            revenue: 0,
            ordersSet: new Set<string>()
          });
        }
        const rec = categoryMap.get(cat)!;
        rec.unitsSold += item.quantity;
        rec.revenue += item.total;
        rec.ordersSet.add(order.invoiceNumber);
        grandTotalRevenue += item.total;
      });
    });

    const result: CategorySalesItem[] = Array.from(categoryMap.entries()).map(([category, data]) => {
      const revenueSharePercent = grandTotalRevenue > 0
        ? Math.round((data.revenue / grandTotalRevenue) * 1000) / 10
        : 0;
      const averageUnitSellingPrice = data.unitsSold > 0
        ? Math.round((data.revenue / data.unitsSold) * 100) / 100
        : 0;

      return {
        category,
        unitsSold: data.unitsSold,
        totalRevenue: Math.round(data.revenue * 100) / 100,
        ordersCount: data.ordersSet.size,
        revenueSharePercent,
        averageUnitSellingPrice
      };
    });

    return result.sort((a, b) => b.totalRevenue - a.totalRevenue);
  }

  /**
   * Inventory Valuation Report
   */
  getInventoryValuation(products: Product[]): { items: InventoryValuationItem[]; summary: InventoryValuationSummary } {
    let totalStockUnits = 0;
    let totalCostValuation = 0;
    let totalRetailValuation = 0;

    const items: InventoryValuationItem[] = products.map(p => {
      const totalCostValue = Math.round(p.stockQuantity * p.costPrice * 100) / 100;
      const totalRetailValue = Math.round(p.stockQuantity * p.sellingPrice * 100) / 100;
      const projectedProfit = Math.round((totalRetailValue - totalCostValue) * 100) / 100;
      const marginPercent = totalRetailValue > 0
        ? Math.round((projectedProfit / totalRetailValue) * 1000) / 10
        : 0;

      totalStockUnits += p.stockQuantity;
      totalCostValuation += totalCostValue;
      totalRetailValuation += totalRetailValue;

      return {
        productId: p.id,
        sku: p.sku,
        name: p.name,
        category: p.category,
        stockQuantity: p.stockQuantity,
        unit: p.unit,
        costPrice: p.costPrice,
        sellingPrice: p.sellingPrice,
        totalCostValue,
        totalRetailValue,
        projectedProfit,
        marginPercent,
        status: p.stockQuantity === 0 ? 'Out of Stock' : (p.stockQuantity <= p.minStockLevel ? 'Low Stock' : 'Optimal')
      };
    });

    const totalProjectedProfit = Math.round((totalRetailValuation - totalCostValuation) * 100) / 100;
    const overallMarginPercent = totalRetailValuation > 0
      ? Math.round((totalProjectedProfit / totalRetailValuation) * 1000) / 10
      : 0;

    const summary: InventoryValuationSummary = {
      totalSkus: products.length,
      totalStockUnits,
      totalCostValuation: Math.round(totalCostValuation * 100) / 100,
      totalRetailValuation: Math.round(totalRetailValuation * 100) / 100,
      totalProjectedProfit,
      overallMarginPercent
    };

    return { items, summary };
  }

  /**
   * Low Stock & Out-of-Stock Report
   */
  getLowStockReport(products: Product[]): LowStockReportItem[] {
    const alertItems = products.filter(p => p.stockQuantity <= p.minStockLevel);

    return alertItems.map(p => {
      const shortfall = Math.max(0, p.minStockLevel - p.stockQuantity);
      // Recommended reorder to bring up to 2x min stock + buffer
      const recommendedReorderQty = Math.max(shortfall + p.minStockLevel, 10);
      const estimatedReorderCost = Math.round(recommendedReorderQty * p.costPrice * 100) / 100;

      let urgency: 'OUT_OF_STOCK' | 'CRITICAL' | 'LOW';
      if (p.stockQuantity === 0) {
        urgency = 'OUT_OF_STOCK';
      } else if (p.stockQuantity <= Math.ceil(p.minStockLevel / 2)) {
        urgency = 'CRITICAL';
      } else {
        urgency = 'LOW';
      }

      return {
        productId: p.id,
        sku: p.sku,
        name: p.name,
        category: p.category,
        currentStock: p.stockQuantity,
        minStockLevel: p.minStockLevel,
        unit: p.unit,
        shortfall,
        recommendedReorderQty,
        unitCostPrice: p.costPrice,
        estimatedReorderCost,
        urgency,
        supplierStatus: urgency === 'OUT_OF_STOCK' ? 'PO Required Immediately' : 'Reorder Pending'
      };
    }).sort((a, b) => {
      const order = { OUT_OF_STOCK: 1, CRITICAL: 2, LOW: 3 };
      return order[a.urgency] - order[b.urgency] || b.estimatedReorderCost - a.estimatedReorderCost;
    });
  }

  /**
   * Indian GST Summary with CGST and SGST breakdown across 0%, 5%, 12%, 18% slabs
   */
  getGstSummary(orders: PosOrder[]): GstSummaryReport {
    // Slabs in Indian Retail: 0% (exempt), 5%, 12%, 18%
    const slabsConfig = [
      { rate: 0, label: '0% (Exempt)' },
      { rate: 5, label: '5% GST' },
      { rate: 12, label: '12% GST' },
      { rate: 18, label: '18% GST' }
    ];

    const slabMap = new Map<number, {
      rateLabel: string;
      taxableAmount: number;
      totalGst: number;
      itemCount: number;
      invoiceSet: Set<string>;
    }>();

    slabsConfig.forEach(s => {
      slabMap.set(s.rate, {
        rateLabel: s.label,
        taxableAmount: 0,
        totalGst: 0,
        itemCount: 0,
        invoiceSet: new Set<string>()
      });
    });

    let totalTaxableAmount = 0;
    let grandTotalGst = 0;

    orders.forEach(order => {
      order.items.forEach(item => {
        const rate = item.taxRate || 0;
        if (!slabMap.has(rate)) {
          slabMap.set(rate, {
            rateLabel: `${rate}% GST`,
            taxableAmount: 0,
            totalGst: 0,
            itemCount: 0,
            invoiceSet: new Set<string>()
          });
        }
        const slab = slabMap.get(rate)!;
        slab.taxableAmount += item.subtotal;
        slab.totalGst += item.taxAmount;
        slab.itemCount += item.quantity;
        slab.invoiceSet.add(order.invoiceNumber);

        totalTaxableAmount += item.subtotal;
        grandTotalGst += item.taxAmount;
      });
    });

    const slabs: GstSlabItem[] = Array.from(slabMap.entries()).map(([rate, data]) => {
      const cgstRate = rate / 2;
      const sgstRate = rate / 2;
      const cgstAmount = Math.round((data.totalGst / 2) * 100) / 100;
      const sgstAmount = Math.round((data.totalGst / 2) * 100) / 100;

      return {
        rateLabel: data.rateLabel,
        taxRate: rate,
        taxableAmount: Math.round(data.taxableAmount * 100) / 100,
        cgstRate,
        cgstAmount,
        sgstRate,
        sgstAmount,
        totalGst: Math.round(data.totalGst * 100) / 100,
        itemCount: data.itemCount,
        invoiceCount: data.invoiceSet.size
      };
    }).sort((a, b) => a.taxRate - b.taxRate);

    const totalCgst = Math.round((grandTotalGst / 2) * 100) / 100;
    const totalSgst = Math.round((grandTotalGst / 2) * 100) / 100;

    return {
      slabs,
      totalTaxableAmount: Math.round(totalTaxableAmount * 100) / 100,
      totalCgst,
      totalSgst,
      grandTotalGst: Math.round(grandTotalGst * 100) / 100
    };
  }

  /**
   * Payment method breakdown (Cash, UPI, Card)
   */
  getPaymentMethodAnalysis(orders: PosOrder[]): PaymentMethodAnalysisItem[] {
    const methods: Array<{ method: 'CASH' | 'UPI' | 'CARD'; label: string; iconName: string }> = [
      { method: 'CASH', label: 'Cash Tender', iconName: 'cash' },
      { method: 'UPI', label: 'UPI / QR Code', iconName: 'upi' },
      { method: 'CARD', label: 'Debit / Credit Card', iconName: 'card' }
    ];

    const stats = new Map<'CASH' | 'UPI' | 'CARD', { total: number; count: number }>();
    methods.forEach(m => stats.set(m.method, { total: 0, count: 0 }));

    let totalRevenue = 0;

    orders.forEach(o => {
      const m = o.paymentMethod || 'CASH';
      if (stats.has(m)) {
        const item = stats.get(m)!;
        item.total += o.grandTotal;
        item.count += 1;
        totalRevenue += o.grandTotal;
      }
    });

    return methods.map(m => {
      const rec = stats.get(m.method)!;
      const sharePercent = totalRevenue > 0
        ? Math.round((rec.total / totalRevenue) * 1000) / 10
        : 0;
      const averageTicketSize = rec.count > 0
        ? Math.round(rec.total / rec.count)
        : 0;

      return {
        method: m.method,
        label: m.label,
        totalAmount: Math.round(rec.total * 100) / 100,
        transactionCount: rec.count,
        sharePercent,
        averageTicketSize,
        iconName: m.iconName
      };
    });
  }

  /**
   * Export array of data rows to CSV file with UTF-8 BOM
   */
  exportToCsv(fileName: string, headers: string[], rows: (string | number)[][]): void {
    const escapeCell = (cell: string | number): string => {
      if (cell === null || cell === undefined) return '""';
      const str = String(cell);
      if (str.includes(',') || str.includes('"') || str.includes('\n')) {
        return `"${str.replace(/"/g, '""')}"`;
      }
      return `"${str}"`;
    };

    const headerLine = headers.map(escapeCell).join(',');
    const rowLines = rows.map(r => r.map(escapeCell).join(','));
    const csvContent = '\ufeff' + [headerLine, ...rowLines].join('\r\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', fileName.endsWith('.csv') ? fileName : `${fileName}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  // --- Helper Methods ---

  private formatDate(d: Date): string {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  private getGroupKey(dateStr: string, period: AggregationPeriod): { key: string; label: string } {
    if (!dateStr) {
      return { key: 'unknown', label: 'Unknown' };
    }

    if (period === 'daily') {
      const parts = dateStr.split('-');
      const d = new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
      const options: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric', weekday: 'short' };
      const formatted = d.toLocaleDateString('en-IN', options);
      return { key: dateStr, label: `${dateStr} (${formatted})` };
    }

    if (period === 'weekly') {
      const parts = dateStr.split('-');
      const d = new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
      // Calculate week number
      const firstDayOfYear = new Date(d.getFullYear(), 0, 1);
      const pastDaysOfYear = (d.getTime() - firstDayOfYear.getTime()) / 86400000;
      const weekNum = Math.ceil((pastDaysOfYear + firstDayOfYear.getDay() + 1) / 7);
      return {
        key: `${parts[0]}-W${String(weekNum).padStart(2, '0')}`,
        label: `Week ${weekNum}, ${parts[0]}`
      };
    }

    if (period === 'monthly') {
      const parts = dateStr.split('-');
      const d = new Date(Number(parts[0]), Number(parts[1]) - 1, 1);
      const monthName = d.toLocaleDateString('en-IN', { month: 'long', year: 'numeric' });
      return {
        key: `${parts[0]}-${parts[1]}`,
        label: monthName
      };
    }

    return { key: dateStr, label: dateStr };
  }
}
