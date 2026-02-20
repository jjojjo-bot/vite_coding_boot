import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  if (!user) return null;

  return (
    <>
      <div className="header">
        <div className="header-left">
          <img
            src="https://infra1-static.recruiter.co.kr/builder/2024/01/26/9f60ffa1-8560-4a34-a76c-863bc2dac21c.png"
            alt="CI"
            className="header-logo"
          />
          <h1>과제관리 시스템</h1>
        </div>
        <div className="header-right">
          <span>{user.name} ({user.role})</span>
          <a href="#" onClick={(e) => { e.preventDefault(); handleLogout(); }}>로그아웃</a>
        </div>
      </div>
      <div className="layout">
        <nav className="sidebar">
          <ul className="sidebar-menu">
            <li><NavLink to="/dashboard" className={({ isActive }) => isActive ? 'active' : ''}>대시보드</NavLink></li>
            <li><NavLink to="/approval-history" className={({ isActive }) => isActive ? 'active' : ''}>결재함</NavLink></li>
            <li><NavLink to="/otp-settings" className={({ isActive }) => isActive ? 'active' : ''}>OTP 설정</NavLink></li>
            {user.role === 'LEADER' && (
              <>
                <li style={{ borderTop: '1px solid #e5e8eb', margin: '8px 0' }}></li>
                <li style={{ padding: '4px 24px', fontSize: '11px', color: '#999', fontWeight: 600 }}>관리자</li>
                <li><NavLink to="/admin/users" className={({ isActive }) => isActive ? 'active' : ''}>사용자관리</NavLink></li>
                <li><NavLink to="/admin/teams" className={({ isActive }) => isActive ? 'active' : ''}>부서관리</NavLink></li>
                <li><NavLink to="/admin/logs" className={({ isActive }) => isActive ? 'active' : ''}>로그관리</NavLink></li>
              </>
            )}
          </ul>
        </nav>
        <div className="main-content">
          <Outlet />
        </div>
      </div>
    </>
  );
}
