import client from './client';
import type { User } from '../types';

export const authApi = {
  login: (username: string, password: string, otpCode?: string) =>
    client.post('/auth/login', { username, password, otpCode }),

  otpSetupVerify: (otpCode: string) =>
    client.post('/auth/otp-setup-verify', { otpCode }),

  logout: () => client.post('/auth/logout'),

  me: () => client.get<User>('/auth/me'),
};
