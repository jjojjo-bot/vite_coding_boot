import client from './client';
import type { Assignment } from '../types';

export const assignmentApi = {
  list: () => client.get<Assignment[]>('/assignments'),

  get: (id: number) => client.get<Assignment>(`/assignments/${id}`),

  create: (data: { title: string; description: string; startDate: string; dueDate: string }) =>
    client.post<Assignment>('/assignments', data),

  update: (id: number, data: { title: string; description: string; startDate: string; dueDate: string }) =>
    client.put<Assignment>(`/assignments/${id}`, data),

  delete: (id: number) => client.delete(`/assignments/${id}`),

  approve: (id: number, assigneeUserId: number) =>
    client.post<Assignment>(`/assignments/${id}/approve`, { assigneeUserId }),

  reject: (id: number, rejectionReason: string) =>
    client.post<Assignment>(`/assignments/${id}/reject`, { rejectionReason }),

  submitResult: (id: number, finalResult: string) =>
    client.post<Assignment>(`/assignments/${id}/result`, { finalResult }),
};
