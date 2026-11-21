import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../services/product.service';
import { Product, StockStatus } from '../../models/product.model';

@Component({
  selector: 'app-products-list',
  templateUrl: './products-list.component.html',
  styleUrls: ['./products-list.component.css']
})
export class ProductsListComponent implements OnInit {
  products: Product[] = [];
  categories: string[] = [];

  // Filter & Search states
  searchTerm = '';
  selectedCategory = 'ALL';
  selectedStockFilter = 'ALL';

  // Modal states
  showFormModal = false;
  isEditMode = false;
  currentEditingId: number | null = null;

  showDetailsModal = false;
  selectedProduct: Product | null = null;

  showDeleteModal = false;
  productToDelete: Product | null = null;

  // Form Model
  formData = this.getEmptyForm();

  // Unit Options common in Indian retail
  unitOptions = ['Bag', 'Pouch', 'Pack', 'Bottle', 'Box', 'Kg', 'Litre', 'Piece'];
  taxOptions = [0, 5, 12, 18, 28];

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.productService.getProducts().subscribe((res) => {
      this.products = res;
      this.categories = Array.from(new Set(res.map((p) => p.category))).sort();
    });
  }

  get filteredProducts(): Product[] {
    return this.products.filter((p) => {
      // 1. Search filter by Name, SKU, or Barcode
      const term = this.searchTerm.trim().toLowerCase();
      const matchesSearch =
        !term ||
        p.name.toLowerCase().includes(term) ||
        p.sku.toLowerCase().includes(term) ||
        p.barcode.includes(term);

      // 2. Category filter
      const matchesCategory =
        this.selectedCategory === 'ALL' || p.category === this.selectedCategory;

      // 3. Stock Status filter
      let matchesStock = true;
      const stockStatus = this.getStockStatus(p);
      if (this.selectedStockFilter === 'IN_STOCK') {
        matchesStock = stockStatus === 'IN_STOCK';
      } else if (this.selectedStockFilter === 'LOW_STOCK') {
        matchesStock = stockStatus === 'LOW_STOCK';
      } else if (this.selectedStockFilter === 'OUT_OF_STOCK') {
        matchesStock = stockStatus === 'OUT_OF_STOCK';
      } else if (this.selectedStockFilter === 'INACTIVE') {
        matchesStock = p.status === 'INACTIVE';
      }

      return matchesSearch && matchesCategory && matchesStock;
    });
  }

  // Summary counts for filter chips
  get totalCount(): number {
    return this.products.length;
  }

  get inStockCount(): number {
    return this.products.filter((p) => p.stockQuantity > p.minStockLevel && p.status === 'ACTIVE').length;
  }

  get lowStockCount(): number {
    return this.products.filter((p) => p.stockQuantity > 0 && p.stockQuantity <= p.minStockLevel).length;
  }

  get outOfStockCount(): number {
    return this.products.filter((p) => p.stockQuantity <= 0).length;
  }

  getStockStatus(p: Product): StockStatus {
    if (p.stockQuantity <= 0) return 'OUT_OF_STOCK';
    if (p.stockQuantity <= p.minStockLevel) return 'LOW_STOCK';
    return 'IN_STOCK';
  }

  calculateMargin(cost: number, sell: number): number {
    if (!sell || sell <= 0) return 0;
    return Math.round(((sell - cost) / sell) * 100);
  }

  // Add Product Flow
  openAddModal(): void {
    this.isEditMode = false;
    this.currentEditingId = null;
    this.formData = this.getEmptyForm();
    this.showFormModal = true;
  }

  // Edit Product Flow
  openEditModal(product: Product): void {
    this.isEditMode = true;
    this.currentEditingId = product.id;
    this.formData = {
      name: product.name,
      sku: product.sku,
      barcode: product.barcode,
      category: product.category,
      costPrice: product.costPrice,
      sellingPrice: product.sellingPrice,
      taxRate: product.taxRate,
      stockQuantity: product.stockQuantity,
      minStockLevel: product.minStockLevel,
      unit: product.unit,
      status: product.status,
      description: product.description || ''
    };
    this.showFormModal = true;
  }

  saveProduct(): void {
    if (!this.formData.name || !this.formData.sku || !this.formData.barcode || !this.formData.category) {
      alert('Please fill in all required fields (Name, SKU, Barcode, and Category).');
      return;
    }

    if (this.formData.sellingPrice < 0 || this.formData.costPrice < 0) {
      alert('Price values cannot be negative.');
      return;
    }

    if (this.isEditMode && this.currentEditingId !== null) {
      this.productService.updateProduct(this.currentEditingId, this.formData).subscribe(() => {
        this.closeFormModal();
      });
    } else {
      this.productService.addProduct(this.formData).subscribe(() => {
        this.closeFormModal();
      });
    }
  }

  closeFormModal(): void {
    this.showFormModal = false;
    this.currentEditingId = null;
    this.formData = this.getEmptyForm();
  }

  // View Details Flow
  openDetailsModal(product: Product): void {
    this.selectedProduct = product;
    this.showDetailsModal = true;
  }

  closeDetailsModal(): void {
    this.showDetailsModal = false;
    this.selectedProduct = null;
  }

  // Delete Flow with confirmation
  openDeleteModal(product: Product): void {
    this.productToDelete = product;
    this.showDeleteModal = true;
  }

  confirmDelete(): void {
    if (this.productToDelete) {
      this.productService.deleteProduct(this.productToDelete.id).subscribe(() => {
        this.closeDeleteModal();
      });
    }
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.productToDelete = null;
  }

  private getEmptyForm() {
    return {
      name: '',
      sku: '',
      barcode: '',
      category: 'Flour & Grains',
      costPrice: 0,
      sellingPrice: 0,
      taxRate: 5,
      stockQuantity: 10,
      minStockLevel: 5,
      unit: 'Pack',
      status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE',
      description: ''
    };
  }
}
