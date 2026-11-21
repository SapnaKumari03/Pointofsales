import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainLayoutComponent } from './shared/components/main-layout/main-layout.component';
import { DashboardComponent } from './modules/dashboard/dashboard.component';
import { PlaceholderComponent } from './shared/components/placeholder/placeholder.component';
import { ProductsListComponent } from './modules/products/products-list.component';
import { InventoryManagementComponent } from './modules/inventory/inventory-management.component';
import { PosTerminalComponent } from './modules/pos/pos-terminal.component';
import { SalesHistoryComponent } from './modules/sales/sales-history.component';
import { CashManagementComponent } from './modules/cash/cash-management.component';
import { ReportsAnalyticsComponent } from './modules/reports/reports-analytics.component';

const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'products', component: ProductsListComponent },
      { path: 'inventory', component: InventoryManagementComponent },
      { path: 'pos-billing', component: PosTerminalComponent },
      { path: 'sales-history', component: SalesHistoryComponent },
      { path: 'payments', component: CashManagementComponent },
      { path: 'reports', component: ReportsAnalyticsComponent },
      { path: 'settings', component: PlaceholderComponent, data: { moduleKey: 'settings' } },

      // Route aliases
      { path: 'pos', redirectTo: 'pos-billing' },
      { path: 'sales', redirectTo: 'sales-history' },
      { path: 'stock', redirectTo: 'inventory' },
      { path: 'cash-drawer', redirectTo: 'payments' },
      { path: 'analytics', redirectTo: 'reports' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
