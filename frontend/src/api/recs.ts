import { api } from './client';
import { Page, Rec, StatusHistory } from '../types';

export async function searchRecs(energySource?: string, vintageYear?: string, status?: string) {
  const params: any = { page: 0, size: 50 };
  if (energySource) params.energySource = energySource;
  if (vintageYear) params.vintageYear = Number(vintageYear);
  if (status) params.status = status;
  const res = await api.get<Page<Rec>>('/recs', { params });
  return res.data;
}

export async function updateRecStatus(id: number, status: string, comment?: string) {
  const res = await api.patch<Rec>(`/recs/${id}/status`, { status, comment });
  return res.data;
}

export async function purchaseRec(id: number) {
  const res = await api.post<Rec>(`/recs/${id}/purchase`);
  return res.data;
}

export async function getRecHistory(id: number) {
  const res = await api.get<StatusHistory[]>(`/recs/${id}/history`);
  return res.data;
}
