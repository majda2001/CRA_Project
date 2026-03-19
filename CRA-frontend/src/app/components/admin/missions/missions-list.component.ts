import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { finalize } from 'rxjs';
import { MissionRequest, MissionResponse, MissionService } from '../../../services/mission.service';
import { UiFeedbackService } from '../../../services/ui-feedback.service';

@Component({
  selector: 'app-missions-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule],
  templateUrl: './missions-list.component.html',
  styleUrls: ['./missions-list.component.css']
})
export class MissionsListComponent implements OnInit {
  missions: MissionResponse[] = [];
  filteredMissions: MissionResponse[] = [];
  loading = true;
  saving = false;
  showModal = false;
  editMode = false;
  selectedId: number | null = null;
  skeletonItems = Array.from({ length: 4 });

  private readonly fb = inject(FormBuilder);

  readonly missionForm = this.fb.group({
    titre: ['', Validators.required],
    description: ['', Validators.required],
    dateDebut: ['', Validators.required],
    dateFin: [''],
    motif: [''],
    tjm: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  constructor(private readonly service: MissionService, private readonly ui: UiFeedbackService) {}

  ngOnInit(): void {
    this.loadMissions();
  }

  loadMissions(): void {
    this.loading = true;
    this.service
      .getAll()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => {
          this.missions = data;
          this.filteredMissions = data;
        },
        error: () => {
          this.ui.showToast('error', 'Impossible de charger les missions.');
        }
      });
  }

  filter(event: Event): void {
    const input = event.target as HTMLInputElement;
    const query = input.value.toLowerCase();
    this.filteredMissions = this.missions.filter(
      (m) => m.titre.toLowerCase().includes(query) || m.description.toLowerCase().includes(query)
    );
  }

  openCreateModal(): void {
    this.editMode = false;
    this.selectedId = null;
    this.missionForm.reset({
      titre: '',
      description: '',
      dateDebut: '',
      dateFin: '',
      motif: '',
      tjm: null
    });
    this.showModal = true;
  }

  openEditModal(mission: MissionResponse): void {
    this.editMode = true;
    this.selectedId = mission.id;
    this.missionForm.reset({
      titre: mission.titre,
      description: mission.description,
      dateDebut: mission.dateDebut,
      dateFin: mission.dateFin || '',
      motif: mission.motif || '',
      tjm: mission.tjm
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.saving = false;
  }

  submitForm(): void {
    if (this.missionForm.invalid) {
      this.missionForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const raw = this.missionForm.getRawValue();

    const payload: MissionRequest = {
      titre: raw.titre || '',
      description: raw.description || '',
      dateDebut: raw.dateDebut || '',
      dateFin: raw.dateFin || null,
      motif: raw.motif || '',
      tjm: Number(raw.tjm || 0)
    };

    const request$ = this.editMode && this.selectedId
      ? this.service.update(this.selectedId, payload)
      : this.service.create(payload);

    request$.pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => {
        this.closeModal();
        this.ui.showToast('success', this.editMode ? 'Mission mise a jour.' : 'Mission creee.');
        this.loadMissions();
      },
      error: () => {
        this.ui.showToast('error', "Echec de l'enregistrement de la mission.");
      }
    });
  }

  async deleteMission(mission: MissionResponse): Promise<void> {
    const confirmed = await this.ui.confirm({
      title: 'Supprimer cette mission ?',
      message: `La mission "${mission.titre}" sera supprimee definitivement.`
    });

    if (!confirmed) {
      return;
    }

    this.service.delete(mission.id).subscribe({
      next: () => {
        this.ui.showToast('success', 'Mission supprimee.');
        this.loadMissions();
      },
      error: () => {
        this.ui.showToast('error', 'Impossible de supprimer cette mission.');
      }
    });
  }
}
