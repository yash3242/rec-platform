import { FormEvent, useEffect, useState } from 'react';
import { Asset, EnergySource } from '../types';
import { createAsset, searchAssets, updateAssetStatus } from '../api/assets';
import { useAuth } from '../context/AuthContext';

export default function AssetsPage() {
  const { user } = useAuth();
  const [assets, setAssets] = useState<Asset[]>([]);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    assetCode: '',
    name: '',
    energySource: 'SOLAR' as EnergySource,
    location: '',
    capacityMw: '',
  });

  async function load() {
    const data = await searchAssets();
    setAssets(data.content);
  }

  useEffect(() => { load(); }, []);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setError('');
    try {
      await createAsset({
        assetCode: form.assetCode,
        name: form.name,
        energySource: form.energySource,
        location: form.location || undefined,
        capacityMw: form.capacityMw ? Number(form.capacityMw) : undefined,
      });
      setForm({ assetCode: '', name: '', energySource: 'SOLAR', location: '', capacityMw: '' });
      await load();
    } catch (err: any) {
      setError(`Could not create asset: ${err?.response?.data?.message ?? err?.message ?? 'unknown'}`);
    }
  }

  async function setStatus(id: number, status: string) {
    await updateAssetStatus(id, status, status === 'ACTIVE' ? 'Verified by admin' : 'Suspended by admin');
    await load();
  }

  return (
    <div>
      <h2>Assets</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <div className="card">
        <h3>Create Asset</h3>
        <form onSubmit={handleCreate}>
          <div className="form-grid">
            <input id="assetCode" placeholder="Asset code" value={form.assetCode} onChange={(e) => setForm({ ...form, assetCode: e.target.value })} required />
            <input id="assetName" placeholder="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            <select id="assetSource" value={form.energySource} onChange={(e) => setForm({ ...form, energySource: e.target.value as EnergySource })}>
              {['SOLAR','WIND','HYDRO','BIOMASS','GEOTHERMAL','OTHER'].map(s => <option key={s}>{s}</option>)}
            </select>
            <input id="assetLocation" placeholder="Location" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} />
            <input id="assetCapacity" type="number" step="0.01" placeholder="Capacity MW" value={form.capacityMw} onChange={(e) => setForm({ ...form, capacityMw: e.target.value })} />
          </div>
          <button id="createAssetButton" className="btn" type="submit" style={{ marginTop: 12 }}>Create Asset</button>
        </form>
      </div>

      <div className="card">
        <h3>Asset List</h3>
        <table id="assetTable">
          <thead><tr><th>Code</th><th>Name</th><th>Source</th><th>Capacity</th><th>Status</th><th>Owner</th><th>Actions</th></tr></thead>
          <tbody>
            {assets.map((asset) => (
              <tr key={asset.id}>
                <td>{asset.assetCode}</td>
                <td>{asset.name}</td>
                <td>{asset.energySource}</td>
                <td>{asset.capacityMw ?? '-'}</td>
                <td>{asset.status}</td>
                <td>{asset.ownerName}</td>
                <td>
                  {user?.role === 'ADMIN' && asset.status === 'PENDING_VERIFICATION' && (
                    <button id={`verifyAsset-${asset.id}`} className="btn" onClick={() => setStatus(asset.id, 'ACTIVE')}>Verify</button>
                  )}
                  {user?.role === 'ADMIN' && asset.status === 'ACTIVE' && (
                    <button id={`suspendAsset-${asset.id}`} className="btn" onClick={() => setStatus(asset.id, 'SUSPENDED')}>Suspend</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
