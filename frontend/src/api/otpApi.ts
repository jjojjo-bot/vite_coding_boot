import client from './client';

export const otpApi = {
  status: () => client.get<{ otpEnabled: boolean }>('/otp/status'),
  setup: () => client.post<{ qrCodeDataUri: string }>('/otp/setup'),
  verify: (otpCode: string) => client.post('/otp/verify', { otpCode }),
  disable: () => client.post('/otp/disable'),
};
