import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const { user, logout } = useAuth();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h1>REC Platform</h1>
        <nav>
          <Link to="/dashboard">Dashboard</Link>
          <Link to="/recs">RECs</Link>
          {(user?.role === 'PRODUCER' || user?.role === 'ADMIN') && <Link to="/recs/new">New REC</Link>}
        </nav>
        <div className="user-box">
          <strong>{user?.fullName}</strong>
          <span>{user?.role}</span>
          <button onClick={logout}>Logout</button>
        </div>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
