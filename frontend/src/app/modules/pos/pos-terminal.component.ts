import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../services/product.service';
import { PosService } from '../../services/pos.service';
import { Product } from '../../models/product.model';
import { CartItem, PaymentMethod, PosOrder } from '../../models/pos.model';

@Component({
  selector: 'app-pos-terminal',
  templateUrl: './pos-terminal.component.html',
  styleUrls: ['./pos-terminal.component.css']
})
export class PosTerminalComponent implements OnInit {
  products: Product[] = [];
  categories: string[] = [];

  // Catalog search & filter
  searchTerm = '';
  selectedCategory = 'ALL';

  // Shopping Cart
  cart: CartItem[] = [];
  discountAmount = 0;

  // Customer & Cashier Info
  customerName = 'Walk-in Customer';
  customerPhone = '';
  cashierName = 'Sapna (Store Manager)';
  terminalId = 'POS-01';

  // Payment State
  paymentMethod: PaymentMethod = 'CASH';
  cashReceived = 0;
  upiReference = '';
  cardLast4 = '';

  // Completed Order & Receipt Modal
  showReceiptModal = false;
  completedOrder: PosOrder | null = null;

  constructor(
    private productService: ProductService,
    private posService: PosService
  ) {}

  ngOnInit(): void {
    this.loadCatalog();
  }

  loadCatalog(): void {
    this.productService.getProducts().subscribe((res) => {
      this.products = res;
      this.categories = Array.from(new Set(res.map((p) => p.category))).sort();
    });
  }

  get filteredProducts(): Product[] {
    return this.products.filter((p) => {
      const term = this.searchTerm.trim().toLowerCase();
      const matchesSearch =
        !term ||
        p.name.toLowerCase().includes(term) ||
        p.sku.toLowerCase().includes(term) ||
        p.barcode.includes(term);

      const matchesCat = this.selectedCategory === 'ALL' || p.category === this.selectedCategory;
      return matchesSearch && matchesCat;
    });
  }

  // Handle barcode scanner input enter key
  onSearchEnter(): void {
    const term = this.searchTerm.trim();
    if (!term) return;

    // Check exact barcode match first
    const exactMatch = this.products.find((p) => p.barcode === term);
    if (exactMatch) {
      this.addToCart(exactMatch);
      this.searchTerm = '';
      return;
    }

    // Check if search results have exactly 1 item
    const matches = this.filteredProducts;
    if (matches.length === 1) {
      this.addToCart(matches[0]);
      this.searchTerm = '';
    }
  }

  // Cart Operations
  addToCart(product: Product): void {
    if (product.stockQuantity <= 0) {
      alert(`Cannot add "${product.name}". Product is currently out of stock!`);
      return;
    }

    const existingIndex = this.cart.findIndex((item) => item.product.id === product.id);

    if (existingIndex > -1) {
      const currentQty = this.cart[existingIndex].quantity;
      if (currentQty >= product.stockQuantity) {
        alert(`Maximum available stock reached for "${product.name}" (${product.stockQuantity} ${product.unit}).`);
        return;
      }
      this.updateItemQuantity(existingIndex, currentQty + 1);
    } else {
      const unitPrice = product.sellingPrice;
      const basePrice = unitPrice / (1 + product.taxRate / 100);
      const taxPerUnit = unitPrice - basePrice;

      const newItem: CartItem = {
        product,
        quantity: 1,
        unitPrice,
        taxRate: product.taxRate,
        taxAmount: Math.round(taxPerUnit * 100) / 100,
        subtotal: Math.round(basePrice * 100) / 100,
        total: unitPrice
      };

      this.cart.push(newItem);
      this.updateCashDefaults();
    }
  }

  increaseQuantity(index: number): void {
    const item = this.cart[index];
    if (item.quantity >= item.product.stockQuantity) {
      alert(`Cannot add more. Available stock is ${item.product.stockQuantity} ${item.product.unit}.`);
      return;
    }
    this.updateItemQuantity(index, item.quantity + 1);
  }

  decreaseQuantity(index: number): void {
    const item = this.cart[index];
    if (item.quantity > 1) {
      this.updateItemQuantity(index, item.quantity - 1);
    } else {
      this.removeFromCart(index);
    }
  }

