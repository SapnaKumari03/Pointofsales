import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { map, switchMap, take } from 'rxjs/operators';
import { ProductService } from './product.service';
import { Product } from '../models/product.model';
import { StockMovement, InventoryMetrics, MovementType } from '../models/inventory.model';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  // Historical stock movement audit log
  private initialMovements: StockMovement[] = [
    {
      id: 1,
      productId: 1,
      productName: 'Aashirvaad Shudh Chakki Atta 5kg',
      sku: 'GRO-ATT-001',
      type: 'STOCK_IN',
      quantity: 20,
      balanceAfter: 20,
      reason: 'Vendor Delivery Received',
      referenceNo: 'PO-2026-0881',
      timestamp: '2026-11-15 10:15 AM',
      performedBy: 'Sapna (Store Manager)'
    },
    {
      id: 2,
      productId: 1,
      productName: 'Aashirvaad Shudh Chakki Atta 5kg',
      sku: 'GRO-ATT-001',
      type: 'STOCK_OUT',
      quantity: -16,
      balanceAfter: 4,
      reason: 'POS Counter Sales Outflow',
      referenceNo: 'BATCH-SALES-101',
      timestamp: '2026-11-19 08:30 PM',
      performedBy: 'POS Terminal 01'
    },
    {
      id: 3,
      productId: 2,
      productName: 'Fortune Sunlite Refined Sunflower Oil 1L',
      sku: 'OIL-SUN-002',
      type: 'STOCK_IN',
      quantity: 25,
      balanceAfter: 25,
      reason: 'Distributor Intake Batch',
      referenceNo: 'PO-2026-0895',
      timestamp: '2026-11-16 11:30 AM',
      performedBy: 'Sapna (Store Manager)'
    },
    {
      id: 4,
      productId: 2,
      productName: 'Fortune Sunlite Refined Sunflower Oil 1L',
      sku: 'OIL-SUN-002',
      type: 'STOCK_OUT',
      quantity: -19,
      balanceAfter: 6,
      reason: 'POS Counter Sales Outflow',
      referenceNo: 'BATCH-SALES-102',
      timestamp: '2026-11-19 08:30 PM',
      performedBy: 'POS Terminal 01'
    },
    {
      id: 5,
      productId: 3,
      productName: 'Amul Butter Pasteurised 500g',
      sku: 'DAI-BUT-003',
      type: 'STOCK_OUT',
      quantity: -1,
      balanceAfter: 3,
      reason: 'Cold Chain Meltage / Defective Packaging',
      referenceNo: 'DMG-2026-004',
      timestamp: '2026-11-18 04:45 PM',
      performedBy: 'Pooja S. (Cashier)'
    },
    {
      id: 6,
      productId: 10,
      productName: 'Surf Excel Easy Wash Detergent Powder 1kg',
      sku: 'HH-SUR-010',
      type: 'STOCK_OUT',
      quantity: -15,
      balanceAfter: 0,
      reason: 'Institutional Bulk Order Checkout',
      referenceNo: 'INV-2026-0980',
      timestamp: '2026-11-18 06:10 PM',
      performedBy: 'Ramesh K. (Cashier)'
    },
    {
      id: 7,
      productId: 7,
      productName: 'Maggi 2-Minute Masala Instant Noodles 70g',
      sku: 'SNK-MAG-007',
      type: 'STOCK_IN',
      quantity: 100,
      balanceAfter: 100,
      reason: 'Master Carton Restock',
      referenceNo: 'PO-2026-0912',
      timestamp: '2026-11-17 02:20 PM',
      performedBy: 'Sapna (Store Manager)'
    },
    {
      id: 8,
      productId: 5,
      productName: 'MDH Garam Masala 100g',
      sku: 'MAS-GAR-005',
      type: 'ADJUSTMENT',
      quantity: -1,
      balanceAfter: 5,
      reason: 'Physical Audit Discrepancy Correction',
      referenceNo: 'AUD-NOV-W3',
      timestamp: '2026-11-19 09:00 PM',
      performedBy: 'Sapna (Store Manager)'
    }
  ];

  private movementsSubject = new BehaviorSubject<StockMovement[]>(this.initialMovements);

  constructor(private productService: ProductService) {}

  getMovements(): Observable<StockMovement[]> {
    return this.movementsSubject.asObservable();
  }

  getMetrics(): Observable<InventoryMetrics> {
    return this.productService.getProducts().pipe(
      map((products: Product[]) => {
        let totalUnits = 0;
        let lowStockCount = 0;
        let outOfStockCount = 0;
        let totalStockValue = 0;
        let potentialRetailValue = 0;

        products.forEach((p) => {
          totalUnits += p.stockQuantity;
          totalStockValue += p.stockQuantity * p.costPrice;
          potentialRetailValue += p.stockQuantity * p.sellingPrice;

          if (p.stockQuantity <= 0) {
            outOfStockCount++;
          } else if (p.stockQuantity <= p.minStockLevel) {
            lowStockCount++;
          }
        });

        return {
          totalItems: products.length,
          totalUnits,
          lowStockCount,
          outOfStockCount,
          totalStockValue,
          potentialRetailValue
        };
      })
    );
  }

  stockIn(productId: number, quantityToAdd: number, referenceNo: string, notes?: string): Observable<boolean> {
    if (quantityToAdd <= 0) {
      throw new Error('Stock in quantity must be greater than zero.');
    }

    return this.productService.getProductById(productId).pipe(
      take(1),
      switchMap((product) => {
        if (!product) throw new Error('Product not found.');

        const newBalance = product.stockQuantity + quantityToAdd;
        return this.productService.updateProduct(productId, { stockQuantity: newBalance }).pipe(
          map(() => {
            this.recordMovement({
              productId,
              productName: product.name,
              sku: product.sku,
              type: 'STOCK_IN',
              quantity: quantityToAdd,
              balanceAfter: newBalance,
              reason: notes || 'Stock Intake / Vendor Inflow',
              referenceNo: referenceNo || 'STOCK-IN',
              performedBy: 'Sapna (Store Manager)'
            });
            return true;
          })
        );
      })
    );
  }

  stockOut(productId: number, quantityToDeduct: number, reason: string, referenceNo?: string): Observable<boolean> {
    if (quantityToDeduct <= 0) {
      throw new Error('Stock out quantity must be greater than zero.');
    }

    return this.productService.getProductById(productId).pipe(
      take(1),
      switchMap((product) => {
        if (!product) throw new Error('Product not found.');

        if (product.stockQuantity < quantityToDeduct) {
          throw new Error(`Insufficient stock. Available: ${product.stockQuantity}, Requested to remove: ${quantityToDeduct}`);
        }

        const newBalance = product.stockQuantity - quantityToDeduct;
        return this.productService.updateProduct(productId, { stockQuantity: newBalance }).pipe(
          map(() => {
            this.recordMovement({
              productId,
              productName: product.name,
              sku: product.sku,
              type: 'STOCK_OUT',
              quantity: -quantityToDeduct,
              balanceAfter: newBalance,
              reason: reason || 'Manual Stock Outflow',
              referenceNo: referenceNo || 'STOCK-OUT',
              performedBy: 'Sapna (Store Manager)'
            });
            return true;
          })
        );
      })
    );
  }

  adjustStock(productId: number, actualCountedQuantity: number, reason: string, referenceNo?: string): Observable<boolean> {
    if (actualCountedQuantity < 0) {
      throw new Error('Stock quantity cannot be negative.');
    }

    return this.productService.getProductById(productId).pipe(
      take(1),
      switchMap((product) => {
        if (!product) throw new Error('Product not found.');

        const delta = actualCountedQuantity - product.stockQuantity;
        return this.productService.updateProduct(productId, { stockQuantity: actualCountedQuantity }).pipe(
          map(() => {
            this.recordMovement({
              productId,
              productName: product.name,
              sku: product.sku,
              type: 'ADJUSTMENT',
              quantity: delta,
              balanceAfter: actualCountedQuantity,
              reason: reason || 'Physical Audit Reconciliation',
              referenceNo: referenceNo || 'AUDIT-ADJ',
              performedBy: 'Sapna (Store Manager)'
            });
            return true;
          })
        );
      })
    );
  }

  private recordMovement(entry: Omit<StockMovement, 'id' | 'timestamp'>): void {
    const currentList = this.movementsSubject.value;
    const nextId = currentList.length > 0 ? Math.max(...currentList.map((m) => m.id)) + 1 : 1;
    const now = new Date();
    const formattedDate = `${now.toISOString().split('T')[0]} ${now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;

    const newMovement: StockMovement = {
      ...entry,
      id: nextId,
      timestamp: formattedDate
    };

    this.movementsSubject.next([newMovement, ...currentList]);
  }
}
