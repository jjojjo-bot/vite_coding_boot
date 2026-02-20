import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [otpCode, setOtpCode] = useState('');
  const [error, setError] = useState('');
  const [otpRequired, setOtpRequired] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      const data = await login(username, password, otpCode || undefined);
      if (data.otpRequired && data.otpResetRequired) {
        navigate('/otp-setup', { state: { qrCodeDataUri: data.qrCodeDataUri } });
      } else if (data.otpRequired) {
        setOtpRequired(true);
      } else {
        navigate('/dashboard');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || '로그인에 실패했습니다.');
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f4f6f9' }}>
      <div className="login-container">
        <div className="login-logo">
          <img src="https://infra1-static.recruiter.co.kr/builder/2024/01/26/9f60ffa1-8560-4a34-a76c-863bc2dac21c.png" alt="CI" />
        </div>
        <h1 style={{ textAlign: 'center', marginBottom: 32, color: '#002c5f', fontSize: 20, fontWeight: 600 }}>과제관리 시스템</h1>
        <form onSubmit={handleSubmit}>
          {error && <div className="error-message">{error}</div>}
          <div className="form-group">
            <label htmlFor="username">아이디</label>
            <input id="username" type="text" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="아이디를 입력하세요" required />
          </div>
          <div className="form-group">
            <label htmlFor="password">비밀번호 (선택)</label>
            <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="비밀번호 없이 로그인 가능" />
          </div>
          {otpRequired && (
            <div className="form-group">
              <label htmlFor="otpCode">OTP 코드</label>
              <input id="otpCode" type="text" value={otpCode} onChange={(e) => setOtpCode(e.target.value)} placeholder="6자리 OTP 코드" maxLength={6} autoComplete="off" autoFocus />
            </div>
          )}
          {!otpRequired && (
            <div className="form-group">
              <label htmlFor="otpCodeOpt">OTP 코드 (선택)</label>
              <input id="otpCodeOpt" type="text" value={otpCode} onChange={(e) => setOtpCode(e.target.value)} placeholder="OTP 설정 시 입력" maxLength={6} autoComplete="off" />
            </div>
          )}
          <button type="submit" className="login-btn">로그인</button>
        </form>
      </div>
    </div>
  );
}
