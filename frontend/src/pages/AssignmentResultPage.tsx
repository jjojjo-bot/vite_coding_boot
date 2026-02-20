import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { assignmentApi } from '../api/assignmentApi';
import type { Assignment } from '../types';

export default function AssignmentResultPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [assignment, setAssignment] = useState<Assignment | null>(null);
  const [finalResult, setFinalResult] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    assignmentApi.get(Number(id)).then(res => {
      setAssignment(res.data);
      setFinalResult(res.data.finalResult || '');
    }).catch(() => navigate('/dashboard'));
  }, [id, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await assignmentApi.submitResult(Number(id), finalResult);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || '등록에 실패했습니다.');
    }
  };

  if (!assignment) return <div>로딩중...</div>;

  return (
    <>
      <div className="section-header"><h2>최종결과 등록</h2></div>
      <div style={{ background: '#fff', borderRadius: 8, padding: 32, boxShadow: '0 1px 4px rgba(0,0,0,0.06)', maxWidth: 600 }}>
        <div style={{ marginBottom: 20, padding: 16, background: '#f8f9fb', borderRadius: 4 }}>
          <strong>{assignment.title}</strong>
          <div style={{ fontSize: 13, color: '#666', marginTop: 4 }}>마감일: {assignment.dueDate}</div>
        </div>
        {error && <div className="error-message" style={{ marginBottom: 16 }}>{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>최종결과</label>
            <textarea value={finalResult} onChange={e => setFinalResult(e.target.value)} rows={6} required
              style={{ width: '100%', padding: '12px 14px', border: '1px solid #d5d8dc', borderRadius: 4, fontSize: 14 }} />
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button type="submit" className="btn btn-primary">등록</button>
            <button type="button" className="btn" style={{ background: '#6c757d' }} onClick={() => navigate('/dashboard')}>취소</button>
          </div>
        </form>
      </div>
    </>
  );
}
