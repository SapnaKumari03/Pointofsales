import { Component, OnInit } from '@angular/core';
import { CashService } from '../../services/cash.service';
import { CashShift, CashPaidOutEntry, PaymentSummary } from '../../models/cash-shift.model';

@Component({
  selector: 'app-cash-management',
  templateUrl: './cash-management.component.html',
  styleUrls: ['./cash-management.component.css']
})
export class CashManagementComponent implements OnInit {
  activeTab: 'shift' | 'history' | 'payouts' = 'shift';

  activeShift: CashShift | null = null;
  shiftHistory: CashShift[] = [];
  paidOutLogs: CashPaidOutEntry[] = [];
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

  // Modal states
  showOpenShiftModal = false;
  showCloseShiftModal = false;
  showPayoutModal = false;

  // Open Shift Form
  newOpeningFloat = 5000;
  openShiftNotes = 'Morning Register Shift';

  // Payout Form
  payoutAmount = 250;
  payoutReason = 'Cleaning & Housekeeping Supplies';
  payoutRecipient = 'Sri Balaji General Stores';

  payoutReasons = [
    'Dairy / Milk Vendor Payment',
    'Cleaning & Housekeeping Supplies',
    'Cash Register Paper Rolls & Stationary',
    'Emergency Petty Cash Expense',
    'Cash Refund / Cashier Payout',
    'Other Payout'
  ];

  // Close Shift & Reconciliation Form
  actualCountedCash = 0;
  closeShiftNotes = '';

  constructor(private cashService: CashService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.cashService.getActiveShift().subscribe((shift) => {
      this.activeShift = shift;
      if (shift) {
        this.actualCountedCash = shift.expectedCash;
      }
    });

    this.cashService.getShiftHistory().subscribe((history) => {
      this.shiftHistory = history;
    });

    this.cashService.getPaidOutLogs().subscribe((logs) => {
      this.paidOutLogs = logs;
    });

    this.cashService.getPaymentSummary().subscribe((summary) => {
      this.paymentSummary = summary;
    });
  }

  // Reconciliation Variance Calculation
  get reconciliationVariance(): number {
    if (!this.activeShift) return 0;
    return Math.round((this.actualCountedCash - this.activeShift.expectedCash) * 100) / 100;
  }

  get varianceStatus(): 'BALANCED' | 'SURPLUS' | 'SHORTAGE' {
    const v = this.reconciliationVariance;
    if (v === 0) return 'BALANCED';
    if (v > 0) return 'SURPLUS';
    return 'SHORTAGE';
  }

  // Open Shift Flow
  openNewShiftDialog(): void {
    this.newOpeningFloat = 5000;
    this.openShiftNotes = 'Morning Register Shift';
    this.showOpenShiftModal = true;
  }

  submitOpenShift(): void {
    if (this.newOpeningFloat < 0) {
      alert('Opening float cannot be negative.');
      return;
    }
    this.cashService.openShift(this.newOpeningFloat, this.openShiftNotes).subscribe(() => {
      this.showOpenShiftModal = false;
    });
  }

  // Payout Flow
  openPayoutDialog(): void {
    this.payoutAmount = 250;
    this.payoutReason = this.payoutReasons[0];
    this.payoutRecipient = '';
    this.showPayoutModal = true;
  }

  submitPayout(): void {
    if (this.payoutAmount <= 0) {
      alert('Payout amount must be greater than zero.');
      return;
    }
    if (!this.payoutRecipient.trim()) {
      alert('Please enter recipient name.');
      return;
    }

    this.cashService.recordCashPaidOut(this.payoutAmount, this.payoutReason, this.payoutRecipient).subscribe(() => {
      this.showPayoutModal = false;
    });
  }

  // Close Shift & Reconcile Flow
  openCloseShiftDialog(): void {
    if (!this.activeShift) return;
    this.actualCountedCash = this.activeShift.expectedCash;
    this.closeShiftNotes = '';
    this.showCloseShiftModal = true;
  }

  submitCloseShift(): void {
    if (this.actualCountedCash < 0) {
      alert('Counted cash cannot be negative.');
      return;
    }

    if (this.reconciliationVariance !== 0 && !this.closeShiftNotes.trim()) {
      if (!confirm(`There is a cash variance of ₹${this.reconciliationVariance}. Do you want to proceed with shift closing?`)) {
        return;
      }
    }

    this.cashService.closeShift(this.actualCountedCash, this.closeShiftNotes).subscribe(() => {
      this.showCloseShiftModal = false;
      this.activeTab = 'history';
    });
  }

  closeModals(): void {
    this.showOpenShiftModal = false;
    this.showCloseShiftModal = false;
    this.showPayoutModal = false;
  }
}
