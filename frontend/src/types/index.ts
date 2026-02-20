export interface User {
  id: number;
  username: string;
  name: string;
  role: 'LEADER' | 'MEMBER';
  teamId: number | null;
  teamFullName: string | null;
  otpEnabled: boolean;
}

export interface Assignment {
  id: number;
  title: string;
  description: string;
  createdById: number | null;
  createdByName: string;
  assigneeId: number | null;
  assigneeName: string;
  startDate: string;
  dueDate: string;
  approvalStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
  rejectionReason: string | null;
  progressStatus: string | null;
  hasFinalResult: boolean;
  finalResult: string | null;
  resultRegisteredAt: string | null;
}

export interface Team {
  id: number;
  division: string;
  department: string;
  name: string;
  fullName: string;
}

export interface AuditLog {
  id: number;
  action: string;
  targetType: string;
  targetId: number;
  details: string;
  performedByName: string;
  createdAt: string;
}

export interface LoginResponse {
  otpRequired?: boolean;
  otpResetRequired?: boolean;
  qrCodeDataUri?: string;
  // If login succeeds, returns UserResponse fields
  id?: number;
  username?: string;
  name?: string;
  role?: string;
}
