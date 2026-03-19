import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { finalize, forkJoin } from 'rxjs';
import { CraService } from '../../../services/cra.service';
import { CraDto, CraStatus } from '../../../models/cra.model';
import { UiFeedbackService } from '../../../services/ui-feedback.service';

@Component({
  selector: 'app-cra-validation',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  templateUrl: './cra-validation.component.html',
  styleUrls: ['./cra-validation.component.css']
})
export class CraValidationComponent implements OnInit {
  cras: CraDto[] = [];
  filteredCras: CraDto[] = [];

  loading = true;
  currentDate = new Date();
  statusFilter = 'ALL';
  searchTerm = '';
  skeletonItems = Array.from({ length: 5 });

  showMotifModal = false;
  motifAction: 'reject' | 'invalidate' = 'reject';
  motifCra: CraDto | null = null;
  motifInput = '';
  sendingMotif = false;

  constructor(
    private readonly service: CraService,
    private readonly ui: UiFeedbackService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  get currentMonthName(): string {
    return this.currentDate.toLocaleString('default', { month: 'long' });
  }

  get currentYear(): number {
    return this.currentDate.getFullYear();
  }

  loadData(): void {
    this.loading = true;
    const month = this.currentDate.getMonth() + 1;
    const year = this.currentDate.getFullYear();

    // Fetch ALL pending CRAs (regardless of month) and verify current month's CRAs context
    const pending$ = this.service.getAll(undefined, undefined, 'SUBMITTED');
    const currentMonth$ = this.service.getAll(month, year);

    forkJoin([pending$, currentMonth$]).pipe(
      finalize(() => (this.loading = false))
    ).subscribe({
      next: ([pendingCras, monthCras]) => {
        // Merge without duplicates: prefer pending list if conflict (though shouldn't matter)
        const uniqueCras = new Map<number, CraDto>();
        monthCras.forEach(c => uniqueCras.set(c.id, c));
        pendingCras.forEach(c => uniqueCras.set(c.id, c));
        
        this.cras = Array.from(uniqueCras.values());
        this.applyFilters();
      },
      error: () => this.ui.showToast('error', 'Erreur chargement CRA')
    });
  }

  previousMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
    this.loadData();
  }

  nextMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
    this.loadData();
  }

  filter(event: Event): void {
    this.searchTerm = (event.target as HTMLInputElement).value.toLowerCase();
    this.applyFilters();
  }

  applyFilters(): void {
    this.filteredCras = this.cras.filter((c) => {
      const name = (c.collaborateurName || '').toLowerCase();
      const email = (c.collaborateurEmail || '').toLowerCase();
      const matchName = name.includes(this.searchTerm) || email.includes(this.searchTerm);
      
      // Filter by status:
      // If 'ALL' is selected, we exclude 'DRAFT' because admins shouldn't see unsubmitted CRAs.
      // Otherwise, we strictly match the selected status.
      let matchStatus = false;
      if (this.statusFilter === 'ALL') {
         matchStatus = c.status !== 'DRAFT'; 
      } else {
         matchStatus = c.status === this.statusFilter;
      }
      
      return matchName && matchStatus;
    });
  }

  countByStatus(status: CraStatus): number {
    return this.cras.filter((c) => c.status === status).length;
  }

  getWorkedDays(cra: CraDto): number {
    if (!cra.activities) {
      return 0;
    }
    return cra.activities.reduce((acc, act) => acc + (act.duree || 0), 0);
  }

  statusClass(status: CraStatus): string {
    if (status === 'SUBMITTED') {
      return 'status-submitted';
    }
    if (status === 'APPROVED') {
      return 'status-approved';
    }
    if (status === 'REJECTED') {
      return 'status-rejected';
    }
    return '';
  }

  getStatusLabel(status: CraStatus): string {
    if (status === 'SUBMITTED') {
      return 'En attente';
    }
    if (status === 'APPROVED') {
      return 'Valide';
    }
    if (status === 'REJECTED') {
      return 'Rejete';
    }
    if (status === 'DRAFT') {
      return 'Brouillon';
    }
    return 'Invalide';
  }

  getActionLabel(status: CraStatus): string {
    if (status === 'DRAFT') {
      return 'Aucune action';
    }
    if (status === 'REJECTED') {
      return 'Deja rejete';
    }
    if (status === 'INVALIDATED') {
      return 'Deja invalide';
    }
    return 'Aucune action';
  }

  validate(cra: CraDto): void {
    this.service.validate(cra.id).subscribe({
      next: () => {
        this.ui.showToast('success', 'CRA valide avec succes');
        this.loadData();
      },
      error: () => this.ui.showToast('error', 'Validation impossible')
    });
  }

  openRejectModal(cra: CraDto): void {
    this.motifAction = 'reject';
    this.motifCra = cra;
    this.motifInput = '';
    this.showMotifModal = true;
  }

  openInvalidateModal(cra: CraDto): void {
    this.motifAction = 'invalidate';
    this.motifCra = cra;
    this.motifInput = '';
    this.showMotifModal = true;
  }

  closeMotifModal(): void {
    this.showMotifModal = false;
    this.sendingMotif = false;
    this.motifCra = null;
    this.motifInput = '';
  }

  submitMotif(): void {
    if (!this.motifCra) {
      return;
    }

    const motif = this.motifInput.trim();
    if (!motif) {
      this.ui.showToast('warning', "Le motif est obligatoire pour cette action.");
      return;
    }

    this.sendingMotif = true;
    const req$ = this.motifAction === 'reject'
      ? this.service.reject(this.motifCra.id, motif)
      : this.service.invalidate(this.motifCra.id, motif);

    req$.subscribe({
      next: () => {
        this.ui.showToast('info', this.motifAction === 'reject' ? 'CRA rejete' : 'CRA invalide');
        this.closeMotifModal();
        this.loadData();
      },
      error: () => {
        this.sendingMotif = false;
        this.ui.showToast('error', this.motifAction === 'reject' ? 'Rejet impossible' : 'Invalidation impossible');
      }
    });
  }
}
