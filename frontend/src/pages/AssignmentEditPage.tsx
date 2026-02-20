import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { assignmentApi } from '../api/assignmentApi';

export default function AssignmentEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [startDate, setStartDate] = useState('');
  const [dueDate, setDueDate] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    assignmentApi.get(Number(id)).then(res => {
      const a = res.data;
      setTitle(a.title);
      setDescription(a.description || '');
      setStartDate(a.startDate);
      setDueDate(a.dueDate);
    }).catch(() => navigate('/dashboard'))
      .finally(() => setLoading(false));
  }, [id, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await assignmentApi.update(Number(id), { title, description, startDate, dueDate });
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || '수정에 실패했습니다.');
    }
  };

  if (loading) return <div>로딩중...</div>;

  return (
    <>
      <div className="section-header"><h2>과제 수정</h2></div>
      <div style={{ background: '#fff', borderRadius: 8, padding: 32, boxShadow: '0 1px 4px rgba(0,0,0,0.06)', maxWidth: 600 }}>
        {error && <div className="error-message" style={{ marginBottom: 16 }}>{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>제목</label>
            <input type="text" value={title} onChange={e => setTitle(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>설명</label>
            <textarea value={description} onChange={e => setDescription(e.target.value)} rows={4}
              style={{ width: '100%', padding: '12px 14px', border: '1px solid #d5d8dc', borderRadius: 4, fontSize: 14 }} />
          </div>
          <div className="form-group">
            <label>착수일</label>
            <input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>마감일</label>
            <input type="date" value={dueDate} onChange={e => setDueDate(e.target.value)} required />
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button type="submit" className="btn btn-primary">수정</button>
            <button type="button" className="btn" style={{ background: '#6c757d' }} onClick={() => navigate('/dashboard')}>취소</button>
          </div>
        </form>
      </div>
    </>
  );
}
