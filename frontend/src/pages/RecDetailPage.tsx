import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { changeRecStatus, getRec, getRecHistory } from '../api/recs';
import { Rec, StatusHistory } from '../types';
import StatusBadge from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';

export default function RecDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const [rec, setRec] = useState<Rec | null>(null);
  const [history, setHistory] = useState<StatusHistory[]>([]);
  const [comment, setComment] = useState('');
  const [error, setError] = useState('');

  async function load() {
    if (!id) return;
    setRec(await getRec(Number(id)));
    setHistory(await getRecHistory(Number(id)));
  }

  useEffect(() => { load().catch(() => setError('Could not load REC')); }, [id]);

  async function updateStatus(status: string) {
    if (!id) return;
    setError('');
    try {
      await changeRecStatus(Number(id), status, comment);
      setComment('');
      await load();
    } catch {
      setError('Could not change status');
    }
  }

  if (error) return <p style={{ color: 'red' }}>{error}</p>;
  if (!rec) return <p>Loading...</p>;

  const canEdit = (user?.role === 'PRODUCER' || user?.role === 'ADMIN') && (rec.status === 'CREATED' || rec.status === 'REJECTED');

  return (
    <div>
      <h2>{rec.recCode}</h2>
      <div className="card">
        <p><strong>Producer:</strong> {rec.producerName}</p>
        <p><strong>Source:</strong> {rec.energySource}</p>
        <p><strong>Period:</strong> {rec.generationStartDate} to {rec.generationEndDate}</p>
        <p><strong>Energy:</strong> {rec.energyQuantityMwh} MWh</p>
        <p><strong>Certificates:</strong> {rec.certificateQuantity}</p>
        <p><strong>Status:</strong> <StatusBadge status={rec.status} /></p>
        {canEdit && <Link className="btn" to={`/recs/${rec.id}/edit`}>Edit</Link>}
      </div>

      <div className="card">
        <h3>Status actions</h3>
        <textarea placeholder="Comment" value={comment} onChange={(e) => setComment(e.target.value)} />
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 8 }}>
          {user?.role === 'PRODUCER' && rec.status === 'CREATED' && <button className="btn" onClick={() => updateStatus('SUBMITTED')}>Submit</button>}
          {user?.role === 'REVIEWER' && rec.status === 'SUBMITTED' && <button className="btn" onClick={() => updateStatus('UNDER_REVIEW')}>Start Review</button>}
          {user?.role === 'REVIEWER' && rec.status === 'UNDER_REVIEW' && <button className="btn" onClick={() => updateStatus('APPROVED')}>Approve</button>}
          {user?.role === 'REVIEWER' && rec.status === 'UNDER_REVIEW' && <button className="btn" onClick={() => updateStatus('REJECTED')}>Reject</button>}
          {user?.role === 'MANAGER' && rec.status === 'APPROVED' && <button className="btn" onClick={() => updateStatus('ISSUED')}>Issue</button>}
          {user?.role === 'MANAGER' && rec.status === 'ISSUED' && <button className="btn" onClick={() => updateStatus('RETIRED')}>Retire</button>}
          {user?.role === 'PRODUCER' && rec.status === 'REJECTED' && <button className="btn" onClick={() => updateStatus('CREATED')}>Return to Draft</button>}
          {user?.role === 'ADMIN' && rec.status === 'CREATED' && <button className="btn" onClick={() => updateStatus('SUBMITTED')}>Submit</button>}
          {user?.role === 'ADMIN' && rec.status === 'SUBMITTED' && <button className="btn" onClick={() => updateStatus('UNDER_REVIEW')}>Start Review</button>}
          {user?.role === 'ADMIN' && rec.status === 'UNDER_REVIEW' && <><button className="btn" onClick={() => updateStatus('APPROVED')}>Approve</button><button className="btn" onClick={() => updateStatus('REJECTED')}>Reject</button></>}
          {user?.role === 'ADMIN' && rec.status === 'APPROVED' && <button className="btn" onClick={() => updateStatus('ISSUED')}>Issue</button>}
          {user?.role === 'ADMIN' && rec.status === 'ISSUED' && <button className="btn" onClick={() => updateStatus('RETIRED')}>Retire</button>}
          {user?.role === 'ADMIN' && rec.status === 'REJECTED' && <button className="btn" onClick={() => updateStatus('CREATED')}>Return to Draft</button>}
        </div>
      </div>

      <div className="card">
        <h3>Status history</h3>
        <table>
          <thead><tr><th>When</th><th>From</th><th>To</th><th>By</th><th>Comment</th></tr></thead>
          <tbody>
            {history.map((h) => (
              <tr key={h.id}><td>{new Date(h.changedAt).toLocaleString()}</td><td>{h.oldStatus ?? '-'}</td><td>{h.newStatus}</td><td>{h.changedByName}</td><td>{h.comment}</td></tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
