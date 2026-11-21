import { Product } from './product.model';

export interface CartItem {
  product: Product;
  quantity: number;
  unitPrice: number;
  taxRate: number;
  taxAmount: number;
  subtotal: number;
  total: number;
}

export type PaymentMethod = 'CASH' | 'UPI' | 'CARD';

export interface PosOrder {
  invoiceNumber: string;
  orderDate: string;
  orderTime: string;
  items: CartItem[];
  subtotal: number;
  taxAmount: number;
  discountAmount: number;
  grandTotal: number;
  paymentMethod: PaymentMethod;
  amountPaid: number;
  changeDue: number;
  customerName: string;
  customerPhone?: string;
  cashierName: string;
  terminalId: string;
}
