import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import './LoginPage.css';

const LoginPage = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!name || !email) {
      setError('Enter your name and a valid email to continue.');
      return;
    }

    if (!email.includes('@')) {
      setError('Enter your name and a valid email to continue.');
      return;
    }

    setError('');
    setIsLoading(true);

    try {
      await login(name, email);
      navigate('/projects');
    } catch (err) {
      setError(
        err.message || 'Failed to login. Please check your credentials.'
      );
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="center-page">
      <div className="auth-card">

        <div className="logo-row">
          <div className="login-logo">
            BOOK ILLUSTRATION STUDIO
          </div>
        </div>

        <h2>Sign in to Book Illustration Studio</h2>

        <p className="lede">
          Create and manage your illustrated books with ease.
        </p>

        <form onSubmit={handleSubmit}>

          <div className="gd-field">
            <label htmlFor="name">
              Name <span className="req">*</span>
            </label>

            <input
              id="name"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Enter your name"
              disabled={isLoading}
            />
          </div>

          <div className="gd-field">
            <label htmlFor="email">
              Email address <span className="req">*</span>
            </label>

            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              disabled={isLoading}
            />
          </div>

          {error && (
            <p className="login-error">
              {error}
            </p>
          )}

          <button
            type="submit"
            className="gd-btn gd-btn-primary login-button"
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <span className="login-spinner" />
                Signing in...
              </>
            ) : (
              <>
                Sign In
                <span className="gd-arrow">→</span>
              </>
            )}
          </button>

        </form>
      </div>
    </div>
  );
};

export default LoginPage;