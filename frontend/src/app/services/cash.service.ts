import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { PosService } from './pos.service';
import { CashShift, CashPaidOutEntry, PaymentSummary } from '../models/cash-shift.model';
import { PosOrder } from '../models/pos.model';

@Injectable({
  providedIn: 'root'
})
export class CashService {
  // Current active morning shift
  private activeShift: CashShift = {
    id: 14,
    shiftCode: 'SHIFT-2026-014',
    cashierName: 'Sapna (Store Manager)',
    openedAt: '2026-11-20 08:00 AM',
    openingFloat: 5000,
    cashSales: 26250,
    cashPaidOut: 800,
    expectedCash: 30450, // 5000 + 26250 - 800
    status: 'ACTIVE',
    notes: 'Morning Register Shift'
  };

  private activeShiftSubject = new BehaviorSubject<CashShift | null>(this.activeShift);

  // Historical closed shifts
  private shiftHistory: CashShift[] = [
    {
      id: 13,
      shiftCode: 'SHIFT-2026-013',
      cashierName: 'Ramesh K. (Cashier)',
      openedAt: '2026-11-19 02:00 PM',
      closedAt: '2026-11-19 10:00 PM',
      openingFloat: 5000,
      cashSales: 31400,
      cashPaidOut: 450,
      expectedCash: 35950,
      actualCash: 35950,
      variance: 0,
      status: 'CLOSED',
      notes: 'Evening Shift Balanced'
    },
    {
      id: 12,
      shiftCode: 'SHIFT-2026-012',
      cashierName: 'Pooja S. (Cashier)',
      openedAt: '2026-11-19 08:00 AM',
      closedAt: '2026-11-19 02:00 PM',
      openingFloat: 5000,
      cashSales: 22800,
      cashPaidOut: 0,
      expectedCash: 27800,
      actualCash: 27850,
      variance: 50,
      status: 'CLOSED',
      notes: 'Customer left ₹50 tip/change'
    },
    {
      id: 11,
      shiftCode: 'SHIFT-2026-011',
      cashierName: 'Sapna (Store Manager)',
      openedAt: '2026-11-18 08:00 AM',
      closedAt: '2026-11-18 08:00 PM',
      openingFloat: 5000,
      cashSales: 41200,
      cashPaidOut: 1200,
      expectedCash: 45000,
      actualCash: 44980,
      variance: -20,
      status: 'CLOSED',
      notes: 'Slight coin rounding discrepancy'
    }
  ];

  private shiftHistorySubject = new BehaviorSubject<CashShift[]>(this.shiftHistory);

  // Paid Out logs (petty cash expenses)
  private paidOutLogs: CashPaidOutEntry[] = [
    {
      id: 1,
      shiftCode: 'SHIFT-2026-014',
      amount: 500,
      reason: 'Dairy Supplier Local Ice Supply & Delivery',
      paidTo: 'Bangalore Dairy Logistics',
      timestamp: '2026-11-20 09:15 AM',
      approvedBy: 'Sapna (Store Manager)'
    },
    {
      id: 2,
      shiftCode: 'SHIFT-2026-014',
      amount: 300,
      reason: 'Store Cleaning & Sanitizer Supplies',
      paidTo: 'Sri Balaji Stores',
      timestamp: '2026-11-20 10:30 AM',
      approvedBy: 'Sapna (Store Manager)'
    }
  ];

  private paidOutSubject = new BehaviorSubject<CashPaidOutEntry[]>(this.paidOutLogs);

  constructor(private posService: PosService) {
    // Listen for newly completed POS orders to dynamically increment cashSales
    this.posService.getCompletedOrders().subscribe((orders: PosOrder[]) => {
      this.recalculateActiveShiftFromOrders(orders);
    });
  }

  getActiveShift(): Observable<CashShift | null> {
    return this.activeShiftSubject.asObservable();
  }

  getShiftHistory(): Observable<CashShift[]> {
    return this.shiftHistorySubject.asObservable();
  }

  getPaidOutLogs(): Observable<CashPaidOutEntry[]> {
    return this.paidOutSubject.asObservable();
  }

