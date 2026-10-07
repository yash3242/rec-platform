import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const { register } = useAuth();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<'GENERATOR' | 'BUYER'>('GENERATOR');
  const [error, setError] = useState('');

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    try {
      await register(fullName, email, password, role);
    } catch {
      setError('Could not register. Email may already be used.');
    }
  }

  return (
    <div className="auth-page card">
      <h2>Register</h2>
      <form onSubmit={handleSubmit}>
        <label>Full name</label>
        <input id="registerName" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
        <label>Email</label>
        <input id="registerEmail" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <label>Password</label>
        <input id="registerPassword" type="password" value={password} onChange={(e) => setPassword(e.target.value)} minLength={8} required />
        <label>Role</label>
        <select id="registerRole" value={role} onChange={(e) => setRole(e.target.value as any)}>
          <option value="GENERATOR">Generator</option>
          <option value="BUYER">Buyer</option>
        </select>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button id="registerButton" className="btn" type="submit">Register</button>
      </form>
      <p>Already have an account? <Link to="/login">Login</Link></p>
    </div>
  );
}
