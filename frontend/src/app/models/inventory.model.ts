export type MovementType = 'STOCK_IN' | 'STOCK_OUT' | 'ADJUSTMENT';

export interface StockMovement {
  id: number;
  productId: number;
  productName: string;
  sku: string;
  type: MovementType;
  quantity: number;          // Positive for additions, negative for deductions
  balanceAfter: number;      // Resulting stock quantity after operation
  reason: string;            // e.g. "Vendor Restock", "Physical Audit Count", "Damaged Pack"
  referenceNo?: string;      // e.g. "PO-2026-089", "AUD-NOV-01"
  timestamp: string;         // e.g. "20-Nov-2026 09:30 AM"
  performedBy: string;       // e.g. "Sapna (Store Manager)"
}

export interface InventoryMetrics {
  totalItems: number;
  totalUnits: number;
  lowStockCount: number;
  outOfStockCount: number;
  totalStockValue: number;
  potentialRetailValue: number;
}
