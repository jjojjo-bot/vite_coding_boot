import client from './client';
import type { User, Team, AuditLog } from '../types';

export const adminApi = {
  // Users
  listUsers: () => client.get<User[]>('/admin/users'),
  createUser: (data: { username: string; password: string; name: string; role: string; teamId: number | null }) =>
    client.post<User>('/admin/users', data),
  updateUser: (id: number, data: { name: string; role: string; teamId: number | null }) =>
    client.put<User>(`/admin/users/${id}`, data),
  deleteUser: (id: number) => client.delete(`/admin/users/${id}`),
  resetPassword: (id: number, newPassword: string) =>
    client.post(`/admin/users/${id}/reset-password`, { newPassword }),
  toggleOtp: (id: number, otpEnabled: string) =>
    client.post(`/admin/users/${id}/toggle-otp`, { otpEnabled }),
  resetOtp: (id: number) => client.post(`/admin/users/${id}/reset-otp`),

  // Teams
  listTeams: () => client.get<Team[]>('/admin/teams'),
  createTeam: (data: { division: string; department: string; name: string }) =>
    client.post<Team>('/admin/teams', data),
  updateTeam: (id: number, data: { division: string; department: string; name: string }) =>
    client.put<Team>(`/admin/teams/${id}`, data),
  deleteTeam: (id: number) => client.delete(`/admin/teams/${id}`),

  // Logs
  listLogs: () => client.get<AuditLog[]>('/admin/logs'),
};
