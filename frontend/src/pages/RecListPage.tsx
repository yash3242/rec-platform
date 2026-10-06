import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { searchRecs } from '../api/recs';
import { Page, Rec } from '../types';
import StatusBadge from '../components/StatusBadge';

export default function RecListPage() {
  const [data, setData] = useState<Page<Rec> | null>(null);
  const [filters, setFilters] = useState<any>({});

  async function load(nextFilters = filters) {
    const params = Object.fromEntries(Object.entries(nextFilters).filter(([, v]) => v !== '' && v !== undefined && v !== null));
    const page = await searchRecs({ ...params, page: 0, size: 20 });
    setData(page);
  }

  useEffect(() => { load({}); }, []);

  function updateFilter(key: string, value: any) {
    setFilters((prev: any) => ({ ...prev, [key]: value }));
  }

  return (
    <div>
      <h2>REC Records</h2>
      <div className="card">
        <div className="filters">
          <input placeholder="REC code" onChange={(e) => updateFilter('recCode', e.target.value)} />
          <select onChange={(e) => updateFilter('energySource', e.target.value)}>
            <option value="">All sources</option>
            <option>SOLAR</option><option>WIND</option><option>HYDRO</option><option>BIOMASS</option><option>GEOTHERMAL</option><option>OTHER</option>
          </select>
          <select onChange={(e) => updateFilter('status', e.target.value)}>
            <option value="">All statuses</option>
            <option>CREATED</option><option>SUBMITTED</option><option>UNDER_REVIEW</option><option>APPROVED</option><option>REJECTED</option><option>ISSUED</option><option>RETIRED</option>
          </select>
          <input type="date" placeholder="Start from" onChange={(e) => updateFilter('startFrom', e.target.value)} />
          <input type="date" placeholder="End to" onChange={(e) => updateFilter('endTo', e.target.value)} />
          <input type="number" placeholder="Min certificates" onChange={(e) => updateFilter('minCertQty', e.target.value)} />
          <input type="number" placeholder="Max certificates" onChange={(e) => updateFilter('maxCertQty', e.target.value)} />
        </div>
        <button className="btn" onClick={() => load()}>Search</button>
      </div>

      <div className="card">
        <table>
          <thead>
            <tr><th>REC ID</th><th>Producer</th><th>Source</th><th>Period</th><th>Energy MWh</th><th>Certificates</th><th>Status</th></tr>
          </thead>
          <tbody>
            {data?.content.map((rec) => (
              <tr key={rec.id}>
                <td><Link to={`/recs/${rec.id}`}>{rec.recCode}</Link></td>
                <td>{rec.producerName}</td>
                <td>{rec.energySource}</td>
                <td>{rec.generationStartDate} to {rec.generationEndDate}</td>
                <td>{rec.energyQuantityMwh}</td>
                <td>{rec.certificateQuantity}</td>
                <td><StatusBadge status={rec.status} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
