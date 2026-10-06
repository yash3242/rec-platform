import { api } from './client';
import { DashboardSummary } from '../types';

export async function getDashboardSummary() {
  const res = await api.get<DashboardSummary>('/dashboard/summary');
  return res.data;
}
