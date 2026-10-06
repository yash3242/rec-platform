export type Role = 'ADMIN' | 'PRODUCER' | 'REVIEWER' | 'MANAGER';
export type EnergySource = 'SOLAR' | 'WIND' | 'HYDRO' | 'BIOMASS' | 'GEOTHERMAL' | 'OTHER';
export type RecStatus = 'CREATED' | 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'ISSUED' | 'RETIRED';

export interface AuthResponse {
  token: string;
  userId: number;
  fullName: string;
  email: string;
  role: Role;
}

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  active: boolean;
}

export interface Rec {
  id: number;
  recCode: string;
  producerId: number;
  producerName: string;
  energySource: EnergySource;
  generationStartDate: string;
  generationEndDate: string;
  energyQuantityMwh: number;
  certificateQuantity: number;
  status: RecStatus;
  createdAt: string;
  updatedAt: string;
}

export interface RecRequest {
  recCode: string;
  producerId: number;
  energySource: EnergySource;
  generationStartDate: string;
  generationEndDate: string;
  energyQuantityMwh: number;
  certificateQuantity: number;
}

export interface StatusHistory {
  id: number;
  oldStatus: string | null;
  newStatus: string;
  changedById: number;
  changedByName: string;
  comment: string | null;
  changedAt: string;
}

export interface DashboardSummary {
  totalRecs: number;
  statusCounts: Record<string, number>;
  energySourceCounts: Record<string, number>;
  totalEnergyMwh: number;
  totalCertificateQuantity: number | null;
  recentRecs: Rec[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
