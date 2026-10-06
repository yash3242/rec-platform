import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getDashboardSummary } from '../api/dashboard';
import { DashboardSummary } from '../types';

export default function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);

  useEffect(() => {
    getDashboardSummary().then(setSummary).catch(() => setSummary(null));
  }, []);

  if (!summary) return <p>Loading dashboard...</p>;

  return (
    <div>
      <h2>Dashboard</h2>
      <div className="grid">
        <div className="stat">Total RECs<strong>{summary.totalRecs}</strong></div>
        <div className="stat">Total Energy MWh<strong>{summary.totalEnergyMwh ?? 0}</strong></div>
        <div className="stat">Certificates<strong>{summary.totalCertificateQuantity ?? 0}</strong></div>
        {Object.entries(summary.statusCounts).map(([status, count]) => (
          <div className="stat" key={status}>{status.replace('_', ' ')}<strong>{count}</strong></div>
        ))}
      </div>

      <div className="card">
        <h3>Energy source breakdown</h3>
        <ul>
          {Object.entries(summary.energySourceCounts).map(([source, count]) => (
            <li key={source}>{source}: {count}</li>
          ))}
        </ul>
      </div>

      <div className="card">
        <h3>Recent records</h3>
        <table>
          <thead>
            <tr><th>REC ID</th><th>Producer</th><th>Source</th><th>Status</th><th>Certificates</th></tr>
          </thead>
          <tbody>
            {summary.recentRecs.map((rec) => (
              <tr key={rec.id}>
                <td><Link to={`/recs/${rec.id}`}>{rec.recCode}</Link></td>
                <td>{rec.producerName}</td>
                <td>{rec.energySource}</td>
                <td>{rec.status}</td>
                <td>{rec.certificateQuantity}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
