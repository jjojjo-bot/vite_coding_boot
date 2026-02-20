import { useState, useMemo, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { AgGridReact } from 'ag-grid-react';
import { AllCommunityModule, ModuleRegistry, type ColDef, type ICellRendererParams } from 'ag-grid-community';
import { assignmentApi } from '../api/assignmentApi';
import { memberApi } from '../api/memberApi';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/common/StatusBadge';
import OrgTreeModal from '../components/common/OrgTreeModal';
import type { Assignment } from '../types';

ModuleRegistry.registerModules([AllCommunityModule]);

export default function ApprovalHistoryPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [filter, setFilter] = useState<string | null>(null);
  const [orgModalOpen, setOrgModalOpen] = useState(false);
  const [selectedAssignmentId, setSelectedAssignmentId] = useState<number | null>(null);
  const [assigneeMap, setAssigneeMap] = useState<Record<number, { id: number; name: string }>>({});

  const { data: allAssignments = [] } = useQuery({ queryKey: ['assignments'], queryFn: () => assignmentApi.list().then(r => r.data) });
  const { data: members = [] } = useQuery({ queryKey: ['members'], queryFn: () => memberApi.list().then(r => r.data), enabled: user?.role === 'LEADER' });

  const assignments = useMemo(() => {
    if (!filter) return allAssignments;
    return allAssignments.filter(a => a.approvalStatus === filter);
  }, [allAssignments, filter]);

  const isLeader = user?.role === 'LEADER';

  const approveMutation = useMutation({
    mutationFn: ({ id, assigneeUserId }: { id: number; assigneeUserId: number }) => assignmentApi.approve(id, assigneeUserId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['assignments'] }),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) => assignmentApi.reject(id, reason),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['assignments'] }),
  });

  const handleApprove = useCallback((id: number) => {
    const a = assigneeMap[id];
    if (!a) { alert('담당자를 먼저 선택해주세요.'); return; }
    approveMutation.mutate({ id, assigneeUserId: a.id });
  }, [assigneeMap, approveMutation]);

  const handleReject = useCallback((id: number) => {
    const reason = prompt('반려 사유를 입력하세요:');
    if (reason && reason.trim()) rejectMutation.mutate({ id, reason });
  }, [rejectMutation]);

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
              <><br /><button className="org-select-btn" onClick={() => { setSelectedAssignmentId(d.id); setOrgModalOpen(true); }}>
                {assigneeMap[d.id]?.name || '담당자 선택'}
              </button></>
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
      headerName: '반려사유', field: 'rejectionReason', flex: 1, minWidth: 100,
      valueFormatter: (params) => params.value || '-',
    },
  ], [isLeader, assigneeMap, handleApprove, handleReject]);

  const tabs = [
    { label: '전체', value: null },
    { label: '승인대기', value: 'PENDING' },
    { label: '승인', value: 'APPROVED' },
    { label: '반려', value: 'REJECTED' },
  ];

  return (
    <>
      <div className="section-header"><h2>결재함</h2></div>
      <div className="filter-tabs">
        {tabs.map(t => (
          <a key={t.label} href="#" className={filter === t.value ? 'active' : ''} onClick={(e) => { e.preventDefault(); setFilter(t.value); }}>{t.label}</a>
        ))}
      </div>
      <div className="ag-theme-alpine" style={{ width: '100%', height: 600 }}>
        <AgGridReact<Assignment>
          rowData={assignments}
          columnDefs={colDefs}
          defaultColDef={{ sortable: true, filter: true, resizable: true }}
          pagination={true}
          paginationPageSize={20}
          paginationPageSizeSelector={[10, 20, 50, 100]}
          overlayNoRowsTemplate='<span style="padding:40px;color:#999;font-size:15px;">결재 이력이 없습니다.</span>'
        />
      </div>
      {isLeader && (
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
      )}
    </>
  );
}
