import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createRec, getRec, updateRec } from '../api/recs';
import { EnergySource, RecRequest } from '../types';
import { useAuth } from '../context/AuthContext';

export default function RecFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [error, setError] = useState('');
  const [form, setForm] = useState<RecRequest>({
    recCode: '',
    producerId: user?.userId ?? 0,
    energySource: 'SOLAR',
    generationStartDate: '',
    generationEndDate: '',
    energyQuantityMwh: 0,
    certificateQuantity: 0,
  });

  useEffect(() => {
    if (id) {
      getRec(Number(id)).then((rec) => setForm({
        recCode: rec.recCode,
        producerId: rec.producerId,
        energySource: rec.energySource,
        generationStartDate: rec.generationStartDate,
        generationEndDate: rec.generationEndDate,
        energyQuantityMwh: rec.energyQuantityMwh,
        certificateQuantity: rec.certificateQuantity,
      }));
    }
  }, [id]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    try {
      if (id) {
        await updateRec(Number(id), form);
      } else {
        await createRec(form);
      }
      navigate('/recs');
    } catch {
      setError('Could not save REC. Check validation and permissions.');
    }
  }

  function update(key: keyof RecRequest, value: any) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  return (
    <div className="card">
      <h2>{id ? 'Edit REC' : 'Create REC'}</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-grid">
          <div><label>REC code</label><input value={form.recCode} onChange={(e) => update('recCode', e.target.value)} required /></div>
          <div><label>Producer ID</label><input type="number" value={form.producerId} onChange={(e) => update('producerId', Number(e.target.value))} required /></div>
          <div><label>Energy source</label>
            <select value={form.energySource} onChange={(e) => update('energySource', e.target.value as EnergySource)}>
              <option>SOLAR</option><option>WIND</option><option>HYDRO</option><option>BIOMASS</option><option>GEOTHERMAL</option><option>OTHER</option>
            </select>
          </div>
          <div><label>Generation start</label><input type="date" value={form.generationStartDate} onChange={(e) => update('generationStartDate', e.target.value)} required /></div>
          <div><label>Generation end</label><input type="date" value={form.generationEndDate} onChange={(e) => update('generationEndDate', e.target.value)} required /></div>
          <div><label>Energy quantity MWh</label><input type="number" step="0.001" value={form.energyQuantityMwh} onChange={(e) => update('energyQuantityMwh', Number(e.target.value))} required /></div>
          <div><label>Certificate quantity</label><input type="number" value={form.certificateQuantity} onChange={(e) => update('certificateQuantity', Number(e.target.value))} required /></div>
        </div>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button className="btn" type="submit">Save</button>
      </form>
    </div>
  );
}
