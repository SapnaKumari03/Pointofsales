import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { MainLayoutComponent } from './shared/components/main-layout/main-layout.component';
import { DashboardComponent } from './modules/dashboard/dashboard.component';
import { PlaceholderComponent } from './shared/components/placeholder/placeholder.component';
import { ProductsListComponent } from './modules/products/products-list.component';
import { InventoryManagementComponent } from './modules/inventory/inventory-management.component';
import { PosTerminalComponent } from './modules/pos/pos-terminal.component';
import { SalesHistoryComponent } from './modules/sales/sales-history.component';
import { CashManagementComponent } from './modules/cash/cash-management.component';
import { ReportsAnalyticsComponent } from './modules/reports/reports-analytics.component';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    SidebarComponent,
    MainLayoutComponent,
    DashboardComponent,
    PlaceholderComponent,
    ProductsListComponent,
    InventoryManagementComponent,
    PosTerminalComponent,
    SalesHistoryComponent,
    CashManagementComponent,
    ReportsAnalyticsComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    FormsModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
