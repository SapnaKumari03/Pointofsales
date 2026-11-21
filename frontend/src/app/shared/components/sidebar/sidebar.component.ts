import { Component, Input, Output, EventEmitter } from '@angular/core';

export interface NavItem {
  label: string;
  route: string;
  icon: string;
  badge?: string;
  badgeType?: 'warning' | 'primary' | 'success';
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @Input() isCollapsed = false;
  @Output() toggleCollapse = new EventEmitter<void>();

  // Exactly matching the requested 8 navigation modules
  navItems: NavItem[] = [
    { label: 'Dashboard', route: '/dashboard', icon: 'dashboard' },
    { label: 'Products', route: '/products', icon: 'products' },
    { label: 'Inventory', route: '/inventory', icon: 'inventory', badge: '6 Low', badgeType: 'warning' },
    { label: 'POS Billing', route: '/pos-billing', icon: 'pos', badge: 'F2', badgeType: 'primary' },
    { label: 'Sales History', route: '/sales-history', icon: 'sales-history' },
    { label: 'Payments', route: '/payments', icon: 'payments' },
    { label: 'Reports', route: '/reports', icon: 'reports' },
    { label: 'Settings', route: '/settings', icon: 'settings' }
  ];

  onToggle() {
    this.toggleCollapse.emit();
  }
}
