import { FormEvent, useEffect, useState } from 'react';
import { Asset, EnergySource, GenerationLog } from '../types';
import { searchAssets } from '../api/assets';
import { createLog, searchLogs, updateLogStatus } from '../api/logs';
import { useAuth } from '../context/AuthContext';

export default function GenerationLogsPage() {
  const { user } = useAuth();
  const [logs, setLogs] = useState<GenerationLog[]>([]);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    assetId: '',
    generationDate: '',
    energySource: 'SOLAR' as EnergySource,
    energyQuantityMwh: '',
    vintageYear: String(new Date().getFullYear()),
  });

  async function load() {
    const [logData, assetData] = await Promise.all([searchLogs(), searchAssets()]);
    setLogs(logData.content);
    setAssets(assetData.content);
  }

  useEffect(() => { load(); }, []);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setError('');
    try {
      await createLog({
        assetId: Number(form.assetId),
        generationDate: form.generationDate,
        energySource: form.energySource,
        energyQuantityMwh: Number(form.energyQuantityMwh),
        vintageYear: Number(form.vintageYear),
      });
      await load();
    } catch {
      setError('Could not submit generation log. Asset may not be active.');
    }
  }

  async function setStatus(id: number, status: string) {
    await updateLogStatus(id, status, status === 'VERIFIED' ? 'Verified by admin' : status === 'REJECTED' ? 'Rejected by admin' : 'Minted');
    await load();
  }

  return (
    <div>
      <h2>Generation Logs</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <div className="card">
        <h3>Submit Log</h3>
        <form onSubmit={handleCreate}>
          <div className="form-grid">
            <select id="logAssetId" value={form.assetId} onChange={(e) => setForm({ ...form, assetId: e.target.value })} required>
              <option value="">Select active asset</option>
              {assets.filter(a => a.status === 'ACTIVE').map(a => <option key={a.id} value={a.id}>{a.assetCode}</option>)}
            </select>
            <input id="logDate" type="date" value={form.generationDate} onChange={(e) => setForm({ ...form, generationDate: e.target.value })} required />
            <select id="logSource" value={form.energySource} onChange={(e) => setForm({ ...form, energySource: e.target.value as EnergySource })}>
              {['SOLAR','WIND','HYDRO','BIOMASS','GEOTHERMAL','OTHER'].map(s => <option key={s}>{s}</option>)}
            </select>
            <input id="logQuantity" type="number" step="0.001" placeholder="MWh" value={form.energyQuantityMwh} onChange={(e) => setForm({ ...form, energyQuantityMwh: e.target.value })} required />
            <input id="logVintage" type="number" placeholder="Vintage year" value={form.vintageYear} onChange={(e) => setForm({ ...form, vintageYear: e.target.value })} required />
          </div>
          <button id="submitLogButton" className="btn" type="submit" style={{ marginTop: 12 }}>Submit Log</button>
        </form>
      </div>

      <div className="card">
        <h3>Log List</h3>
        <table id="logTable">
          <thead><tr><th>ID</th><th>Asset</th><th>Date</th><th>Source</th><th>MWh</th><th>Vintage</th><th>Status</th><th>Actions</th></tr></thead>
          <tbody>
            {logs.map((log) => (
              <tr key={log.id}>
                <td>{log.id}</td>
                <td>{log.assetCode}</td>
                <td>{log.generationDate}</td>
                <td>{log.energySource}</td>
                <td>{log.energyQuantityMwh}</td>
                <td>{log.vintageYear}</td>
                <td>{log.status}</td>
                <td>
                  {user?.role === 'ADMIN' && log.status === 'SUBMITTED' && <button id={`verifyLog-${log.id}`} className="btn" onClick={() => setStatus(log.id, 'VERIFIED')}>Verify</button>}
                  {user?.role === 'ADMIN' && log.status === 'SUBMITTED' && <button id={`rejectLog-${log.id}`} className="btn" onClick={() => setStatus(log.id, 'REJECTED')}>Reject</button>}
                  {user?.role === 'ADMIN' && log.status === 'VERIFIED' && <button id={`mintLog-${log.id}`} className="btn" onClick={() => setStatus(log.id, 'MINTED')}>Mint REC</button>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
