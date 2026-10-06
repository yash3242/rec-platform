import React, { createContext, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthResponse, Role } from '../types';
import { login as loginApi, register as registerApi } from '../api/auth';

interface AuthState {
  user: AuthResponse | null;
  login: (email: string, password: string) => Promise<void>;
  register: (fullName: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthResponse | null>(() => {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
  });
  const navigate = useNavigate();

  useEffect(() => {
    if (user?.token) {
      localStorage.setItem('token', user.token);
      localStorage.setItem('user', JSON.stringify(user));
    }
  }, [user]);

  async function login(email: string, password: string) {
    const response = await loginApi(email, password);
    setUser(response);
    navigate('/dashboard');
  }

  async function register(fullName: string, email: string, password: string) {
    const response = await registerApi(fullName, email, password);
    setUser(response);
    navigate('/dashboard');
  }

  function logout() {
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  }

  return <AuthContext.Provider value={{ user, login, register, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}

export function hasRole(userRole: Role | undefined, roles: Role[]) {
  return !!userRole && roles.includes(userRole);
}
