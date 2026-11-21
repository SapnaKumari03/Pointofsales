import { Component, OnInit } from '@angular/core';
import { PosService } from '../../services/pos.service';
import { CashService } from '../../services/cash.service';
import { PosOrder } from '../../models/pos.model';
import { PaymentSummary } from '../../models/cash-shift.model';

@Component({
  selector: 'app-sales-history',
  templateUrl: './sales-history.component.html',
  styleUrls: ['./sales-history.component.css']
})
export class SalesHistoryComponent implements OnInit {
  orders: PosOrder[] = [];
  paymentSummary: PaymentSummary = {
    totalSales: 0,
    totalOrders: 0,
    cashTotal: 0,
    cashTxCount: 0,
    upiTotal: 0,
    upiTxCount: 0,
    cardTotal: 0,
    cardTxCount: 0
  };

  // Search & Filter
  searchTerm = '';
  selectedPaymentFilter = 'ALL';
  selectedDateFilter = 'ALL';

  // Details & Receipt Modal
  selectedOrder: PosOrder | null = null;
  showDetailsModal = false;

  constructor(
    private posService: PosService,
    private cashService: CashService
  ) {}

  ngOnInit(): void {
    this.loadSales();
  }

  loadSales(): void {
    this.posService.getCompletedOrders().subscribe((orders) => {
      this.orders = orders;
    });

    this.cashService.getPaymentSummary().subscribe((summary) => {
      this.paymentSummary = summary;
    });
  }

  get filteredOrders(): PosOrder[] {
    const today = new Date().toISOString().split('T')[0];

    return this.orders.filter((order) => {
      // 1. Search term (Invoice, Customer, Phone, Cashier)
      const term = this.searchTerm.trim().toLowerCase();
      const matchesSearch =
        !term ||
        order.invoiceNumber.toLowerCase().includes(term) ||
        order.customerName.toLowerCase().includes(term) ||
        (order.customerPhone && order.customerPhone.includes(term)) ||
        order.cashierName.toLowerCase().includes(term);

      // 2. Payment method filter
      const matchesPayment =
        this.selectedPaymentFilter === 'ALL' || order.paymentMethod === this.selectedPaymentFilter;

      // 3. Date range filter
      let matchesDate = true;
      if (this.selectedDateFilter === 'TODAY') {
        matchesDate = order.orderDate === today;
      }

      return matchesSearch && matchesPayment && matchesDate;
    });
  }

  openOrderDetails(order: PosOrder): void {
    this.selectedOrder = order;
    this.showDetailsModal = true;
  }

  closeDetailsModal(): void {
    this.showDetailsModal = false;
    this.selectedOrder = null;
  }

  printReceipt(): void {
    window.print();
  }
}
