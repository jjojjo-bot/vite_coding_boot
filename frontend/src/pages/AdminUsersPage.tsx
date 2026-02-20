import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { AgGridReact } from 'ag-grid-react';
import { AllCommunityModule, ModuleRegistry, type ColDef, type ICellRendererParams } from 'ag-grid-community';
import { adminApi } from '../api/adminApi';
import type { User } from '../types';

ModuleRegistry.registerModules([AllCommunityModule]);

export default function AdminUsersPage() {
  const queryClient = useQueryClient();
  const { data: users = [] } = useQuery({ queryKey: ['admin-users'], queryFn: () => adminApi.listUsers().then(r => r.data) });
  const { data: teams = [] } = useQuery({ queryKey: ['admin-teams'], queryFn: () => adminApi.listTeams().then(r => r.data) });

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editUser, setEditUser] = useState<User | null>(null);
  const [resetPwUser, setResetPwUser] = useState<User | null>(null);

  // Create form
  const [newUsername, setNewUsername] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [newName, setNewName] = useState('');
  const [newRole, setNewRole] = useState('MEMBER');
  const [newTeamId, setNewTeamId] = useState<number | null>(null);

  // Edit form
  const [editName, setEditName] = useState('');
  const [editRole, setEditRole] = useState('');
  const [editTeamId, setEditTeamId] = useState<number | null>(null);

  // Reset password
  const [newPw, setNewPw] = useState('');

  const createMutation = useMutation({
    mutationFn: () => adminApi.createUser({ username: newUsername, password: newPassword, name: newName, role: newRole, teamId: newTeamId }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-users'] }); setShowCreateModal(false); setNewUsername(''); setNewPassword(''); setNewName(''); setNewRole('MEMBER'); setNewTeamId(null); },
  });

  const updateMutation = useMutation({
    mutationFn: (id: number) => adminApi.updateUser(id, { name: editName, role: editRole, teamId: editTeamId }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-users'] }); setEditUser(null); },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminApi.deleteUser(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-users'] }),
  });

  const resetPwMutation = useMutation({
    mutationFn: (id: number) => adminApi.resetPassword(id, newPw),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-users'] }); setResetPwUser(null); setNewPw(''); },
  });

  const toggleOtpMutation = useMutation({
    mutationFn: ({ id, enabled }: { id: number; enabled: string }) => adminApi.toggleOtp(id, enabled),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-users'] }),
  });

  const resetOtpMutation = useMutation({
    mutationFn: (id: number) => adminApi.resetOtp(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-users'] }),
  });

  const colDefs: ColDef<User>[] = [
    { headerName: 'ID', field: 'id', width: 60 },
    { headerName: '아이디', field: 'username', width: 100 },
    { headerName: '이름', field: 'name', width: 90 },
    { headerName: '역할', field: 'role', width: 80 },
    { headerName: '소속', field: 'teamFullName', flex: 1, valueFormatter: p => p.value || '-' },
    {
      headerName: 'OTP', width: 100, sortable: false, filter: false,
      cellRenderer: (params: ICellRendererParams<User>) => (
        <span className={`status-badge ${params.data!.otpEnabled ? 'approval-APPROVED' : ''}`}>
          {params.data!.otpEnabled ? '사용' : '미사용'}
        </span>
      ),
    },
    {
      headerName: '관리', width: 280, sortable: false, filter: false,
      cellRenderer: (params: ICellRendererParams<User>) => {
        const u = params.data!;
        return (
          <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
            <button className="btn btn-info btn-sm" onClick={() => { setEditUser(u); setEditName(u.name); setEditRole(u.role); setEditTeamId(u.teamId); }}>수정</button>
            <button className="btn btn-warning btn-sm" onClick={() => setResetPwUser(u)}>비밀번호</button>
            <button className="btn btn-sm" style={{ background: u.otpEnabled ? '#d63031' : '#00875a', color: '#fff' }}
              onClick={() => toggleOtpMutation.mutate({ id: u.id, enabled: u.otpEnabled ? '미사용' : '사용' })}>
              {u.otpEnabled ? 'OTP해제' : 'OTP사용'}
            </button>
            {u.otpEnabled && <button className="btn btn-sm" style={{ background: '#6c757d', color: '#fff' }} onClick={() => { if (confirm('OTP를 초기화하시겠습니까?')) resetOtpMutation.mutate(u.id); }}>OTP초기화</button>}
            <button className="btn btn-danger btn-sm" onClick={() => { if (confirm('삭제하시겠습니까?')) deleteMutation.mutate(u.id); }}>삭제</button>
          </div>
        );
      },
    },
  ];

  return (
    <>
      <div className="section-header">
        <h2>사용자관리</h2>
        <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>사용자 추가</button>
      </div>

      <div className="ag-theme-alpine" style={{ width: '100%', height: 600 }}>
        <AgGridReact<User> rowData={users} columnDefs={colDefs}
          defaultColDef={{ sortable: true, filter: true, resizable: true }}
          pagination paginationPageSize={20} paginationPageSizeSelector={[10, 20, 50, 100]} />
      </div>

      {/* Create Modal */}
      {showCreateModal && (
        <div className="org-modal-overlay active" onClick={e => { if (e.target === e.currentTarget) setShowCreateModal(false); }}>
          <div className="org-modal" style={{ width: 460 }}>
            <div className="org-modal-header"><span>사용자 추가</span><button className="org-modal-close" onClick={() => setShowCreateModal(false)}>&times;</button></div>
            <div className="org-modal-body">
              <form onSubmit={e => { e.preventDefault(); createMutation.mutate(); }}>
                <div className="form-group"><label>아이디</label><input value={newUsername} onChange={e => setNewUsername(e.target.value)} required /></div>
                <div className="form-group"><label>비밀번호</label><input type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)} /></div>
                <div className="form-group"><label>이름</label><input value={newName} onChange={e => setNewName(e.target.value)} required /></div>
                <div className="form-group"><label>역할</label>
                  <select value={newRole} onChange={e => setNewRole(e.target.value)} style={{ width: '100%', padding: '12px 14px', border: '1px solid #d5d8dc', borderRadius: 4 }}>
                    <option value="MEMBER">MEMBER</option><option value="LEADER">LEADER</option>
                  </select>
                </div>
                <div className="form-group"><label>소속</label>
                  <select value={newTeamId ?? ''} onChange={e => setNewTeamId(e.target.value ? Number(e.target.value) : null)} style={{ width: '100%', padding: '12px 14px', border: '1px solid #d5d8dc', borderRadius: 4 }}>
                    <option value="">없음</option>
                    {teams.map(t => <option key={t.id} value={t.id}>{t.fullName}</option>)}
                  </select>
                </div>
                <button type="submit" className="btn btn-primary">추가</button>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Edit Modal */}
      {editUser && (
        <div className="org-modal-overlay active" onClick={e => { if (e.target === e.currentTarget) setEditUser(null); }}>
          <div className="org-modal" style={{ width: 460 }}>
            <div className="org-modal-header"><span>사용자 수정</span><button className="org-modal-close" onClick={() => setEditUser(null)}>&times;</button></div>
            <div className="org-modal-body">
              <form onSubmit={e => { e.preventDefault(); updateMutation.mutate(editUser.id); }}>
                <div className="form-group"><label>이름</label><input value={editName} onChange={e => setEditName(e.target.value)} required /></div>
                <div className="form-group"><label>역할</label>
                  <select value={editRole} onChange={e => setEditRole(e.target.value)} style={{ width: '100%', padding: '12px 14px', border: '1px solid #d5d8dc', borderRadius: 4 }}>
                    <option value="MEMBER">MEMBER</option><option value="LEADER">LEADER</option>
                  </select>
                </div>
                <div className="form-group"><label>소속</label>
                  <select value={editTeamId ?? ''} onChange={e => setEditTeamId(e.target.value ? Number(e.target.value) : null)} style={{ width: '100%', padding: '12px 14px', border: '1px solid #d5d8dc', borderRadius: 4 }}>
                    <option value="">없음</option>
                    {teams.map(t => <option key={t.id} value={t.id}>{t.fullName}</option>)}
                  </select>
                </div>
                <button type="submit" className="btn btn-primary">수정</button>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Reset Password Modal */}
      {resetPwUser && (
        <div className="org-modal-overlay active" onClick={e => { if (e.target === e.currentTarget) setResetPwUser(null); }}>
          <div className="org-modal" style={{ width: 400 }}>
            <div className="org-modal-header"><span>비밀번호 초기화</span><button className="org-modal-close" onClick={() => setResetPwUser(null)}>&times;</button></div>
            <div className="org-modal-body">
              <p style={{ marginBottom: 16, fontSize: 14, color: '#666' }}>대상: {resetPwUser.name} ({resetPwUser.username})</p>
              <form onSubmit={e => { e.preventDefault(); resetPwMutation.mutate(resetPwUser.id); }}>
                <div className="form-group"><label>새 비밀번호</label><input type="password" value={newPw} onChange={e => setNewPw(e.target.value)} required /></div>
                <button type="submit" className="btn btn-primary">초기화</button>
              </form>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
