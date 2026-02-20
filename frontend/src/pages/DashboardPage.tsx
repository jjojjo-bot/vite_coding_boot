import { useState, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { AgGridReact } from 'ag-grid-react';
import { AllCommunityModule, ModuleRegistry, type ColDef, type ICellRendererParams } from 'ag-grid-community';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement } from 'chart.js';
import { Doughnut, Bar } from 'react-chartjs-2';
import { assignmentApi } from '../api/assignmentApi';
import { memberApi } from '../api/memberApi';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/common/StatusBadge';
import OrgTreeModal from '../components/common/OrgTreeModal';
import type { Assignment } from '../types';

ModuleRegistry.registerModules([AllCommunityModule]);
ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement);

export default function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [orgModalOpen, setOrgModalOpen] = useState(false);
  const [selectedAssignmentId, setSelectedAssignmentId] = useState<number | null>(null);
  const [assigneeMap, setAssigneeMap] = useState<Record<number, { id: number; name: string }>>({});

  const { data: assignments = [] } = useQuery({ queryKey: ['assignments'], queryFn: () => assignmentApi.list().then(r => r.data) });
  const { data: members = [] } = useQuery({ queryKey: ['members'], queryFn: () => memberApi.list().then(r => r.data) });

  const approveMutation = useMutation({
    mutationFn: ({ id, assigneeUserId }: { id: number; assigneeUserId: number }) => assignmentApi.approve(id, assigneeUserId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['assignments'] }),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) => assignmentApi.reject(id, reason),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['assignments'] }),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => assignmentApi.delete(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['assignments'] }),
  });

  const isLeader = user?.role === 'LEADER';

  // Summary
  const totalCount = assignments.length;
  const pendingCount = assignments.filter(a => a.approvalStatus === 'PENDING').length;
  const inProgressCount = assignments.filter(a => a.progressStatus === 'IN_PROGRESS').length;
  const delayedCount = assignments.filter(a => a.progressStatus === 'DELAYED').length;

  // Chart data
  const approvalCounts = useMemo(() => {
    const c = { PENDING: 0, APPROVED: 0, REJECTED: 0 };
    assignments.forEach(a => { if (c[a.approvalStatus] !== undefined) c[a.approvalStatus]++; });
    return c;
  }, [assignments]);

  const progressCounts = useMemo(() => {
    const c: Record<string, number> = { NOT_STARTED: 0, IN_PROGRESS: 0, DELAYED: 0, COMPLETED: 0, DELAYED_COMPLETED: 0 };
    assignments.forEach(a => { if (a.progressStatus && c[a.progressStatus] !== undefined) c[a.progressStatus]++; });
    return c;
  }, [assignments]);

  const monthlyChart = useMemo(() => {
    const data: Record<string, Record<string, number>> = {};
    assignments.forEach(a => {
      if (!a.dueDate) return;
      const month = a.dueDate.substring(0, 7);
      if (!data[month]) data[month] = { COMPLETED: 0, DELAYED_COMPLETED: 0, IN_PROGRESS: 0, NOT_STARTED: 0, DELAYED: 0, PENDING: 0 };
      if (a.approvalStatus !== 'APPROVED') data[month].PENDING++;
      else if (a.progressStatus) data[month][a.progressStatus]++;
    });
    const months = Object.keys(data).sort();
    const labels = months.map(m => m.replace('-0', '-').replace(/^\d{4}-/, '') + '월');
    return { months, labels, data };
  }, [assignments]);

  const divisionChart = useMemo(() => {
    const memberDiv: Record<number, string> = {};
    members.forEach(m => { memberDiv[m.id] = m.teamFullName?.split(' > ')[0] || '소속없음'; });
    const divApproval: Record<string, { PENDING: number; APPROVED: number; REJECTED: number }> = {};
    assignments.forEach(a => {
      const uid = a.assigneeId || a.createdById;
      const div = (uid && memberDiv[uid]) ? memberDiv[uid] : '미지정';
      if (!divApproval[div]) divApproval[div] = { PENDING: 0, APPROVED: 0, REJECTED: 0 };
      divApproval[div][a.approvalStatus]++;
    });
    return { names: Object.keys(divApproval).sort(), data: divApproval };
  }, [assignments, members]);

  const handleApprove = useCallback((id: number) => {
    const a = assigneeMap[id];
    if (!a) { alert('담당자를 먼저 선택해주세요.'); return; }
    approveMutation.mutate({ id, assigneeUserId: a.id });
  }, [assigneeMap, approveMutation]);

  const handleReject = useCallback((id: number) => {
    const reason = prompt('반려 사유를 입력하세요:');
    if (reason && reason.trim()) rejectMutation.mutate({ id, reason });
  }, [rejectMutation]);

  const handleDelete = useCallback((id: number) => {
    if (confirm('삭제하시겠습니까?')) deleteMutation.mutate(id);
  }, [deleteMutation]);

  const colDefs = useMemo<ColDef<Assignment>[]>(() => [
    { headerName: '제목', field: 'title', flex: 2, minWidth: 150 },
    { headerName: '등록자', field: 'createdByName', width: 90 },
    {
      headerName: '담당자', width: 140, sortable: false, filter: false, autoHeight: true,
      cellRenderer: (params: ICellRendererParams<Assignment>) => {
        const d = params.data!;
        return (
          <div>
            <span>{d.assigneeName}</span>
            {isLeader && d.approvalStatus === 'PENDING' && (
              <><br />
                <button className="org-select-btn" onClick={() => { setSelectedAssignmentId(d.id); setOrgModalOpen(true); }}>
                  {assigneeMap[d.id]?.name || '담당자 선택'}
                </button>
              </>
            )}
          </div>
        );
      },
    },
    { headerName: '착수일', field: 'startDate', width: 110 },
    { headerName: '마감일', field: 'dueDate', width: 110 },
    {
      headerName: '결재상태', width: 160, sortable: false, filter: false, autoHeight: true,
      cellRenderer: (params: ICellRendererParams<Assignment>) => {
        const d = params.data!;
        return (
          <div>
            <StatusBadge type="approval" value={d.approvalStatus} />
            {d.rejectionReason && <div className="rejection-reason">사유: {d.rejectionReason}</div>}
            {isLeader && d.approvalStatus === 'PENDING' && (
              <div style={{ marginTop: 4 }}>
                <button className="btn btn-success btn-sm" onClick={() => handleApprove(d.id)}>승인</button>{' '}
                <button className="btn btn-warning btn-sm" onClick={() => handleReject(d.id)}>반려</button>
              </div>
            )}
          </div>
        );
      },
    },
    {
      headerName: '진행상태', width: 150, sortable: false, filter: false, autoHeight: true,
      cellRenderer: (params: ICellRendererParams<Assignment>) => {
        const d = params.data!;
        return (
          <div>
            <StatusBadge type="status" value={d.progressStatus} />
            {d.assigneeId === user?.id && d.approvalStatus === 'APPROVED' && !d.hasFinalResult && (
              <div style={{ marginTop: 4 }}>
                <button className="btn btn-success btn-sm" onClick={() => navigate(`/assignments/${d.id}/result`)}>최종결과</button>
              </div>
            )}
          </div>
        );
      },
    },
    {
      headerName: '수정', width: 70, sortable: false, filter: false,
      cellRenderer: (params: ICellRendererParams<Assignment>) => (
        <button className="btn btn-info btn-sm" onClick={() => navigate(`/assignments/${params.data!.id}/edit`)}>수정</button>
      ),
    },
    ...(isLeader ? [{
      headerName: '삭제', width: 70, sortable: false, filter: false,
      cellRenderer: (params: ICellRendererParams<Assignment>) => (
        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(params.data!.id)}>삭제</button>
      ),
    } as ColDef<Assignment>] : []),
  ], [isLeader, assigneeMap, user, navigate, handleApprove, handleReject, handleDelete]);

  return (
    <>
      <div className="summary-cards">
        <div className="summary-card"><div className="label">전체 과제</div><div className="value">{totalCount}</div></div>
        <div className="summary-card highlight-pending"><div className="label">승인대기</div><div className="value">{pendingCount}</div></div>
        <div className="summary-card highlight-progress"><div className="label">진행중</div><div className="value">{inProgressCount}</div></div>
        <div className="summary-card highlight-delayed"><div className="label">지연</div><div className="value">{delayedCount}</div></div>
      </div>

      <div className="chart-section">
        <div className="chart-card">
          <h3>결재상태 현황</h3>
          <div style={{ height: 220 }}>
            <Doughnut data={{
              labels: ['승인대기', '승인', '반려'],
              datasets: [{ data: [approvalCounts.PENDING, approvalCounts.APPROVED, approvalCounts.REJECTED], backgroundColor: ['#f6c344', '#34c78e', '#e74c3c'], borderWidth: 0 }],
            }} options={{ responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { padding: 16, usePointStyle: true, pointStyle: 'circle' } } }, cutout: '55%' }} />
          </div>
        </div>
        <div className="chart-card">
          <h3>진행상태 현황</h3>
          <div style={{ height: 220 }}>
            <Doughnut data={{
              labels: ['미진행', '진행중', '지연', '완료', '지연완료'],
              datasets: [{ data: [progressCounts.NOT_STARTED, progressCounts.IN_PROGRESS, progressCounts.DELAYED, progressCounts.COMPLETED, progressCounts.DELAYED_COMPLETED], backgroundColor: ['#adb5bd', '#54a0ff', '#e74c3c', '#34c78e', '#f6c344'], borderWidth: 0 }],
            }} options={{ responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { padding: 16, usePointStyle: true, pointStyle: 'circle' } } }, cutout: '55%' }} />
          </div>
        </div>
        <div className="chart-card">
          <h3>월별 마감 현황</h3>
          <div style={{ height: 220 }}>
            <Bar data={{
              labels: monthlyChart.labels,
              datasets: [
                { label: '완료', data: monthlyChart.months.map(m => monthlyChart.data[m].COMPLETED), backgroundColor: '#34c78e' },
                { label: '지연완료', data: monthlyChart.months.map(m => monthlyChart.data[m].DELAYED_COMPLETED), backgroundColor: '#f6c344' },
                { label: '진행중', data: monthlyChart.months.map(m => monthlyChart.data[m].IN_PROGRESS), backgroundColor: '#54a0ff' },
                { label: '미진행', data: monthlyChart.months.map(m => monthlyChart.data[m].NOT_STARTED), backgroundColor: '#adb5bd' },
                { label: '지연', data: monthlyChart.months.map(m => monthlyChart.data[m].DELAYED), backgroundColor: '#e74c3c' },
                { label: '미승인', data: monthlyChart.months.map(m => monthlyChart.data[m].PENDING), backgroundColor: '#dfe6e9' },
              ],
            }} options={{ responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { padding: 12, usePointStyle: true, pointStyle: 'circle', font: { size: 11 } } } }, scales: { x: { stacked: true, grid: { display: false } }, y: { stacked: true, beginAtZero: true, ticks: { stepSize: 5 } } } }} />
          </div>
        </div>
        <div className="chart-card">
          <h3>본부별 과제 현황</h3>
          <div style={{ height: 220 }}>
            <Bar data={{
              labels: divisionChart.names,
              datasets: [
                { label: '승인', data: divisionChart.names.map(d => divisionChart.data[d].APPROVED), backgroundColor: '#34c78e' },
                { label: '승인대기', data: divisionChart.names.map(d => divisionChart.data[d].PENDING), backgroundColor: '#f6c344' },
                { label: '반려', data: divisionChart.names.map(d => divisionChart.data[d].REJECTED), backgroundColor: '#e74c3c' },
              ],
            }} options={{ indexAxis: 'y', responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { padding: 12, usePointStyle: true, pointStyle: 'circle', font: { size: 11 } } } }, scales: { x: { stacked: true, beginAtZero: true, ticks: { stepSize: 10 }, grid: { display: false } }, y: { stacked: true, grid: { display: false } } } }} />
          </div>
        </div>
      </div>

      <div className="section-header">
        <h2>과제 목록</h2>
        <button className="btn btn-primary" onClick={() => navigate('/assignments/new')}>과제 생성</button>
      </div>

      <div className="ag-theme-alpine" style={{ width: '100%', height: 600 }}>
        <AgGridReact<Assignment>
          rowData={assignments}
          columnDefs={colDefs}
          defaultColDef={{ sortable: true, filter: true, resizable: true }}
          pagination={true}
          paginationPageSize={20}
          paginationPageSizeSelector={[10, 20, 50, 100]}
          overlayNoRowsTemplate='<span style="padding:40px;color:#999;font-size:15px;">등록된 과제가 없습니다.</span>'
        />
      </div>

      <OrgTreeModal
        members={members}
        open={orgModalOpen}
        onClose={() => setOrgModalOpen(false)}
        onSelect={(userId, userName) => {
          if (selectedAssignmentId !== null) {
            setAssigneeMap(prev => ({ ...prev, [selectedAssignmentId]: { id: userId, name: userName } }));
          }
        }}
      />
    </>
  );
}
