import React from 'react';
import { Route, Routes, Navigate } from 'react-router-dom';
import { useAuth } from './hooks/useAuth';
import Header from './components/layout/Header';
import Footer from './components/layout/Footer';
import LoginPage from './pages/LoginPage';

const ProjectsPage = () => (
  <div className="p-8">
    <h1>Your Projects Dashboard</h1>
  </div>
);

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div className="p-8">Loading session...</div>;
  }

  return isAuthenticated
    ? children
    : <Navigate to="/login" replace />;
};

function App() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="min-h-screen flex flex-col bg-gray-100">

      {/* Header */}
      {isAuthenticated && <Header />}

      {/* Main content */}
      <main className="flex-1">
        <Routes>
          <Route
            path="/login"
            element={<LoginPage />}
          />

          <Route
            path="/projects"
            element={
              <ProtectedRoute>
                <ProjectsPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/"
            element={
              isAuthenticated
                ? <Navigate to="/projects" replace />
                : <Navigate to="/login" replace />
            }
          />
        </Routes>
      </main>

      {/* Footer */}
      <Footer />

    </div>
  );
}

export default App;