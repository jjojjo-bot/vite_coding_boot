interface Props {
  type: 'approval' | 'status';
  value: string | null;
}

const labels: Record<string, string> = {
  PENDING: '승인대기',
  APPROVED: '승인',
  REJECTED: '반려',
  NOT_STARTED: '미진행',
  IN_PROGRESS: '진행중',
  DELAYED: '지연',
  COMPLETED: '완료',
  DELAYED_COMPLETED: '지연완료',
};

export default function StatusBadge({ type, value }: Props) {
  if (!value) return <span>-</span>;
  const cls = type === 'approval' ? `approval-${value}` : `status-${value}`;
  return <span className={`status-badge ${cls}`}>{labels[value] || value}</span>;
}
