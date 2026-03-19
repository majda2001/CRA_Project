import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './status-badge.component.html',
  styleUrls: ['./status-badge.component.css']
})
export class StatusBadgeComponent {
  @Input() status: string = 'DRAFT';

  get label(): string {
    switch (this.status) {
      case 'DRAFT':
        return 'Brouillon';
      case 'SUBMITTED':
        return 'Soumis';
      case 'APPROVED':
        return 'Valide';
      case 'REJECTED':
        return 'Rejete';
      case 'INVALIDATED':
        return 'Invalide';
      default:
        return this.status;
    }
  }
}
