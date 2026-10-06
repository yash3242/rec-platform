import { api } from './client';
import { AuthResponse, User } from '../types';

export async function login(email: string, password: string) {
  const res = await api.post<AuthResponse>('/auth/login', { email, password });
  return res.data;
}

export async function register(fullName: string, email: string, password: string) {
  const res = await api.post<AuthResponse>('/auth/register', { fullName, email, password });
  return res.data;
}

export async function me() {
  const res = await api.get<User>('/auth/me');
  return res.data;
}
