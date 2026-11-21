export interface CashShift {
  id: number;
  shiftCode: string;
  cashierName: string;
  openedAt: string;
  closedAt?: string;
  openingFloat: number;
  cashSales: number;
  cashPaidOut: number;
  expectedCash: number;
  actualCash?: number;
  variance?: number; // actualCash - expectedCash
  status: 'ACTIVE' | 'CLOSED';
  notes?: string;
}

export interface CashPaidOutEntry {
  id: number;
  shiftCode: string;
  amount: number;
  reason: string;
  paidTo: string;
  timestamp: string;
  approvedBy: string;
}

export interface PaymentSummary {
  totalSales: number;
  totalOrders: number;
  cashTotal: number;
  cashTxCount: number;
  upiTotal: number;
  upiTxCount: number;
  cardTotal: number;
  cardTxCount: number;
}
