import { useEffect, useState } from 'react';
import { Rec } from '../types';
import { purchaseRec, searchRecs, updateRecStatus } from '../api/recs';
import { useAuth } from '../context/AuthContext';

export default function RecsPage() {
  const { user } = useAuth();
  const [recs, setRecs] = useState<Rec[]>([]);
  const [filters, setFilters] = useState({ energySource: '', vintageYear: '', status: '' });

  async function load(next = filters) {
    const data = await searchRecs(next.energySource || undefined, next.vintageYear || undefined, next.status || undefined);
    setRecs(data.content);
  }

  useEffect(() => { load({ energySource: '', vintageYear: '', status: '' }); }, []);

  async function setStatus(id: number, status: string) {
    await updateRecStatus(id, status, status === 'LISTED' ? 'Listed for sale' : status === 'RETIRED' ? 'Retired by buyer' : '');
    await load();
  }

  async function buy(id: number) {
    await purchaseRec(id);
    await load();
  }

  return (
    <div>
      <h2>RECs Marketplace</h2>
      <div className="card">
        <div className="filters">
          <select id="recSourceFilter" value={filters.energySource} onChange={(e) => setFilters({ ...filters, energySource: e.target.value })}>
            <option value="">All sources</option>
            {['SOLAR','WIND','HYDRO','BIOMASS','GEOTHERMAL','OTHER'].map(s => <option key={s}>{s}</option>)}
          </select>
          <input id="recVintageFilter" placeholder="Vintage year" value={filters.vintageYear} onChange={(e) => setFilters({ ...filters, vintageYear: e.target.value })} />
          <select id="recStatusFilter" value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
            <option value="">All statuses</option>
            {['ISSUED','LISTED','TRANSFERRED','RETIRED'].map(s => <option key={s}>{s}</option>)}
          </select>
          <button id="searchRecsButton" className="btn" onClick={() => load()}>Search</button>
        </div>
      </div>

      <div className="card">
        <table id="recTable">
          <thead><tr><th>Code</th><th>Source</th><th>Vintage</th><th>MWh</th><th>Quantity</th><th>Status</th><th>Owner</th><th>Actions</th></tr></thead>
          <tbody>
            {recs.map((rec) => (
              <tr key={rec.id}>
                <td>{rec.recCode}</td>
                <td>{rec.energySource}</td>
                <td>{rec.vintageYear}</td>
                <td>{rec.energyQuantityMwh}</td>
                <td>{rec.certificateQuantity}</td>
                <td>{rec.status}</td>
                <td>{rec.ownerName}</td>
                <td>
                  {user?.role === 'GENERATOR' && rec.status === 'ISSUED' && <button id={`listRec-${rec.id}`} className="btn" onClick={() => setStatus(rec.id, 'LISTED')}>List</button>}
                  {user?.role === 'BUYER' && rec.status === 'LISTED' && <button id={`buyRec-${rec.id}`} className="btn" onClick={() => buy(rec.id)}>Purchase</button>}
                  {user?.role === 'BUYER' && rec.status === 'TRANSFERRED' && <button id={`retireRec-${rec.id}`} className="btn" onClick={() => setStatus(rec.id, 'RETIRED')}>Retire</button>}
                  {user?.role === 'ADMIN' && rec.status === 'ISSUED' && <button id={`adminListRec-${rec.id}`} className="btn" onClick={() => setStatus(rec.id, 'LISTED')}>List</button>}
                  {user?.role === 'ADMIN' && rec.status === 'LISTED' && <button id={`adminBuyRec-${rec.id}`} className="btn" onClick={() => buy(rec.id)}>Purchase</button>}
                  {user?.role === 'ADMIN' && rec.status === 'TRANSFERRED' && <button id={`adminRetireRec-${rec.id}`} className="btn" onClick={() => setStatus(rec.id, 'RETIRED')}>Retire</button>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
