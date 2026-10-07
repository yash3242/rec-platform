export type Role = 'ADMIN' | 'GENERATOR' | 'BUYER';
export type EnergySource = 'SOLAR' | 'WIND' | 'HYDRO' | 'BIOMASS' | 'GEOTHERMAL' | 'OTHER';
export type AssetStatus = 'PENDING_VERIFICATION' | 'ACTIVE' | 'SUSPENDED';
export type GenerationLogStatus = 'SUBMITTED' | 'VERIFIED' | 'MINTED' | 'REJECTED';
export type RecStatus = 'ISSUED' | 'LISTED' | 'TRANSFERRED' | 'RETIRED';

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

export interface Asset {
  id: number;
  assetCode: string;
  name: string;
  energySource: EnergySource;
  location: string | null;
  capacityMw: number | null;
  status: AssetStatus;
  ownerId: number;
  ownerName: string;
  createdAt: string;
}

export interface AssetRequest {
  assetCode: string;
  name: string;
  energySource: EnergySource;
  location?: string;
  capacityMw?: number;
}

export interface GenerationLog {
  id: number;
  assetId: number;
  assetCode: string;
  generationDate: string;
  energySource: EnergySource;
  energyQuantityMwh: number;
  vintageYear: number;
  status: GenerationLogStatus;
  createdById: number;
  createdByName: string;
  createdAt: string;
}

export interface GenerationLogRequest {
  assetId: number;
  generationDate: string;
  energySource: EnergySource;
  energyQuantityMwh: number;
  vintageYear: number;
}

export interface Rec {
  id: number;
  recCode: string;
  assetId: number;
  assetCode: string;
  energySource: EnergySource;
  vintageYear: number;
  energyQuantityMwh: number;
  certificateQuantity: number;
  status: RecStatus;
  ownerId: number;
  ownerName: string;
  listedAt: string | null;
  transferredAt: string | null;
  retiredAt: string | null;
  createdAt: string;
}

export interface DashboardSummary {
  totalAssets: number;
  totalGenerationLogs: number;
  totalRecs: number;
  assetStatusCounts: Record<string, number>;
  logStatusCounts: Record<string, number>;
  recStatusCounts: Record<string, number>;
  energySourceCounts: Record<string, number>;
  mintedThisVintageYear: number;
  transferredCount: number;
  retiredCount: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface StatusHistory {
  id: number;
  resourceType: string;
  resourceId: number;
  oldStatus: string | null;
  newStatus: string;
  changedById: number;
  changedByName: string;
  comment: string | null;
  changedAt: string;
}
