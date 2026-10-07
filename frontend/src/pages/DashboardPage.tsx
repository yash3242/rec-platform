import { useEffect, useState } from 'react';
import { getDashboardSummary } from '../api/dashboard';
import { DashboardSummary } from '../types';
import { useAuth } from '../context/AuthContext';

export default function DashboardPage() {
  const { user } = useAuth();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);

  useEffect(() => { getDashboardSummary().then(setSummary); }, []);

  if (!summary) return <p>Loading dashboard...</p>;

  return (
    <div>
      <h2>{user?.role} Dashboard</h2>
      <div className="grid">
        <div className="stat">Total Assets<strong>{summary.totalAssets}</strong></div>
        <div className="stat">Generation Logs<strong>{summary.totalGenerationLogs}</strong></div>
        <div className="stat">RECs<strong>{summary.totalRecs}</strong></div>
        <div className="stat">Minted This Year<strong>{summary.mintedThisVintageYear}</strong></div>
        <div className="stat">Transferred<strong>{summary.transferredCount}</strong></div>
        <div className="stat">Retired<strong>{summary.retiredCount}</strong></div>
      </div>

      <div className="card">
        <h3>REC Status Counts</h3>
        {Object.entries(summary.recStatusCounts).map(([k, v]) => <p key={k}>{k}: {v}</p>)}
      </div>
      <div className="card">
        <h3>Asset Status Counts</h3>
        {Object.entries(summary.assetStatusCounts).map(([k, v]) => <p key={k}>{k}: {v}</p>)}
      </div>
      <div className="card">
        <h3>Generation Log Status Counts</h3>
        {Object.entries(summary.logStatusCounts).map(([k, v]) => <p key={k}>{k}: {v}</p>)}
      </div>
      <div className="card">
        <h3>Energy Source Counts</h3>
        {Object.entries(summary.energySourceCounts).map(([k, v]) => <p key={k}>{k}: {v}</p>)}
      </div>
    </div>
  );
}
