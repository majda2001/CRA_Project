import { User } from './user.model';

export type ActivityType = 'MISSION' | 'INTERCONTRACT' | 'CONGE' | 'RTT' | 'MALADIE';

export interface Activity {
  id: number;
  date: Date;
  duree: number;
  type: ActivityType;
  missionId?: number;
  missionName?: string;
}

export type CraStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'INVALIDATED';

export interface Cra {
  id: number;
  collaborateur: User;
  mois: number;
  annee: number;
  status: CraStatus;
  dateSoumission?: Date;
  dateValidation?: Date;
  motifRejet?: string;
  activities: Activity[];
}

export interface CraConfiguration {
  windowStartDay: number;
  windowEndDay: number;
}

export interface CraDto {
  id: number;
  collaborateurId?: number;
  collaborateurName?: string;
  collaborateurEmail?: string;
  mois: number;
  annee: number;
  status: CraStatus;
  dateSoumission?: string;
  dateValidation?: string;
  motifRejet?: string;
  activities: {
      date: string;
      duree: number;
      type: ActivityType;
      missionId?: number;
      missionName?: string;
  }[];
}
