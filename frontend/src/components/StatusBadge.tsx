import { RecStatus } from '../types';

export default function StatusBadge({ status }: { status: RecStatus }) {
  return <span className={`badge status-${status.toLowerCase()}`}>{status.replace('_', ' ')}</span>;
}
