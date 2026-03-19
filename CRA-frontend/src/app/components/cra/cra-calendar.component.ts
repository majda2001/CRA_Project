import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CraService } from '../../services/cra.service';
import { StatusBadgeComponent } from '../shared/status-badge.component';
import { ActivityType, CraDto, CraConfiguration } from '../../models/cra.model';
import { format, startOfMonth, endOfMonth, eachDayOfInterval, isWeekend } from 'date-fns';
import { fr } from 'date-fns/locale';
import { UiFeedbackService } from '../../services/ui-feedback.service';

interface DayActivity {
  date: Date;
  activity?: {
    date: string;
    duree: number;
    type: ActivityType;
    missionId?: number;
    missionName?: string;
  };
  isWeekend: boolean;
  dayNumber: number;
}

@Component({
  selector: 'app-cra-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatIconModule, StatusBadgeComponent],
  templateUrl: './cra-calendar.component.html',
  styleUrls: ['./cra-calendar.component.css']
})
export class CraCalendarComponent implements OnInit {
  currentDate = new Date();
  monthName = '';
  currentYear = 0;
  cra: CraDto | null = null;
  config: CraConfiguration = { windowStartDay: 17, windowEndDay: 28 }; // Default values

  calendarDays: DayActivity[] = [];
  emptyDays: unknown[] = [];
  weekDays = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

  showDayModal = false;
  savingDay = false;
  selectedDay: DayActivity | null = null;
  selectedDateLabel = '';

  editType: ActivityType = 'CONGE';
  editDuree = 1;

  readonly activityTypes: ActivityType[] = ['CONGE', 'RTT', 'MALADIE'];

  constructor(private readonly craService: CraService, private readonly ui: UiFeedbackService) {}

  ngOnInit(): void {
    this.craService.getConfig().subscribe(config => {
      this.config = config;
    });
    this.loadCra();
  }

  changeMonth(delta: number): void {
    this.currentDate.setMonth(this.currentDate.getMonth() + delta);
    this.currentDate = new Date(this.currentDate);
    this.loadCra();
  }

  loadCra(): void {
    const mois = this.currentDate.getMonth() + 1;
    const annee = this.currentDate.getFullYear();

    this.monthName = format(this.currentDate, 'MMMM', { locale: fr });
    this.currentYear = annee;

    const firstDay = startOfMonth(this.currentDate);
    let startDayOfWeek = firstDay.getDay() - 1;
    if (startDayOfWeek === -1) {
      startDayOfWeek = 6;
    }

    this.emptyDays = new Array(startDayOfWeek);

    this.craService.getCurrent(mois, annee).subscribe({
      next: (cra) => {
        this.cra = cra;
        this.buildCalendar();
      },
      error: (err) => {
        if (err.status === 404) {
          this.ui.showToast('warning', 'Utilisateur introuvable. Veuillez vous reconnecter.');
        } else {
          this.ui.showToast('error', 'Impossible de charger le CRA du mois.');
        }
      }
    });
  }

  buildCalendar(): void {
    const start = startOfMonth(this.currentDate);
    const end = endOfMonth(this.currentDate);
    const days = eachDayOfInterval({ start, end });

    this.calendarDays = days.map((date: Date) => {
      const dateStr = format(date, 'yyyy-MM-dd');
      const activity = this.cra?.activities.find((a) => a.date === dateStr);

      return {
        date,
        activity,
        isWeekend: isWeekend(date),
        dayNumber: date.getDate()
      };
    });
  }

  canSubmit(): boolean {
    return (
      !!this.cra &&
      (this.cra.status === 'DRAFT' || this.cra.status === 'REJECTED' || this.cra.status === 'INVALIDATED') &&
      this.isInDeclarationWindow()
    );
  }

  canEdit(): boolean {
    if (!this.cra || this.cra.status === 'APPROVED' || this.cra.status === 'SUBMITTED') {
      return false;
    }
    
    // Editable if in declaration window (17-28) OR if user has INVALIDATED status (meaning correction required)
    // REJECTED status does NOT grant an exception outside the window.
    const isInvalidated = this.cra.status === 'INVALIDATED';
    
    if (this.isInDeclarationWindow() || isInvalidated) {
      return true;
    }
    
    return false;
  }

  isInDeclarationWindow(): boolean {
    const day = new Date().getDate();
    // Use dynamic configuration or fallback to defaults
    return day >= this.config.windowStartDay && day <= this.config.windowEndDay;
  }

  fillWholeMonthOneClick(): void {
    if (!this.cra || !this.canEdit()) {
      return;
    }

    const payload = this.calendarDays
      .filter((day) => !day.isWeekend)
      .map((day) => ({
        date: format(day.date, 'yyyy-MM-dd'),
        duree: 1,
        type: 'MISSION' as const,
        missionId: null
      }));

    this.craService.updateActivities(this.cra.id, payload).subscribe({
      next: (updated) => {
        this.cra = updated;
        this.buildCalendar();
        this.ui.showToast('success', 'Mois complet pre-rempli. Mission active selon affectation admin, sinon intercontrat.');
      },
      error: (err) => {
        this.ui.showToast('error', err?.error?.message || 'Impossible de pre-remplir le mois.');
      }
    });
  }

  async submitCra(): Promise<void> {
    if (!this.cra) {
      return;
    }

    const confirmed = await this.ui.confirm({
      title: 'Soumettre le CRA ?',
      message: "Apres soumission, vous ne pourrez plus le modifier tant qu'il n'est pas rejete.",
      confirmLabel: 'Soumettre'
    });

    if (!confirmed) {
      return;
    }

    this.craService.submit(this.cra.id).subscribe({
      next: (updated) => {
        this.cra = updated;
        this.buildCalendar();
        this.ui.showToast('success', 'CRA soumis avec succes.');
      },
      error: (err) => {
        this.ui.showToast('error', err?.error?.message || 'Echec de soumission du CRA.');
      }
    });
  }

  openDayModal(day: DayActivity): void {
    if (!this.canEdit()) {
      return;
    }

    this.selectedDay = day;
    this.selectedDateLabel = format(day.date, 'EEEE dd MMMM yyyy', { locale: fr });
    this.editType = this.isAbsenceType(day.activity?.type) ? day.activity!.type : 'CONGE';
    this.editDuree = day.activity?.duree ?? 1;
    this.showDayModal = true;
  }

  private isAbsenceType(type?: ActivityType): type is 'CONGE' | 'RTT' | 'MALADIE' {
    return type === 'CONGE' || type === 'RTT' || type === 'MALADIE';
  }

  closeDayModal(): void {
    this.showDayModal = false;
    this.savingDay = false;
    this.selectedDay = null;
  }

  saveDay(): void {
    if (!this.cra || !this.selectedDay) {
      return;
    }

    if (this.editDuree < 0 || this.editDuree > 1) {
      this.ui.showToast('error', 'La duree doit etre comprise entre 0 et 1 jour.');
      return;
    }

    const payload = [
      {
        date: format(this.selectedDay.date, 'yyyy-MM-dd'),
        duree: Number(this.editDuree),
        type: this.editType,
        missionId: null
      }
    ];

    this.savingDay = true;
    this.craService.updateActivities(this.cra.id, payload).subscribe({
      next: (updated) => {
        this.cra = updated;
        this.buildCalendar();
        this.closeDayModal();
        this.ui.showToast('success', 'Journee mise a jour.');
      },
      error: (err) => {
        this.savingDay = false;
        this.ui.showToast('error', err?.error?.message || 'Impossible de mettre a jour cette journee.');
      }
    });
  }
}
