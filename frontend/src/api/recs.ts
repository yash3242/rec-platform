import { api } from './client';
import { Page, Rec, RecRequest, StatusHistory } from '../types';

export interface RecSearchParams {
  recCode?: string;
  producerId?: number;
  energySource?: string;
  status?: string;
  startFrom?: string;
  endTo?: string;
  minCertQty?: number;
  maxCertQty?: number;
  page?: number;
  size?: number;
  sort?: string;
}

export async function searchRecs(params: RecSearchParams) {
  const res = await api.get<Page<Rec>>('/recs', { params });
  return res.data;
}

export async function getRec(id: number) {
  const res = await api.get<Rec>(`/recs/${id}`);
  return res.data;
}

export async function createRec(request: RecRequest) {
  const res = await api.post<Rec>('/recs', request);
  return res.data;
}

export async function updateRec(id: number, request: RecRequest) {
  const res = await api.put<Rec>(`/recs/${id}`, request);
  return res.data;
}

export async function changeRecStatus(id: number, status: string, comment?: string) {
  const res = await api.patch<Rec>(`/recs/${id}/status`, { status, comment });
  return res.data;
}

export async function getRecHistory(id: number) {
  const res = await api.get<StatusHistory[]>(`/recs/${id}/history`);
  return res.data;
}
