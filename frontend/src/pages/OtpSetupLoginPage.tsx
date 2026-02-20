import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { useAuth } from '../context/AuthContext';

export default function OtpSetupLoginPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { setUser } = useAuth();
  const qrCodeDataUri = (location.state as any)?.qrCodeDataUri;
  const [otpCode, setOtpCode] = useState('');
  const [error, setError] = useState('');

  if (!qrCodeDataUri) {
    navigate('/login');
    return null;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      const res = await authApi.otpSetupVerify(otpCode);
      setUser(res.data);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || 'OTP 코드가 올바르지 않습니다.');
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f4f6f9' }}>
      <div className="setup-container">
        <h1 style={{ textAlign: 'center', marginBottom: 8, color: '#002c5f', fontSize: 20, fontWeight: 600 }}>OTP 설정</h1>
        {error && <div className="error-message">{error}</div>}
        <div className="qr-section">
          <img src={qrCodeDataUri} alt="QR Code" width={200} height={200} style={{ border: '1px solid #e5e8eb', borderRadius: 8, padding: 8 }} />
          <p style={{ marginTop: 12, fontSize: 13, color: '#666' }}>
            Google Authenticator 앱으로 QR 코드를 스캔한 후<br />인증 코드를 입력하세요.
          </p>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="otpCode">인증 코드 (6자리)</label>
            <input type="text" id="otpCode" value={otpCode} onChange={(e) => setOtpCode(e.target.value)}
              maxLength={6} pattern="[0-9]{6}" placeholder="000000" required autoComplete="off"
              style={{ textAlign: 'center', letterSpacing: 8, fontSize: 16 }} />
          </div>
          <button type="submit" className="login-btn">확인</button>
        </form>
        <a href="/login" style={{ display: 'block', textAlign: 'center', marginTop: 16, color: '#666', fontSize: 13, textDecoration: 'none' }}>
          로그인으로 돌아가기
        </a>
      </div>
    </div>
  );
}
