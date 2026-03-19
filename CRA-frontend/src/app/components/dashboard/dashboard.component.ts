import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth.service';
import { CraService } from '../../services/cra.service';
import { CollaborateurService } from '../../services/collaborateur.service';
import { RoleLabelPipe } from '../../pipes/role-label.pipe';
import { CraDto } from '../../models/cra.model';
import { Observable, combineLatest, map, of } from 'rxjs';
import { StatusBadgeComponent } from '../shared/status-badge.component';

interface DashboardStats {
  totalCollaborators: number;
  crasSubmitted: number;
  pendingValidation: number;
  approvalRate: number;
  statusDistribution: { status: string; count: number; color: string }[];
  recentActivity: CraDto[];
  monthlyTrend: { month: string; submitted: number; approved: number; total: number }[];
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RoleLabelPipe, RouterLink, MatIconModule, StatusBadgeComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private craService = inject(CraService);
  private collaborateurService = inject(CollaborateurService);
  private router = inject(Router);

  vm$!: Observable<{ user: any; stats: DashboardStats }>;

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (user?.role === 'COLLABORATEUR') {
      this.router.navigate(['/cra']);
      return;
    }

    const collaborators$ = user?.role === 'ADMIN' ? this.collaborateurService.getAll().pipe(map((list) => list.length)) : of(0);

    const cras$ = this.craService.getAll();

    this.vm$ = combineLatest([of(user), collaborators$, cras$]).pipe(
      map(([u, totalColabs, allCras]) => {
        const currentMonth = new Date().getMonth() + 1;
        const currentYear = new Date().getFullYear();

        const thisMonthCras = allCras.filter((c) => c.mois === currentMonth && c.annee === currentYear);
        const submittedCount = thisMonthCras.filter((c) => c.status === 'SUBMITTED' || c.status === 'APPROVED').length;
        const pendingCount = allCras.filter((c) => c.status === 'SUBMITTED').length;

        const approvedTotal = allCras.filter((c) => c.status === 'APPROVED').length;
        const rejectedTotal = allCras.filter((c) => c.status === 'REJECTED').length;
        const totalDecided = approvedTotal + rejectedTotal;
        const approvalRate = totalDecided > 0 ? Math.round((approvedTotal / totalDecided) * 100) : 0;

        const statuses = ['DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'INVALIDATED'];
        const colors: Record<string, string> = {
          DRAFT: '#94a3b8',
          SUBMITTED: '#3b82f6',
          APPROVED: '#10b981',
          REJECTED: '#ef4444',
          INVALIDATED: '#f59e0b'
        };

        const distribution = statuses
          .map((s) => ({
            status: s,
            count: thisMonthCras.filter((c) => c.status === s).length,
            color: colors[s]
          }))
          .filter((i) => i.count > 0);

        const monthlyTrend: { month: string; submitted: number; approved: number; total: number }[] = [];
        for (let i = 5; i >= 0; i--) {
          const d = new Date();
          d.setMonth(d.getMonth() - i);
          const m = d.getMonth() + 1;
          const y = d.getFullYear();

          const monthCras = allCras.filter((c) => c.mois === m && c.annee === y);
          const submitted = monthCras.filter((c) => c.status === 'SUBMITTED').length;
          const approved = monthCras.filter((c) => c.status === 'APPROVED').length;
          monthlyTrend.push({
            month: d.toLocaleString('default', { month: 'short' }),
            submitted,
            approved,
            total: monthCras.length
          });
        }

        const recentActivity = [...allCras].sort((a, b) => b.id - a.id).slice(0, 5);

        return {
          user: u,
          stats: {
            totalCollaborators: totalColabs,
            crasSubmitted: submittedCount,
            pendingValidation: pendingCount,
            approvalRate,
            statusDistribution: distribution,
            recentActivity,
            monthlyTrend
          }
        };
      })
    );
  }

  getDonutGradient(distribution: { count: number; color: string }[]): string {
    if (!distribution || distribution.length === 0) {
      return 'background: #e2e8f0';
    }

    const total = distribution.reduce((sum, item) => sum + item.count, 0);
    let gradient = 'conic-gradient(';
    let currentAngle = 0;

    distribution.forEach((item, index) => {
      const angle = (item.count / total) * 360;
      gradient += `${item.color} ${currentAngle}deg ${currentAngle + angle}deg`;
      currentAngle += angle;
      if (index < distribution.length - 1) {
        gradient += ', ';
      }
    });

    gradient += ')';
    return `background: ${gradient}`;
  }

  hasTrendData(monthlyTrend: { submitted: number; approved: number; total: number }[]): boolean {
    return monthlyTrend.some((month) => month.total > 0 || month.submitted > 0 || month.approved > 0);
  }

  getTrendScale(monthlyTrend: { submitted: number; approved: number; total: number }[]): number {
    const maxValue = Math.max(0, ...monthlyTrend.map((month) => Math.max(month.submitted, month.approved, month.total)));
    return maxValue > 0 ? maxValue : 1;
  }

  getBarHeight(value: number, max: number): number {
    if (value <= 0) {
      return 0;
    }
    return Math.max(12, (value / max) * 100);
  }
}
