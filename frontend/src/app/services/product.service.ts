import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { Product } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  // Realistic Indian retail inventory seed data
  private initialProducts: Product[] = [
    {
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
      createdAt: '2026-11-01',
      description: '100% pure whole wheat flour processed with traditional chakki grinding'
    },
    {
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
      createdAt: '2026-11-02',
      description: 'Refined sunflower oil enriched with Vitamin A and Vitamin D'
    },
    {
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
      createdAt: '2026-11-03',
      description: 'Pasteurised table butter made from fresh dairy milk cream'
    },
    {
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
      createdAt: '2026-11-04',
      description: 'Aromatic aged long grain basmati rice ideal for everyday cooking'
    },
    {
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
      createdAt: '2026-11-05',
      description: 'Authentic blend of roasted spices for rich north Indian gravies'
    },
    {
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
      createdAt: '2026-11-06',
      description: 'Iodized vacuum evaporated salt maintaining purity and essential minerals'
    },
    {
      id: 7,
      name: 'Maggi 2-Minute Masala Instant Noodles 70g',
      sku: 'SNK-MAG-007',
      barcode: '8901058852312',
      category: 'Snacks & Instant Food',
      costPrice: 11.5,
      sellingPrice: 14,
      taxRate: 12,
      stockQuantity: 85,
      minStockLevel: 30,
      unit: 'Pack',
      status: 'ACTIVE',
      createdAt: '2026-11-07',
      description: 'Favorite Indian instant noodles with signature tastemaker seasoning'
    },
    {
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
      createdAt: '2026-11-08',
      description: 'Rich buttery crunchy cookies with signature smile patterns'
    },
    {
      id: 9,
      name: 'Dettol Original Liquid Handwash 200ml',
      sku: 'PC-DET-009',
      barcode: '8901396328101',
      category: 'Personal Care',
      costPrice: 78,
      sellingPrice: 99,
      taxRate: 18,
      stockQuantity: 18,
      minStockLevel: 15,
      unit: 'Bottle',
      status: 'ACTIVE',
      createdAt: '2026-11-09',
      description: 'Antibacterial germ protection liquid hand wash with pine fragrance'
    },
    {
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
      createdAt: '2026-11-10',
      description: 'Superior stain removal detergent powder with quick foaming action'
    },
    {
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
      createdAt: '2026-11-11',
      description: 'Strong CTC black tea with rich color and rejuvenating taste'
    },
    {
      id: 12,
      name: 'Parle-G Original Glucose Biscuits 800g',
      sku: 'SNK-PAR-012',
      barcode: '8901719101050',
      category: 'Biscuits & Bakery',
      costPrice: 70,
      sellingPrice: 82,
      taxRate: 18,
      stockQuantity: 35,
      minStockLevel: 15,
      unit: 'Pack',
      status: 'ACTIVE',
      createdAt: '2026-11-12',
      description: 'Wholesome glucose biscuits made from wheat milk and energy solids'
    }
  ];

  private productsSubject = new BehaviorSubject<Product[]>(this.initialProducts);

  getProducts(): Observable<Product[]> {
    return this.productsSubject.asObservable();
  }

  getProductById(id: number): Observable<Product | undefined> {
    return this.productsSubject.pipe(
      map(products => products.find(p => p.id === id))
    );
  }

  getCategories(): Observable<string[]> {
    return this.productsSubject.pipe(
      map(products => Array.from(new Set(products.map(p => p.category))).sort())
    );
  }

  addProduct(newProductData: Omit<Product, 'id' | 'createdAt'>): Observable<Product> {
    const currentList = this.productsSubject.value;
    const nextId = currentList.length > 0 ? Math.max(...currentList.map(p => p.id)) + 1 : 1;
    const today = new Date().toISOString().split('T')[0];

    const newProduct: Product = {
      ...newProductData,
      id: nextId,
      createdAt: today
    };

    this.productsSubject.next([newProduct, ...currentList]);
    return of(newProduct);
  }

  updateProduct(id: number, updatedFields: Partial<Product>): Observable<Product> {
    const currentList = this.productsSubject.value;
    const index = currentList.findIndex(p => p.id === id);

    if (index === -1) {
      throw new Error(`Product with ID ${id} not found`);
    }

    const updatedProduct: Product = {
      ...currentList[index],
      ...updatedFields
    };

    const updatedList = [...currentList];
    updatedList[index] = updatedProduct;

    this.productsSubject.next(updatedList);
    return of(updatedProduct);
  }

  deleteProduct(id: number): Observable<boolean> {
    const currentList = this.productsSubject.value;
    const filteredList = currentList.filter(p => p.id !== id);
    this.productsSubject.next(filteredList);
    return of(true);
  }
}