  getPaymentSummary(): Observable<PaymentSummary> {
    return this.posService.getCompletedOrders().pipe(
      map((orders) => {
        let cashTotal = 0;
        let cashTxCount = 0;
        let upiTotal = 0;
        let upiTxCount = 0;
        let cardTotal = 0;
        let cardTxCount = 0;

        orders.forEach((o) => {
          if (o.paymentMethod === 'CASH') {
            cashTotal += o.grandTotal;
            cashTxCount++;
          } else if (o.paymentMethod === 'UPI') {
            upiTotal += o.grandTotal;
            upiTxCount++;
          } else if (o.paymentMethod === 'CARD') {
            cardTotal += o.grandTotal;
            cardTxCount++;
          }
        });

        return {
          totalSales: cashTotal + upiTotal + cardTotal,
          totalOrders: orders.length,
          cashTotal,
          cashTxCount,
          upiTotal,
          upiTxCount,
          cardTotal,
          cardTxCount
        };
      })
    );
  }

  openShift(openingFloat: number, notes?: string): Observable<CashShift> {
    const nextId = this.shiftHistory.length + 15;
    const shiftCode = `SHIFT-2026-${String(nextId).padStart(3, '0')}`;
    const now = new Date();
    const openedAt = `${now.toISOString().split('T')[0]} ${now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;

    const newShift: CashShift = {
      id: nextId,
      shiftCode,
      cashierName: 'Sapna (Store Manager)',
      openedAt,
      openingFloat,
      cashSales: 0,
      cashPaidOut: 0,
      expectedCash: openingFloat,
      status: 'ACTIVE',
      notes: notes || 'New Cash Register Shift'
    };

    this.activeShiftSubject.next(newShift);
    return of(newShift);
  }

  recordCashPaidOut(amount: number, reason: string, paidTo: string): Observable<CashPaidOutEntry> {
    const active = this.activeShiftSubject.value;
    if (!active) throw new Error('No active shift found');

    const nextId = this.paidOutLogs.length + 1;
    const now = new Date();
    const timestamp = `${now.toISOString().split('T')[0]} ${now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;

    const entry: CashPaidOutEntry = {
      id: nextId,
      shiftCode: active.shiftCode,
      amount,
      reason,
      paidTo,
      timestamp,
      approvedBy: 'Sapna (Store Manager)'
    };

    this.paidOutLogs = [entry, ...this.paidOutLogs];
    this.paidOutSubject.next(this.paidOutLogs);

    // Update active shift paidOut
    active.cashPaidOut += amount;
    active.expectedCash = active.openingFloat + active.cashSales - active.cashPaidOut;
    this.activeShiftSubject.next({ ...active });

    return of(entry);
  }

  closeShift(actualCountedCash: number, notes?: string): Observable<CashShift> {
    const active = this.activeShiftSubject.value;
    if (!active) throw new Error('No active shift found');

    const now = new Date();
    const closedAt = `${now.toISOString().split('T')[0]} ${now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
    const variance = actualCountedCash - active.expectedCash;

    const closedShift: CashShift = {
      ...active,
      closedAt,
      actualCash: actualCountedCash,
      variance,
      status: 'CLOSED',
      notes: notes || active.notes
    };

    // Prepend to history
    this.shiftHistory = [closedShift, ...this.shiftHistory];
    this.shiftHistorySubject.next(this.shiftHistory);

    // Reset active shift
    this.activeShiftSubject.next(null);

    return of(closedShift);
  }

  private recalculateActiveShiftFromOrders(orders: PosOrder[]): void {
    const active = this.activeShiftSubject.value;
    if (!active) return;

    // Recalculate cash sales
    const currentCashSales = orders
      .filter((o) => o.paymentMethod === 'CASH')
      .reduce((sum, o) => sum + o.grandTotal, 0);

    active.cashSales = currentCashSales;
    active.expectedCash = active.openingFloat + active.cashSales - active.cashPaidOut;
    this.activeShiftSubject.next({ ...active });
  }
}
