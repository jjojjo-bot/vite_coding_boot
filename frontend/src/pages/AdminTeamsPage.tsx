import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { AgGridReact } from 'ag-grid-react';
import { AllCommunityModule, ModuleRegistry, type ColDef, type ICellRendererParams } from 'ag-grid-community';
import { adminApi } from '../api/adminApi';
import type { Team } from '../types';

ModuleRegistry.registerModules([AllCommunityModule]);

export default function AdminTeamsPage() {
  const queryClient = useQueryClient();
  const { data: teams = [] } = useQuery({ queryKey: ['admin-teams'], queryFn: () => adminApi.listTeams().then(r => r.data) });

  const [showCreate, setShowCreate] = useState(false);
  const [editTeam, setEditTeam] = useState<Team | null>(null);

  const [division, setDivision] = useState('');
  const [department, setDepartment] = useState('');
  const [name, setName] = useState('');

  const [editDivision, setEditDivision] = useState('');
  const [editDepartment, setEditDepartment] = useState('');
  const [editName, setEditName] = useState('');

  const createMutation = useMutation({
    mutationFn: () => adminApi.createTeam({ division, department, name }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-teams'] }); setShowCreate(false); setDivision(''); setDepartment(''); setName(''); },
  });

  const updateMutation = useMutation({
    mutationFn: (id: number) => adminApi.updateTeam(id, { division: editDivision, department: editDepartment, name: editName }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-teams'] }); setEditTeam(null); },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminApi.deleteTeam(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-teams'] }),
    onError: (err: any) => alert(err.response?.data?.message || '삭제 실패'),
  });

  const colDefs: ColDef<Team>[] = [
    { headerName: 'ID', field: 'id', width: 60 },
    { headerName: '본부', field: 'division', flex: 1 },
    { headerName: '부서', field: 'department', flex: 1 },
    { headerName: '팀', field: 'name', flex: 1 },
    { headerName: '전체명', field: 'fullName', flex: 2 },
    {
      headerName: '관리', width: 140, sortable: false, filter: false,
      cellRenderer: (params: ICellRendererParams<Team>) => {
        const t = params.data!;
        return (
          <div style={{ display: 'flex', gap: 4 }}>
            <button className="btn btn-info btn-sm" onClick={() => { setEditTeam(t); setEditDivision(t.division); setEditDepartment(t.department); setEditName(t.name); }}>수정</button>
            <button className="btn btn-danger btn-sm" onClick={() => { if (confirm('삭제하시겠습니까?')) deleteMutation.mutate(t.id); }}>삭제</button>
          </div>
        );
      },
    },
  ];

  return (
    <>
      <div className="section-header">
        <h2>부서관리</h2>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)}>부서 추가</button>
      </div>

      <div className="ag-theme-alpine" style={{ width: '100%', height: 600 }}>
        <AgGridReact<Team> rowData={teams} columnDefs={colDefs}
          defaultColDef={{ sortable: true, filter: true, resizable: true }}
          pagination paginationPageSize={20} paginationPageSizeSelector={[10, 20, 50, 100]} />
      </div>

      {showCreate && (
        <div className="org-modal-overlay active" onClick={e => { if (e.target === e.currentTarget) setShowCreate(false); }}>
          <div className="org-modal" style={{ width: 460 }}>
            <div className="org-modal-header"><span>부서 추가</span><button className="org-modal-close" onClick={() => setShowCreate(false)}>&times;</button></div>
            <div className="org-modal-body">
              <form onSubmit={e => { e.preventDefault(); createMutation.mutate(); }}>
                <div className="form-group"><label>본부</label><input value={division} onChange={e => setDivision(e.target.value)} required /></div>
                <div className="form-group"><label>부서</label><input value={department} onChange={e => setDepartment(e.target.value)} required /></div>
                <div className="form-group"><label>팀</label><input value={name} onChange={e => setName(e.target.value)} required /></div>
                <button type="submit" className="btn btn-primary">추가</button>
              </form>
            </div>
          </div>
        </div>
      )}

      {editTeam && (
        <div className="org-modal-overlay active" onClick={e => { if (e.target === e.currentTarget) setEditTeam(null); }}>
          <div className="org-modal" style={{ width: 460 }}>
            <div className="org-modal-header"><span>부서 수정</span><button className="org-modal-close" onClick={() => setEditTeam(null)}>&times;</button></div>
            <div className="org-modal-body">
              <form onSubmit={e => { e.preventDefault(); updateMutation.mutate(editTeam.id); }}>
                <div className="form-group"><label>본부</label><input value={editDivision} onChange={e => setEditDivision(e.target.value)} required /></div>
                <div className="form-group"><label>부서</label><input value={editDepartment} onChange={e => setEditDepartment(e.target.value)} required /></div>
                <div className="form-group"><label>팀</label><input value={editName} onChange={e => setEditName(e.target.value)} required /></div>
                <button type="submit" className="btn btn-primary">수정</button>
              </form>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
