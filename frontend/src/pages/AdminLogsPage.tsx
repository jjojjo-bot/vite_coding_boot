import { useQuery } from '@tanstack/react-query';
import { AgGridReact } from 'ag-grid-react';
import { AllCommunityModule, ModuleRegistry, type ColDef } from 'ag-grid-community';
import { adminApi } from '../api/adminApi';
import type { AuditLog } from '../types';

ModuleRegistry.registerModules([AllCommunityModule]);

export default function AdminLogsPage() {
  const { data: logs = [] } = useQuery({ queryKey: ['admin-logs'], queryFn: () => adminApi.listLogs().then(r => r.data) });

  const colDefs: ColDef<AuditLog>[] = [
    { headerName: 'ID', field: 'id', width: 60 },
    { headerName: '액션', field: 'action', width: 90 },
    { headerName: '대상', field: 'targetType', width: 100 },
    { headerName: '대상ID', field: 'targetId', width: 70 },
    { headerName: '상세', field: 'details', flex: 2 },
    { headerName: '수행자', field: 'performedByName', width: 90 },
    { headerName: '일시', field: 'createdAt', width: 160, valueFormatter: p => p.value ? new Date(p.value).toLocaleString('ko-KR') : '' },
  ];

  return (
    <>
      <div className="section-header"><h2>로그관리</h2></div>
      <div className="ag-theme-alpine" style={{ width: '100%', height: 600 }}>
        <AgGridReact<AuditLog> rowData={logs} columnDefs={colDefs}
          defaultColDef={{ sortable: true, filter: true, resizable: true }}
          pagination paginationPageSize={20} paginationPageSizeSelector={[10, 20, 50, 100]} />
      </div>
    </>
  );
}
