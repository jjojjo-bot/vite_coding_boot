import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { otpApi } from '../api/otpApi';

export default function OtpSettingsPage() {
  const queryClient = useQueryClient();
  const { data: otpStatus } = useQuery({ queryKey: ['otp-status'], queryFn: () => otpApi.status().then(r => r.data) });
  const [qrCodeDataUri, setQrCodeDataUri] = useState<string | null>(null);
  const [otpCode, setOtpCode] = useState('');
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const otpEnabled = otpStatus?.otpEnabled ?? false;

  const handleSetup = async () => {
    try {
      const res = await otpApi.setup();
      setQrCodeDataUri(res.data.qrCodeDataUri);
      setMessage(null);
    } catch {
      setMessage({ type: 'error', text: 'OTP 설정 시작에 실패했습니다.' });
    }
  };

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await otpApi.verify(otpCode);
      setQrCodeDataUri(null);
      setOtpCode('');
      setMessage({ type: 'success', text: 'OTP가 성공적으로 활성화되었습니다.' });
      queryClient.invalidateQueries({ queryKey: ['otp-status'] });
    } catch (err: any) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'OTP 코드가 올바르지 않습니다.' });
    }
  };

  const handleDisable = async () => {
    if (!confirm('OTP 인증을 해제하시겠습니까?')) return;
    try {
      await otpApi.disable();
      setMessage({ type: 'success', text: 'OTP가 해제되었습니다.' });
      queryClient.invalidateQueries({ queryKey: ['otp-status'] });
    } catch {
      setMessage({ type: 'error', text: 'OTP 해제에 실패했습니다.' });
    }
  };

  return (
    <>
      <div className="section-header"><h2>OTP 설정</h2></div>

      {message && (
        <div className={`alert ${message.type === 'success' ? 'alert-success' : 'alert-error'}`}>
          {message.text}
        </div>
      )}

      <div className="otp-card">
        <div className={`otp-status ${otpEnabled ? 'enabled' : 'disabled'}`}>
          <div className="otp-status-dot"></div>
          <span className="otp-status-text">{otpEnabled ? 'OTP 인증 활성화됨' : 'OTP 인증 비활성화'}</span>
        </div>

        {qrCodeDataUri && (
          <>
            <div className="qr-section">
              <img src={qrCodeDataUri} alt="QR Code" width={200} height={200} style={{ border: '1px solid #e5e8eb', borderRadius: 8, padding: 8 }} />
              <p style={{ marginTop: 12, fontSize: 13, color: '#666' }}>Google Authenticator 앱으로 QR 코드를 스캔하세요.</p>
            </div>
            <form onSubmit={handleVerify}>
              <div className="form-group">
                <label>인증 코드 (6자리)</label>
                <input type="text" value={otpCode} onChange={e => setOtpCode(e.target.value)}
                  maxLength={6} pattern="[0-9]{6}" placeholder="000000" required autoComplete="off"
                  style={{ textAlign: 'center', letterSpacing: 8, fontSize: 16 }} />
              </div>
              <button type="submit" className="btn btn-primary">확인</button>
            </form>
          </>
        )}

        {!otpEnabled && !qrCodeDataUri && (
          <>
            <p style={{ marginBottom: 20, fontSize: 14, color: '#666' }}>
              OTP를 설정하면 로그인 시 Google Authenticator 앱의 인증 코드를 추가로 입력해야 합니다.
            </p>
            <button className="btn btn-primary" onClick={handleSetup}>OTP 설정하기</button>
          </>
        )}

        {otpEnabled && !qrCodeDataUri && (
          <>
            <p style={{ marginBottom: 20, fontSize: 14, color: '#666' }}>
              OTP 인증이 활성화되어 있습니다. 해제하면 로그인 시 OTP 코드 입력이 필요하지 않습니다.
            </p>
            <button className="btn btn-danger" onClick={handleDisable}>OTP 해제</button>
          </>
        )}
      </div>
    </>
  );
}
