export const UserRole = {
  ADMIN: 'ADMIN',
  COLLABORATEUR: 'COLLABORATEUR'
};

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'ADMIN' | 'COLLABORATEUR';
  statut?: string;
  contrat?: string;
  seniorite?: string;
  salaire?: number;
  active: boolean;
}
