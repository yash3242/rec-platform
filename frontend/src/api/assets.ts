import { api } from './client';
import { Asset, AssetRequest, Page } from '../types';

export async function searchAssets(energySource?: string, status?: string) {
  const params: any = { page: 0, size: 50 };
  if (energySource) params.energySource = energySource;
  if (status) params.status = status;
  const res = await api.get<Page<Asset>>('/assets', { params });
  return res.data;
}

export async function createAsset(request: AssetRequest) {
  const res = await api.post<Asset>('/assets', request);
  return res.data;
}

export async function updateAssetStatus(id: number, status: string, comment?: string) {
  const res = await api.patch<Asset>(`/assets/${id}/status`, { status, comment });
  return res.data;
}
