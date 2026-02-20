import client from './client';
import type { User } from '../types';

export const memberApi = {
  list: () => client.get<User[]>('/members'),
};
