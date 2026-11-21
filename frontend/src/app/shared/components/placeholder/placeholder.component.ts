import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-placeholder',
  templateUrl: './placeholder.component.html',
  styleUrls: ['./placeholder.component.css']
})
export class PlaceholderComponent implements OnInit {
  moduleTitle = '';
  moduleDescription = '';
  moduleIcon = '';

  private moduleConfigs: Record<string, { title: string; desc: string; icon: string }> = {
    'products': {
      title: 'Product Catalog Management',
      desc: 'Maintain SKU, barcode, brand, category, cost price, selling price, and tax rates.',
      icon: 'products'
    },
    'inventory': {
      title: 'Stock & Inventory Management',
      desc: 'Track physical stock, automated stock-in entries, manual audit adjustments, and reorder levels.',
      icon: 'inventory'
    },
    'pos-billing': {
      title: 'POS Cashier Billing Terminal',
      desc: 'High-speed barcode scanner checkout, dynamic item discounts, tax calculations, and thermal receipts.',
      icon: 'pos'
    },
    'sales-history': {
      title: 'Sales Orders & Invoices History',
      desc: 'Lookup historical invoices, customer purchase histories, payment reconciliation, and reprint receipts.',
      icon: 'sales-history'
    },
    'payments': {
      title: 'Payments & Cash Drawer Management',
      desc: 'Daily shift opening/closing floats, UPI/Card settlements, and cash drawer balance reconciliation.',
      icon: 'payments'
    },
    'reports': {
      title: 'Sales & Inventory Analytics Reports',
      desc: 'Detailed gross profit margins, fast/slow-moving goods analysis, and periodic tax filing reports.',
      icon: 'reports'
    },
    'settings': {
      title: 'Store & System Settings',
      desc: 'Configure Oracle 21c XE connection profiles, thermal printer paper size, tax percentages, and staff accounts.',
      icon: 'settings'
    }
  };

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.data.subscribe((data) => {
      const key = data['moduleKey'] || 'module';
      const config = this.moduleConfigs[key] || {
        title: 'Module Overview',
        desc: 'Phase 1 POS Dashboard is complete. This module is queued for Phase 2.',
        icon: 'dashboard'
      };
      this.moduleTitle = config.title;
      this.moduleDescription = config.desc;
      this.moduleIcon = config.icon;
    });
  }
}
