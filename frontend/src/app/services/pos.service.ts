import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { ProductService } from './product.service';
import { InventoryService } from './inventory.service';
import { PosOrder, CartItem } from '../models/pos.model';

function getPastDateStr(daysAgo: number): string {
  const d = new Date();
  d.setDate(d.getDate() - daysAgo);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

@Injectable({
  providedIn: 'root'
})
export class PosService {
  private invoiceCounter = 1049;

  // Initial seed orders matching realistic Indian retail checkouts
  private initialOrders: PosOrder[] = [
    {
      invoiceNumber: 'INV-2026-1048',
      orderDate: getPastDateStr(0),
      orderTime: '12:42 PM',
      items: [
        {
          product: {
            id: 1,
            name: 'Aashirvaad Shudh Chakki Atta 5kg',
            sku: 'GRO-ATT-001',
            barcode: '8901030383701',
            category: 'Flour & Grains',
            costPrice: 210,
            sellingPrice: 245,
            taxRate: 0,
            stockQuantity: 4,
            minStockLevel: 15,
            unit: 'Bag',
            status: 'ACTIVE',
            createdAt: '2026-11-01'
          },
          quantity: 2,
          unitPrice: 245,
          taxRate: 0,
          taxAmount: 0,
          subtotal: 490,
          total: 490
        },
        {
          product: {
            id: 2,
            name: 'Fortune Sunlite Refined Sunflower Oil 1L',
            sku: 'OIL-SUN-002',
            barcode: '8906007281023',
            category: 'Edible Oils',
            costPrice: 132,
            sellingPrice: 155,
            taxRate: 5,
            stockQuantity: 6,
            minStockLevel: 20,
            unit: 'Pouch',
            status: 'ACTIVE',
            createdAt: '2026-11-02'
          },
          quantity: 2,
          unitPrice: 155,
          taxRate: 5,
          taxAmount: 14.76,
          subtotal: 295.24,
          total: 310
        },
        {
          product: {
            id: 4,
            name: 'India Gate Feast Rozzana Basmati 5kg',
            sku: 'RIC-BAS-004',
            barcode: '8901384010321',
            category: 'Rice & Cereals',
            costPrice: 415,
            sellingPrice: 490,
            taxRate: 0,
            stockQuantity: 2,
            minStockLevel: 10,
            unit: 'Bag',
            status: 'ACTIVE',
            createdAt: '2026-11-04'
          },
          quantity: 1,
          unitPrice: 490,
          taxRate: 0,
          taxAmount: 0,
          subtotal: 490,
          total: 490
        },
        {
          product: {
            id: 3,
            name: 'Amul Butter Pasteurised 500g',
            sku: 'DAI-BUT-003',
            barcode: '8901262010053',
            category: 'Dairy',
            costPrice: 250,
            sellingPrice: 285,
            taxRate: 12,
            stockQuantity: 3,
            minStockLevel: 12,
            unit: 'Pack',
            status: 'ACTIVE',
            createdAt: '2026-11-03'
          },
          quantity: 1,
          unitPrice: 285,
          taxRate: 12,
          taxAmount: 30.54,
          subtotal: 254.46,
          total: 285
        },
        {
          product: {
            id: 11,
            name: 'Brooke Bond Red Label Tea 500g',
            sku: 'BEV-TEA-011',
            barcode: '8901030712301',
            category: 'Beverages',
            costPrice: 260,
            sellingPrice: 295,
            taxRate: 5,
            stockQuantity: 24,
            minStockLevel: 12,
            unit: 'Pack',
            status: 'ACTIVE',
            createdAt: '2026-11-11'
          },
          quantity: 1,
          unitPrice: 295,
          taxRate: 5,
          taxAmount: 14.05,
          subtotal: 280.95,
          total: 295
        }
      ],
      subtotal: 1810.65,
      taxAmount: 59.35,
      discountAmount: 20,
      grandTotal: 1850,
      paymentMethod: 'UPI',
      amountPaid: 1850,
      changeDue: 0,
      customerName: 'Ananya Rao',
      customerPhone: '9845012345',
      cashierName: 'Sapna (Store Manager)',
      terminalId: 'POS-01'
    },
    {
      invoiceNumber: 'INV-2026-1047',
      orderDate: getPastDateStr(0),
      orderTime: '12:35 PM',
      items: [
        {
          product: {
            id: 3,
            name: 'Amul Butter Pasteurised 500g',
            sku: 'DAI-BUT-003',
            barcode: '8901262010053',
            category: 'Dairy',
            costPrice: 250,
            sellingPrice: 285,
            taxRate: 12,
            stockQuantity: 3,
            minStockLevel: 12,
            unit: 'Pack',
            status: 'ACTIVE',
            createdAt: '2026-11-03'
          },
          quantity: 1,
          unitPrice: 285,
          taxRate: 12,
          taxAmount: 30.54,
          subtotal: 254.46,
          total: 285
        },
        {
          product: {
            id: 10,
            name: 'Surf Excel Easy Wash Detergent Powder 1kg',
            sku: 'HH-SUR-010',
            barcode: '8901030012403',
            category: 'Household Supplies',
            costPrice: 125,
            sellingPrice: 145,
            taxRate: 18,
            stockQuantity: 0,
            minStockLevel: 15,
            unit: 'Pack',
            status: 'ACTIVE',
            createdAt: '2026-11-10'
          },
          quantity: 1,
          unitPrice: 145,
          taxRate: 18,
          taxAmount: 22.12,
          subtotal: 122.88,
          total: 145
        }
      ],
      subtotal: 377.34,
      taxAmount: 52.66,
      discountAmount: 10,
      grandTotal: 420,
      paymentMethod: 'CASH',
      amountPaid: 500,
      changeDue: 80,
      customerName: 'Rajesh Kumar',
      customerPhone: '9900112233',
      cashierName: 'Sapna (Store Manager)',
      terminalId: 'POS-01'
    },
    {
      invoiceNumber: 'INV-2026-1046',
      orderDate: getPastDateStr(1),
      orderTime: '12:20 PM',
      items: [
        {
          product: {
            id: 4,
            name: 'India Gate Feast Rozzana Basmati 5kg',
            sku: 'RIC-BAS-004',
            barcode: '8901384010321',
            category: 'Rice & Cereals',
            costPrice: 415,
            sellingPrice: 490,
            taxRate: 0,
            stockQuantity: 2,
            minStockLevel: 10,
            unit: 'Bag',
            status: 'ACTIVE',
            createdAt: '2026-11-04'
          },
          quantity: 4,
          unitPrice: 490,
          taxRate: 0,
          taxAmount: 0,
          subtotal: 1960,
          total: 1960
        },
        {
          product: {
            id: 2,
            name: 'Fortune Sunlite Refined Sunflower Oil 1L',
            sku: 'OIL-SUN-002',
            barcode: '8906007281023',
            category: 'Edible Oils',
            costPrice: 132,
            sellingPrice: 155,
            taxRate: 5,
            stockQuantity: 6,
            minStockLevel: 20,
            unit: 'Pouch',
            status: 'ACTIVE',
            createdAt: '2026-11-02'
          },
          quantity: 4,
          unitPrice: 155,
          taxRate: 5,
          taxAmount: 29.52,
          subtotal: 590.48,
          total: 620
        },
        {
          product: {
            id: 11,
            name: 'Brooke Bond Red Label Tea 500g',
            sku: 'BEV-TEA-011',
            barcode: '8901030712301',
            category: 'Beverages',
            costPrice: 260,
            sellingPrice: 295,
            taxRate: 5,
            stockQuantity: 24,
            minStockLevel: 12,
            unit: 'Pack',
            status: 'ACTIVE',
            createdAt: '2026-11-11'
          },
          quantity: 2,
          unitPrice: 295,
          taxRate: 5,
          taxAmount: 28.1,
          subtotal: 561.9,
          total: 590
        },
        {
          product: {
            id: 6,
            name: 'Tata Salt Vacuum Evaporated 1kg',
            sku: 'GRO-SLT-006',
            barcode: '8904043901005',
            category: 'Groceries',
            costPrice: 22,
            sellingPrice: 28,
            taxRate: 0,
            stockQuantity: 8,
            minStockLevel: 25,
            unit: 'Pack',
            status: 'ACTIVE',
            createdAt: '2026-11-06'
          },
          quantity: 2,
          unitPrice: 28,
          taxRate: 0,
          taxAmount: 0,
          subtotal: 56,
          total: 56
        },
        {
          product: {
            id: 8,
            name: 'Britannia Good Day Butter Cookies 200g',
            sku: 'SNK-BRI-008',
            barcode: '8901063012107',
            category: 'Biscuits & Bakery',
            costPrice: 38,
            sellingPrice: 45,
            taxRate: 18,
            stockQuantity: 42,
            minStockLevel: 20,
            unit: 'Pack',
            status: 'ACTIVE',
            createdAt: '2026-11-08'
          },
          quantity: 1,
          unitPrice: 45,
          taxRate: 18,
          taxAmount: 6.86,
          subtotal: 38.14,
          total: 45
        }
      ],
      subtotal: 3175.52,
      taxAmount: 64.48,
      discountAmount: 0,
      grandTotal: 3240,
      paymentMethod: 'CARD',
      amountPaid: 3240,
      changeDue: 0,
      customerName: 'Walk-in Customer',
      cashierName: 'Sapna (Store Manager)',
      terminalId: 'POS-01'
    },
    {
      invoiceNumber: 'INV-2026-1045',
      orderDate: getPastDateStr(2),
      orderTime: '12:08 PM',
      items: [
        {
          product: {
            id: 2,
            name: 'Fortune Sunlite Refined Sunflower Oil 1L',
            sku: 'OIL-SUN-002',
            barcode: '8906007281023',
            category: 'Edible Oils',
            costPrice: 132,
            sellingPrice: 155,
            taxRate: 5,
            stockQuantity: 6,
            minStockLevel: 20,
            unit: 'Pouch',
            status: 'ACTIVE',
            createdAt: '2026-11-02'
          },
          quantity: 1,
          unitPrice: 155,
          taxRate: 5,
          taxAmount: 7.38,
          subtotal: 147.62,
          total: 155
        }
      ],
      subtotal: 147.62,
      taxAmount: 7.38,
      discountAmount: 0,
      grandTotal: 155,
      paymentMethod: 'UPI',
      amountPaid: 155,
      changeDue: 0,
      customerName: 'Priya Sharma',
      cashierName: 'Sapna (Store Manager)',
      terminalId: 'POS-01'
    },
    {
      invoiceNumber: 'INV-2026-1044',
      orderDate: getPastDateStr(3),
      orderTime: '11:55 AM',
      items: [
        {
          product: {
            id: 1,
            name: 'Aashirvaad Shudh Chakki Atta 5kg',
            sku: 'GRO-ATT-001',
            barcode: '8901030383701',
            category: 'Flour & Grains',
            costPrice: 210,
            sellingPrice: 245,
            taxRate: 0,
            stockQuantity: 4,
            minStockLevel: 15,
            unit: 'Bag',
            status: 'ACTIVE',
            createdAt: '2026-11-01'
          },
          quantity: 4,
          unitPrice: 245,
          taxRate: 0,
          taxAmount: 0,
          subtotal: 980,
          total: 980
        }
      ],
      subtotal: 980,
      taxAmount: 0,
      discountAmount: 0,
      grandTotal: 980,
      paymentMethod: 'CASH',
      amountPaid: 1000,
      changeDue: 20,
      customerName: 'Walk-in Customer',
      cashierName: 'Sapna (Store Manager)',
      terminalId: 'POS-01'
    },
    {
      invoiceNumber: 'INV-2026-1043',
      orderDate: getPastDateStr(4),
      orderTime: '03:15 PM',
      items: [
        {
          product: {
            id: 5,
            name: 'MDH Garam Masala 100g',
            sku: 'MAS-GAR-005',
            barcode: '8902167000214',
            category: 'Spices & Masalas',
            costPrice: 72,
            sellingPrice: 88,
            taxRate: 5,
            stockQuantity: 5,
            minStockLevel: 15,
            unit: 'Box',
            status: 'ACTIVE',
            createdAt: '2026-11-05'
          },
          quantity: 3,
          unitPrice: 88,
          taxRate: 5,
          taxAmount: 12.57,
          subtotal: 251.43,
          total: 264
        },
        {
          product: {
            id: 11,
            name: 'Brooke Bond Red Label Tea 500g',
            sku: 'BEV-TEA-011',
            barcode: '8901030712301',
            category: 'Beverages',
            costPrice: 260,
            sellingPrice: 295,
            taxRate: 5,
            stockQuantity: 24,
            minStockLevel: 12,
            unit: 'Pack',
            status: 'ACTIVE',
            createdAt: '2026-11-11'
          },
          quantity: 2,
          unitPrice: 295,
          taxRate: 5,
          taxAmount: 28.1,
          subtotal: 561.9,
          total: 590
        }
      ],
      subtotal: 813.33,
      taxAmount: 40.67,
      discountAmount: 14,
      grandTotal: 840,
      paymentMethod: 'UPI',
      amountPaid: 840,
      changeDue: 0,
      customerName: 'Kavita Reddy',
      cashierName: 'Sapna (Store Manager)',
      terminalId: 'POS-01'
    },
    {
      invoiceNumber: 'INV-2026-1042',
      orderDate: getPastDateStr(5),
      orderTime: '05:40 PM',
      items: [
        {
          product: {
            id: 4,
            name: 'India Gate Feast Rozzana Basmati 5kg',
            sku: 'RIC-BAS-004',
            barcode: '8901384010321',
            category: 'Rice & Cereals',
            costPrice: 415,
            sellingPrice: 490,
            taxRate: 0,
            stockQuantity: 2,
            minStockLevel: 10,
            unit: 'Bag',
            status: 'ACTIVE',
            createdAt: '2026-11-04'
          },
          quantity: 2,
          unitPrice: 490,
          taxRate: 0,
          taxAmount: 0,
          subtotal: 980,
          total: 980
        },
        {
          product: {
            id: 2,
            name: 'Fortune Sunlite Refined Sunflower Oil 1L',
            sku: 'OIL-SUN-002',
            barcode: '8906007281023',
            category: 'Edible Oils',
            costPrice: 132,
            sellingPrice: 155,
            taxRate: 5,
            stockQuantity: 6,
            minStockLevel: 20,
            unit: 'Pouch',
            status: 'ACTIVE',
            createdAt: '2026-11-02'
          },
          quantity: 2,
          unitPrice: 155,
          taxRate: 5,
          taxAmount: 14.76,
          subtotal: 295.24,
          total: 310
        }
      ],
      subtotal: 1275.24,
      taxAmount: 14.76,
      discountAmount: 0,
      grandTotal: 1290,
      paymentMethod: 'CARD',
      amountPaid: 1290,
      changeDue: 0,
      customerName: 'Suresh Menon',
      cashierName: 'Sapna (Store Manager)',
      terminalId: 'POS-01'
    },
    {
      invoiceNumber: 'INV-2026-1041',
      orderDate: getPastDateStr(6),
      orderTime: '06:10 PM',
      items: [
        {
          product: {
            id: 8,
            name: 'Britannia Good Day Butter Cookies 200g',
            sku: 'SNK-BRI-008',
            barcode: '8901063012107',
            category: 'Biscuits & Bakery',
            costPrice: 38,
            sellingPrice: 45,
            taxRate: 18,
            stockQuantity: 42,
            minStockLevel: 20,
            unit: 'Pack',
            status: 'ACTIVE',
            createdAt: '2026-11-08'
          },
          quantity: 4,
          unitPrice: 45,
          taxRate: 18,
          taxAmount: 27.46,
          subtotal: 152.54,
          total: 180
        },
        {
          product: {
            id: 3,
            name: 'Amul Butter Pasteurised 500g',
            sku: 'DAI-BUT-003',
            barcode: '8901262010053',
            category: 'Dairy',
            costPrice: 250,
            sellingPrice: 285,
            taxRate: 12,
            stockQuantity: 3,
            minStockLevel: 12,
            unit: 'Pack',
            status: 'ACTIVE',
            createdAt: '2026-11-03'
          },
          quantity: 2,
          unitPrice: 285,
          taxRate: 12,
          taxAmount: 61.07,
          subtotal: 508.93,
          total: 570
        }
      ],
      subtotal: 661.47,
      taxAmount: 88.53,
      discountAmount: 0,
      grandTotal: 750,
      paymentMethod: 'CASH',
      amountPaid: 800,
      changeDue: 50,
      customerName: 'Walk-in Customer',
      cashierName: 'Sapna (Store Manager)',
      terminalId: 'POS-01'
    },
    {
      invoiceNumber: 'INV-2026-1040',
      orderDate: getPastDateStr(10),
      orderTime: '02:25 PM',
      items: [
        {
          product: {
            id: 1,
            name: 'Aashirvaad Shudh Chakki Atta 5kg',
            sku: 'GRO-ATT-001',
            barcode: '8901030383701',
            category: 'Flour & Grains',
            costPrice: 210,
            sellingPrice: 245,
            taxRate: 0,
            stockQuantity: 4,
            minStockLevel: 15,
            unit: 'Bag',
            status: 'ACTIVE',
            createdAt: '2026-11-01'
          },
          quantity: 2,
          unitPrice: 245,
          taxRate: 0,
          taxAmount: 0,
          subtotal: 490,
          total: 490
        },
        {
          product: {
            id: 4,
            name: 'India Gate Feast Rozzana Basmati 5kg',
            sku: 'RIC-BAS-004',
            barcode: '8901384010321',
            category: 'Rice & Cereals',
            costPrice: 415,
            sellingPrice: 490,
            taxRate: 0,
            stockQuantity: 2,
            minStockLevel: 10,
            unit: 'Bag',
            status: 'ACTIVE',
            createdAt: '2026-11-04'
          },
          quantity: 1,
          unitPrice: 490,
          taxRate: 0,
          taxAmount: 0,
          subtotal: 490,
          total: 490
        }
      ],
      subtotal: 980,
      taxAmount: 0,
      discountAmount: 30,
      grandTotal: 950,
      paymentMethod: 'UPI',
      amountPaid: 950,
      changeDue: 0,
      customerName: 'Vikram Joshi',
      cashierName: 'Sapna (Store Manager)',
      terminalId: 'POS-01'
    },
    {
      invoiceNumber: 'INV-2026-1039',
      orderDate: getPastDateStr(16),
      orderTime: '11:10 AM',
      items: [
        {
          product: {
            id: 10,
            name: 'Surf Excel Easy Wash Detergent Powder 1kg',
            sku: 'HH-SUR-010',
            barcode: '8901030012403',
            category: 'Household Supplies',
            costPrice: 125,
            sellingPrice: 145,
            taxRate: 18,
            stockQuantity: 0,
            minStockLevel: 15,
            unit: 'Pack',
            status: 'ACTIVE',
            createdAt: '2026-11-10'
          },
          quantity: 2,
          unitPrice: 145,
          taxRate: 18,
          taxAmount: 44.24,
          subtotal: 245.76,
          total: 290
        },
        {
          product: {
            id: 2,
            name: 'Fortune Sunlite Refined Sunflower Oil 1L',
            sku: 'OIL-SUN-002',
            barcode: '8906007281023',
            category: 'Edible Oils',
            costPrice: 132,
            sellingPrice: 155,
            taxRate: 5,
            stockQuantity: 6,
            minStockLevel: 20,
            unit: 'Pouch',
            status: 'ACTIVE',
            createdAt: '2026-11-02'
          },
          quantity: 3,
          unitPrice: 155,
          taxRate: 5,
          taxAmount: 22.14,
          subtotal: 442.86,
          total: 465
        }
      ],
      subtotal: 688.62,
      taxAmount: 66.38,
      discountAmount: 0,
      grandTotal: 755,
      paymentMethod: 'CARD',
      amountPaid: 755,
      changeDue: 0,
      customerName: 'Meera Iyer',
      cashierName: 'Sapna (Store Manager)',
      terminalId: 'POS-01'
    }
  ];

  private completedOrdersSubject = new BehaviorSubject<PosOrder[]>(this.initialOrders);

  constructor(
    private productService: ProductService,
    private inventoryService: InventoryService
  ) {}

  generateInvoiceNumber(): string {
    const invoice = `INV-2026-${this.invoiceCounter}`;
    this.invoiceCounter++;
    return invoice;
  }

  getCompletedOrders(): Observable<PosOrder[]> {
    return this.completedOrdersSubject.asObservable();
  }

  processCheckout(order: PosOrder): Observable<PosOrder> {
    // 1. Deduct stock for each cart item via InventoryService to maintain full inventory synchronization
    order.items.forEach((item) => {
      this.inventoryService.stockOut(
        item.product.id,
        item.quantity,
        `POS Sale Checkout (${order.invoiceNumber})`,
        order.invoiceNumber
      ).subscribe();
    });

    // 2. Record completed order
    const currentOrders = this.completedOrdersSubject.value;
    this.completedOrdersSubject.next([order, ...currentOrders]);

    return of(order);
  }
}
