import { Component, Output, EventEmitter, Input } from '@angular/core';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  @Input() isSidebarCollapsed = false;
  @Output() toggleSidebar = new EventEmitter<void>();

  searchQuery = '';
  notificationsOpen = false;
  profileOpen = false;

  todayDate = new Date();

  notifications = [
    { title: 'Low Stock Alert', desc: 'Aashirvaad Atta 5kg has only 4 bags left', time: '10m ago', urgent: true },
    { title: 'Low Stock Alert', desc: 'Amul Butter 500g has only 3 packs left', time: '25m ago', urgent: true },
    { title: 'Cash Drawer Float', desc: 'Opening float verified ₹5,000', time: '1h ago', urgent: false }
  ];

  onToggleSidebar() {
    this.toggleSidebar.emit();
  }

  toggleNotifications() {
    this.notificationsOpen = !this.notificationsOpen;
    if (this.notificationsOpen) {
      this.profileOpen = false;
    }
  }

  toggleProfile() {
    this.profileOpen = !this.profileOpen;
    if (this.profileOpen) {
      this.notificationsOpen = false;
    }
  }

  onSearch(event: Event) {
    event.preventDefault();
    if (this.searchQuery.trim()) {
      alert(`Searching for product/barcode: ${this.searchQuery}`);
    }
  }
}
