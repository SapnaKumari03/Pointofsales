export interface Product {
  id: number;
  name: string;
  sku: string;
  barcode: string;
  category: string;
  costPrice: number;
  sellingPrice: number;
  taxRate: number; // In percentage: 0, 5, 12, 18, 28
  stockQuantity: number;
  minStockLevel: number;
  unit: string; // Bag, Pouch, Pack, Bottle, Kg, Box
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  description?: string;
}

export type StockStatus = 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';

export interface ProductFilter {
  searchTerm: string;
  category: string;
  stockStatus: string;
}
