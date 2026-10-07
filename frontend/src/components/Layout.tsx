import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const { user, logout } = useAuth();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h1>REC Platform</h1>
        <nav>
          {user?.role === 'ADMIN' && <Link to="/dashboard">Dashboard</Link>}
          {(user?.role === 'GENERATOR' || user?.role === 'ADMIN') && <Link to="/assets">Assets</Link>}
          {(user?.role === 'GENERATOR' || user?.role === 'ADMIN') && <Link to="/generation-logs">Generation Logs</Link>}
          {(user?.role === 'BUYER' || user?.role === 'ADMIN' || user?.role === 'GENERATOR') && <Link to="/recs">RECs</Link>}
        </nav>
        <div className="user-box">
          <strong>{user?.fullName}</strong>
          <span>{user?.role}</span>
          <button id="logoutButton" onClick={logout}>Logout</button>
        </div>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
