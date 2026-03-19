import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { finalize } from 'rxjs';
import {
  AssignmentRequest,
  AssignmentResponse,
  AssignmentService
} from '../../../services/assignment.service';
import { MissionResponse, MissionService } from '../../../services/mission.service';
import {
  CollaborateurResponse,
  CollaborateurService
} from '../../../services/collaborateur.service';
import { UiFeedbackService } from '../../../services/ui-feedback.service';

@Component({
  selector: 'app-assignments-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule],
  templateUrl: './assignments-list.component.html',
  styleUrls: ['./assignments-list.component.css']
})
export class AssignmentsListComponent implements OnInit {
  assignments: AssignmentResponse[] = [];
  filteredAssignments: AssignmentResponse[] = [];
  missions: MissionResponse[] = [];
  collaborateurs: CollaborateurResponse[] = [];

  loading = true;
  saving = false;
  showModal = false;
  editMode = false;
  selectedId: number | null = null;
  skeletonItems = Array.from({ length: 4 });

  private readonly fb = inject(FormBuilder);

  readonly assignmentForm = this.fb.group({
    collaborateurId: [null as number | null, Validators.required],
    missionId: [null as number | null, Validators.required]
  });

  constructor(
    private readonly assignmentService: AssignmentService,
    private readonly missionService: MissionService,
    private readonly collaborateurService: CollaborateurService,
    private readonly ui: UiFeedbackService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;

    this.missionService.getAll().subscribe({
      next: (m) => {
        this.missions = m;
      }
    });

    this.collaborateurService.getAll().subscribe({
      next: (c) => {
        this.collaborateurs = c;
      }
    });

    this.assignmentService
      .getAll()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => {
          this.assignments = data;
          this.filteredAssignments = data;
        },
        error: () => {
          this.assignments = [];
          this.filteredAssignments = [];
          this.ui.showToast('error', 'Erreur de chargement des affectations');
        }
      });
  }

  filter(event: Event): void {
    const query = (event.target as HTMLInputElement).value.toLowerCase();
    this.filteredAssignments = this.assignments.filter(
      (a) =>
        a.collaborateurName.toLowerCase().includes(query) ||
        a.missionTitle.toLowerCase().includes(query)
    );
  }

  filterStatus(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    if (!value) {
      this.filteredAssignments = [...this.assignments];
      return;
    }

    if (value === 'active') {
      this.filteredAssignments = this.assignments.filter((a) => !a.dateFin);
      return;
    }

    this.filteredAssignments = this.assignments.filter((a) => !!a.dateFin);
  }

  openCreateModal(): void {
    this.editMode = false;
    this.selectedId = null;
    this.assignmentForm.reset({
      collaborateurId: null,
      missionId: null
    });
    this.showModal = true;
  }

  openEditModal(assignment: AssignmentResponse): void {
    this.editMode = true;
    this.selectedId = assignment.id;
    this.assignmentForm.patchValue({
      collaborateurId: assignment.collaborateurId,
      missionId: assignment.missionId
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  submitForm(): void {
    if (this.assignmentForm.invalid) {
      this.assignmentForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const raw = this.assignmentForm.getRawValue();

    const payload: AssignmentRequest = {
      collaborateurId: Number(raw.collaborateurId),
      missionId: Number(raw.missionId)
    };

    const req$ = this.editMode && this.selectedId
      ? this.assignmentService.update(this.selectedId, payload)
      : this.assignmentService.create(payload);

    req$.pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => {
        this.closeModal();
        this.ui.showToast('success', this.editMode ? 'Affectation mise a jour' : 'Nouvelle affectation creee');
        this.loadData();
      },
      error: () => this.ui.showToast('error', "Erreur lors de l'enregistrement")
    });
  }

  async deleteAssignment(assignment: AssignmentResponse): Promise<void> {
    const confirmed = await this.ui.confirm({
      title: 'Supprimer ?',
      message: 'Cette affectation sera supprimee.'
    });

    if (!confirmed) {
      return;
    }

    this.assignmentService.delete(assignment.id).subscribe({
      next: () => {
        this.ui.showToast('success', 'Affectation supprimee');
        this.loadData();
      },
      error: () => this.ui.showToast('error', 'Suppression impossible')
    });
  }

  getInitials(name: string): string {
    if (!name) {
      return '??';
    }
    const parts = name.trim().split(' ').filter(Boolean);
    if (parts.length === 1) {
      return parts[0].slice(0, 2).toUpperCase();
    }
    return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
  }
}
