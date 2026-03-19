import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { finalize } from 'rxjs';
import {
  CollaborateurRequest,
  CollaborateurResponse,
  CollaborateurService
} from '../../../services/collaborateur.service';
import { UiFeedbackService } from '../../../services/ui-feedback.service';

@Component({
  selector: 'app-collaborators-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule],
  templateUrl: './collaborators-list.component.html',
  styleUrls: ['./collaborators-list.component.css']
})
export class CollaboratorsListComponent implements OnInit {
  collaborateurs: CollaborateurResponse[] = [];
  filteredCollaborateurs: CollaborateurResponse[] = [];
  loading = true;
  saving = false;
  showModal = false;
  editMode = false;
  selectedId: number | null = null;
  skeletonItems = Array.from({ length: 5 });

  // Filter states
  searchTerm = '';
  selectedContract = '';
  selectedStatus = '';

  private readonly fb = inject(FormBuilder);

  readonly collaborateurForm = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    statut: ['MISSION', Validators.required],
    contrat: ['CDI', Validators.required],
    seniorite: ['JUNIOR', Validators.required],
    salaire: [null as number | null, Validators.required]
  });

  constructor(private readonly service: CollaborateurService, private readonly ui: UiFeedbackService) {}

  ngOnInit(): void {
    this.loadCollaborateurs();
  }

  loadCollaborateurs(): void {
    this.loading = true;
    this.service
      .getAll()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => {
          this.collaborateurs = data;
          this.applyFilters();
        },
        error: () => {
          this.ui.showToast('error', 'Impossible de charger les collaborateurs.');
        }
      });
  }

  onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchTerm = input.value.toLowerCase();
    this.applyFilters();
  }

  onContractChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedContract = select.value;
    this.applyFilters();
  }

  onStatusChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedStatus = select.value;
    this.applyFilters();
  }

  applyFilters(): void {
    this.filteredCollaborateurs = this.collaborateurs.filter((c) => {
      const matchesSearch =
        !this.searchTerm ||
        c.firstName.toLowerCase().includes(this.searchTerm) ||
        c.lastName.toLowerCase().includes(this.searchTerm) ||
        c.email.toLowerCase().includes(this.searchTerm);

      const matchesContract = !this.selectedContract || c.contrat === this.selectedContract;

      let matchesStatus = true;
      if (this.selectedStatus === 'Actif') {
        matchesStatus = c.active;
      } else if (this.selectedStatus === 'Inactif') {
        matchesStatus = !c.active;
      }

      return matchesSearch && matchesContract && matchesStatus;
    });
  }

  getAvatarColor(name: string): string {
    const colors = ['bg-blue', 'bg-purple', 'bg-orange', 'bg-rose', 'bg-emerald'];
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    const index = Math.abs(hash) % colors.length;
    return colors[index];
  }

  openCreateModal(): void {
    this.editMode = false;
    this.selectedId = null;
    this.collaborateurForm.reset({
      firstName: '',
      lastName: '',
      email: '',
      statut: 'MISSION',
      contrat: 'CDI',
      seniorite: 'JUNIOR',
      salaire: null
    });
    this.showModal = true;
  }

  openEditModal(collaborateur: CollaborateurResponse): void {
    this.editMode = true;
    this.selectedId = collaborateur.id;
    this.collaborateurForm.reset({
      firstName: collaborateur.firstName,
      lastName: collaborateur.lastName,
      email: collaborateur.email,
      statut: collaborateur.statut,
      contrat: collaborateur.contrat,
      seniorite: collaborateur.seniorite,
      salaire: collaborateur.salaire
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.saving = false;
  }

  submitForm(): void {
    if (this.collaborateurForm.invalid) {
      this.collaborateurForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const raw = this.collaborateurForm.getRawValue();

    const payload: CollaborateurRequest = {
      firstName: raw.firstName || '',
      lastName: raw.lastName || '',
      email: raw.email || '',
      statut: raw.statut || 'MISSION',
      contrat: raw.contrat || 'CDI',
      seniorite: raw.seniorite || 'JUNIOR',
      salaire: Number(raw.salaire || 0)
    };

    const request$ = this.editMode && this.selectedId ? this.service.update(this.selectedId, payload) : this.service.create(payload);

    request$.pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => {
        this.closeModal();
        this.ui.showToast('success', this.editMode ? 'Collaborateur mis a jour.' : 'Invitation envoyee au collaborateur.');
        this.loadCollaborateurs();
      },
      error: () => {
        this.ui.showToast('error', "Echec de l'enregistrement.");
      }
    });
  }

  toggleActivation(collaborateur: CollaborateurResponse): void {
    this.service.toggleActivation(collaborateur.id).subscribe({
      next: () => {
        this.ui.showToast(
          'success',
          collaborateur.active
            ? 'Compte desactive avec succes.'
            : 'Compte active et lien d activation envoye au collaborateur.'
        );
        this.loadCollaborateurs();
      },
      error: () => {
        this.ui.showToast('error', 'Impossible de changer le statut du compte.');
      }
    });
  }
}
