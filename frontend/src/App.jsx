import React from 'react';
import { Route, Routes, Navigate } from 'react-router-dom';

import { useAuth } from './hooks/useAuth';

import Header from './components/layout/Header';
import Footer from './components/layout/Footer';

import LoginPage from './pages/LoginPage';
import Homepage from './pages/Homepage';
import ProjectDetailPage from './pages/ProjectDetailPage';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <div className="text-gray-500">Loading session...</div>
      </div>
    );
  }

  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

function App() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      {isAuthenticated && <Header />}

      <main className="flex-1">
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Homepage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/projects/:projectId"
            element={
              <ProtectedRoute>
                <ProjectDetailPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="*"
            element={<Navigate to="/" replace />}
          />
        </Routes>
      </main>

      {isAuthenticated && <Footer />}
    </div>
  );
}

export default App;