  private updateItemQuantity(index: number, newQty: number): void {
    const item = this.cart[index];
    item.quantity = newQty;
    const itemTotal = item.unitPrice * newQty;
    const baseTotal = itemTotal / (1 + item.taxRate / 100);

    item.total = itemTotal;
    item.subtotal = Math.round(baseTotal * 100) / 100;
    item.taxAmount = Math.round((itemTotal - baseTotal) * 100) / 100;

    this.updateCashDefaults();
  }

  removeFromCart(index: number): void {
    this.cart.splice(index, 1);
    this.updateCashDefaults();
  }

  clearCart(): void {
    if (this.cart.length > 0 && confirm('Are you sure you want to clear all items in the current cart?')) {
      this.cart = [];
      this.discountAmount = 0;
      this.cashReceived = 0;
    }
  }

  // Cart Calculations
  get totalItemsCount(): number {
    return this.cart.reduce((sum, item) => sum + item.quantity, 0);
  }

  get cartSubtotal(): number {
    return Math.round(this.cart.reduce((sum, item) => sum + item.subtotal, 0) * 100) / 100;
  }

  get cartTax(): number {
    return Math.round(this.cart.reduce((sum, item) => sum + item.taxAmount, 0) * 100) / 100;
  }

  get cartGrandTotal(): number {
    const total = this.cart.reduce((sum, item) => sum + item.total, 0) - this.discountAmount;
    return Math.max(0, Math.round(total * 100) / 100);
  }

  get changeDue(): number {
    if (this.paymentMethod !== 'CASH') return 0;
    return Math.max(0, Math.round((this.cashReceived - this.cartGrandTotal) * 100) / 100);
  }

  get isCashSufficient(): boolean {
    if (this.paymentMethod !== 'CASH') return true;
    return this.cashReceived >= this.cartGrandTotal;
  }

  // Quick cash tender helper buttons
  setQuickCash(amount: number): void {
    this.cashReceived = amount;
  }

  setExactCash(): void {
    this.cashReceived = this.cartGrandTotal;
  }

  private updateCashDefaults(): void {
    if (this.paymentMethod === 'CASH' && this.cashReceived < this.cartGrandTotal) {
      this.cashReceived = Math.ceil(this.cartGrandTotal / 100) * 100 || this.cartGrandTotal;
    }
  }

  // Checkout Execution
  checkout(): void {
    if (this.cart.length === 0) {
      alert('Cart is empty. Please add products before checking out.');
      return;
    }

    // 1. Validate Stock Availability for all cart items
    for (const item of this.cart) {
      if (item.quantity > item.product.stockQuantity) {
        alert(
          `Insufficient stock for "${item.product.name}". Requested: ${item.quantity}, Available: ${item.product.stockQuantity}. Please adjust quantity before checkout.`
        );
        return;
      }
    }

    // 2. Validate Payment
    if (this.paymentMethod === 'CASH' && !this.isCashSufficient) {
      alert(`Tendered cash (₹${this.cashReceived}) is less than grand total (₹${this.cartGrandTotal}).`);
      return;
    }

    const now = new Date();
    const orderDate = now.toISOString().split('T')[0];
    const orderTime = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });

    const newOrder: PosOrder = {
      invoiceNumber: this.posService.generateInvoiceNumber(),
      orderDate,
      orderTime,
      items: [...this.cart],
      subtotal: this.cartSubtotal,
      taxAmount: this.cartTax,
      discountAmount: this.discountAmount,
      grandTotal: this.cartGrandTotal,
      paymentMethod: this.paymentMethod,
      amountPaid: this.paymentMethod === 'CASH' ? this.cashReceived : this.cartGrandTotal,
      changeDue: this.changeDue,
      customerName: this.customerName || 'Walk-in Customer',
      customerPhone: this.customerPhone || undefined,
      cashierName: this.cashierName,
      terminalId: this.terminalId
    };

    // 3. Process Checkout via PosService (which deducts inventory via InventoryService)
    this.posService.processCheckout(newOrder).subscribe((confirmedOrder) => {
      this.completedOrder = confirmedOrder;
      this.showReceiptModal = true;

      // Reset cart and register
      this.cart = [];
      this.discountAmount = 0;
      this.cashReceived = 0;
      this.customerName = 'Walk-in Customer';
      this.customerPhone = '';
      this.loadCatalog(); // Refresh available stock levels in UI
    });
  }

  closeReceiptModal(): void {
    this.showReceiptModal = false;
    this.completedOrder = null;
  }

  printReceipt(): void {
    window.print();
  }
}
