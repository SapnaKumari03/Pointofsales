import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { DashboardService } from '../../services/dashboard.service';
import { DashboardData, SalesChartPoint, LowStockItem, RecentTransaction } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  data: DashboardData | null = null;
  activeChartPeriod: 'weekly' | 'monthly' = 'weekly';
  maxSalesAmount = 100000;

  constructor(
    private dashboardService: DashboardService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.dashboardService.getDashboardData().subscribe((res) => {
      this.data = res;
      this.updateMaxAmount();
    });
  }

  get currentChartPoints(): SalesChartPoint[] {
    if (!this.data) return [];
    return this.activeChartPeriod === 'weekly' ? this.data.weeklySales : this.data.monthlySales;
  }

  setPeriod(period: 'weekly' | 'monthly'): void {
    this.activeChartPeriod = period;
    this.updateMaxAmount();
  }

  private updateMaxAmount(): void {
    const points = this.currentChartPoints;
    if (points && points.length > 0) {
      this.maxSalesAmount = Math.max(...points.map((p) => p.sales)) * 1.15;
    }
  }

  getBarHeightPercentage(amount: number): number {
    return Math.max(12, Math.round((amount / this.maxSalesAmount) * 100));
  }

  onNewSale(): void {
    this.router.navigate(['/pos-billing']);
  }

  onRestock(item: LowStockItem): void {
    alert(`Initiating Purchase Order / Restock for: ${item.name}\nSKU: ${item.sku}\nCurrent Stock: ${item.currentStock} ${item.unit}\nSafety Level: ${item.minStockLevel}`);
  }

  onPrintReceipt(tx: RecentTransaction): void {
    alert(`[Thermal Receipt 80mm Preview]\n=================================\nJ20 RETAIL POS - STORE #102\nInvoice: ${tx.invoiceNo}\nDate/Time: Today, ${tx.timestamp}\nCustomer: ${tx.customerName}\nCashier: ${tx.cashierName}\nPayment: ${tx.paymentMethod}\n---------------------------------\nTotal Amount: ₹${tx.totalAmount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}\nStatus: ${tx.status}\n=================================`);
  }
}
