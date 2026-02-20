import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import AppLayout from './components/layout/AppLayout';
import ProtectedRoute from './components/common/ProtectedRoute';
import AdminRoute from './components/common/AdminRoute';
import LoginPage from './pages/LoginPage';
import OtpSetupLoginPage from './pages/OtpSetupLoginPage';
import DashboardPage from './pages/DashboardPage';
import ApprovalHistoryPage from './pages/ApprovalHistoryPage';
import AssignmentNewPage from './pages/AssignmentNewPage';
import AssignmentEditPage from './pages/AssignmentEditPage';
import AssignmentResultPage from './pages/AssignmentResultPage';
import AdminUsersPage from './pages/AdminUsersPage';
import AdminTeamsPage from './pages/AdminTeamsPage';
import AdminLogsPage from './pages/AdminLogsPage';
import OtpSettingsPage from './pages/OtpSettingsPage';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false, refetchOnWindowFocus: false },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/otp-setup" element={<OtpSetupLoginPage />} />
            <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/approval-history" element={<ApprovalHistoryPage />} />
              <Route path="/assignments/new" element={<AssignmentNewPage />} />
              <Route path="/assignments/:id/edit" element={<AssignmentEditPage />} />
              <Route path="/assignments/:id/result" element={<AssignmentResultPage />} />
              <Route path="/otp-settings" element={<OtpSettingsPage />} />
              <Route path="/admin/users" element={<AdminRoute><AdminUsersPage /></AdminRoute>} />
              <Route path="/admin/teams" element={<AdminRoute><AdminTeamsPage /></AdminRoute>} />
              <Route path="/admin/logs" element={<AdminRoute><AdminLogsPage /></AdminRoute>} />
            </Route>
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}
