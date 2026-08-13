import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import './Header.css';

const Header = () => {
  const { isAuthenticated, user, logout } = useAuth();

  const initials = user?.name
    ? user.name
        .split(' ')
        .map((word) => word[0])
        .join('')
        .slice(0, 2)
        .toUpperCase()
    : '?';

  return (
    <header className="gd-nav">
      <div className="gd-nav-inner">

        <Link to="/" className="gd-nav-logo">
          Book Illustrator
        </Link>

        {isAuthenticated && (
          <>
            <nav className="gd-nav-links">
              <Link to="/">Home</Link>
              <Link to="/projects">Projects</Link>
            </nav>

            <div className="gd-nav-user">
              <div className="gd-nav-avatar">
                {initials}
              </div>

              <span>{user?.name}</span>

              <button
                type="button"
                className="gd-signout"
                onClick={logout}
              >
                Sign out
              </button>
            </div>
          </>
        )}

      </div>
    </header>
  );
};

export default Header;