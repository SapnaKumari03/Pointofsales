import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../services/product.service';
import { InventoryService } from '../../services/inventory.service';
import { Product, StockStatus } from '../../models/product.model';
import { StockMovement, InventoryMetrics } from '../../models/inventory.model';

@Component({
  selector: 'app-inventory-management',
  templateUrl: './inventory-management.component.html',
  styleUrls: ['./inventory-management.component.css']
})
export class InventoryManagementComponent implements OnInit {
  activeTab: 'inventory' | 'movements' = 'inventory';

  products: Product[] = [];
  categories: string[] = [];
  movements: StockMovement[] = [];

  metrics: InventoryMetrics = {
    totalItems: 0,
    totalUnits: 0,
    lowStockCount: 0,
    outOfStockCount: 0,
    totalStockValue: 0,
    potentialRetailValue: 0
  };

  // Filter & Search states
  searchTerm = '';
  selectedCategory = 'ALL';
  selectedStockFilter = 'ALL';
  movementFilterType = 'ALL';

  // Modal states
  showStockInModal = false;
  showStockOutModal = false;
  showAdjustModal = false;
  showHistoryDrawer = false;

  selectedProduct: Product | null = null;
  productMovements: StockMovement[] = [];

  // Operation Form Models
  stockInQty = 10;
  stockInRef = '';
  stockInNotes = 'Direct Vendor Delivery Intake';

  stockOutQty = 1;
  stockOutReason = 'Damaged / Defective Packaging';
  stockOutRef = '';

  adjustActualQty = 0;
  adjustReason = 'Monthly Physical Inventory Audit';
  adjustRef = 'AUDIT-' + new Date().getFullYear();

  stockOutReasons = [
    'Damaged / Defective Packaging',
    'Expired Shelf Life Batch',
    'Internal Store Consumption / Sampling',
    'Stock Shrinkage / Breakage',
    'Return to Vendor (RTV)',
    'Other Adjustment'
  ];

  adjustmentReasons = [
    'Monthly Physical Inventory Audit',
    'Cycle Count Discrepancy Reconciliation',
    'Barcode Scanning Correction',
    'System Re-alignment',
    'Opening Inventory Calibration'
  ];

  constructor(
    private productService: ProductService,
    private inventoryService: InventoryService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    // 1. Subscribe to reactive products
    this.productService.getProducts().subscribe((res) => {
      this.products = res;
      this.categories = Array.from(new Set(res.map((p) => p.category))).sort();
    });

    // 2. Subscribe to inventory metrics
    this.inventoryService.getMetrics().subscribe((m) => {
      this.metrics = m;
    });

    // 3. Subscribe to stock movements
    this.inventoryService.getMovements().subscribe((mv) => {
      this.movements = mv;
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

      let matchesStock = true;
      const status = this.getStockStatus(p);
      if (this.selectedStockFilter === 'LOW_STOCK') {
        matchesStock = status === 'LOW_STOCK';
      } else if (this.selectedStockFilter === 'OUT_OF_STOCK') {
        matchesStock = status === 'OUT_OF_STOCK';
      } else if (this.selectedStockFilter === 'IN_STOCK') {
        matchesStock = status === 'IN_STOCK';
      }

      return matchesSearch && matchesCat && matchesStock;
    });
  }

  get filteredMovements(): StockMovement[] {
    return this.movements.filter((m) => {
      const term = this.searchTerm.trim().toLowerCase();
      const matchesSearch =
        !term ||
        m.productName.toLowerCase().includes(term) ||
        m.sku.toLowerCase().includes(term) ||
        (m.referenceNo && m.referenceNo.toLowerCase().includes(term));

      const matchesType =
        this.movementFilterType === 'ALL' || m.type === this.movementFilterType;

      return matchesSearch && matchesType;
    });
  }

  getStockStatus(p: Product): StockStatus {
    if (p.stockQuantity <= 0) return 'OUT_OF_STOCK';
    if (p.stockQuantity <= p.minStockLevel) return 'LOW_STOCK';
    return 'IN_STOCK';
  }

  getStockPercentage(p: Product): number {
    const target = p.minStockLevel * 2 || 10;
    return Math.min(100, Math.round((p.stockQuantity / target) * 100));
  }

  // Stock In Flow
  openStockIn(p: Product): void {
    this.selectedProduct = p;
    this.stockInQty = 10;
    this.stockInRef = 'PO-' + Math.floor(1000 + Math.random() * 9000);
    this.stockInNotes = 'Direct Vendor Delivery Intake';
    this.showStockInModal = true;
  }

  submitStockIn(): void {
    if (!this.selectedProduct) return;
    if (this.stockInQty <= 0) {
      alert('Please enter a valid stock addition quantity greater than 0.');
      return;
    }

    this.inventoryService
      .stockIn(this.selectedProduct.id, this.stockInQty, this.stockInRef, this.stockInNotes)
      .subscribe(() => {
        this.closeModals();
      });
  }

  // Stock Out Flow
  openStockOut(p: Product): void {
    this.selectedProduct = p;
    this.stockOutQty = 1;
    this.stockOutReason = this.stockOutReasons[0];
    this.stockOutRef = 'DMG-' + Math.floor(100 + Math.random() * 900);
    this.showStockOutModal = true;
  }

  submitStockOut(): void {
    if (!this.selectedProduct) return;
    if (this.stockOutQty <= 0) {
      alert('Stock deduction quantity must be greater than 0.');
      return;
    }
    if (this.stockOutQty > this.selectedProduct.stockQuantity) {
      alert(`Cannot deduct ${this.stockOutQty} units. Only ${this.selectedProduct.stockQuantity} units available.`);
      return;
    }

    this.inventoryService
      .stockOut(this.selectedProduct.id, this.stockOutQty, this.stockOutReason, this.stockOutRef)
      .subscribe(() => {
        this.closeModals();
      });
  }

  // Stock Adjustment Flow
  openAdjust(p: Product): void {
    this.selectedProduct = p;
    this.adjustActualQty = p.stockQuantity;
    this.adjustReason = this.adjustmentReasons[0];
    this.adjustRef = 'AUD-' + new Date().getMonth() + '-' + Math.floor(10 + Math.random() * 90);
    this.showAdjustModal = true;
  }

  submitAdjust(): void {
    if (!this.selectedProduct) return;
    if (this.adjustActualQty < 0) {
      alert('Stock quantity cannot be negative.');
      return;
    }

    this.inventoryService
      .adjustStock(this.selectedProduct.id, this.adjustActualQty, this.adjustReason, this.adjustRef)
      .subscribe(() => {
        this.closeModals();
      });
  }

  // Item Specific History Drawer
  openItemHistory(p: Product): void {
    this.selectedProduct = p;
    this.productMovements = this.movements.filter((m) => m.productId === p.id);
    this.showHistoryDrawer = true;
  }

  closeModals(): void {
    this.showStockInModal = false;
    this.showStockOutModal = false;
    this.showAdjustModal = false;
    this.showHistoryDrawer = false;
    this.selectedProduct = null;
  }
}
