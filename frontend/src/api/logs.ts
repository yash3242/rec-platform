import { api } from './client';
import { GenerationLog, GenerationLogRequest, Page } from '../types';

export async function searchLogs(energySource?: string, vintageYear?: string, status?: string) {
  const params: any = { page: 0, size: 50 };
  if (energySource) params.energySource = energySource;
  if (vintageYear) params.vintageYear = Number(vintageYear);
  if (status) params.status = status;
  const res = await api.get<Page<GenerationLog>>('/generation-logs', { params });
  return res.data;
}

export async function createLog(request: GenerationLogRequest) {
  const res = await api.post<GenerationLog>('/generation-logs', request);
  return res.data;
}

export async function updateLogStatus(id: number, status: string, comment?: string) {
  const res = await api.patch<GenerationLog>(`/generation-logs/${id}/status`, { status, comment });
  return res.data;
}
