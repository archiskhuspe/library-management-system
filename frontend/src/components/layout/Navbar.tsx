import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './Navbar.css'; // We'll create this CSS file next

const Navbar: React.FC = () => {
  const { authState, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/" className="navbar-item brand-text">Library App</Link>
      </div>
      <div className="navbar-menu">
        <div className="navbar-start">
          {authState.isAuthenticated && (
            <Link to="/books" className="navbar-item">Books</Link>
          )}
          {/* Add other authenticated links here, e.g., My Borrows, Profile */}
        </div>
        <div className="navbar-end">
          {authState.isAuthenticated ? (
            <>
              <div className="navbar-item">
                <span>Welcome, {authState.user?.username || 'User'}</span>
              </div>
              <div className="navbar-item">
                <button onClick={handleLogout} className="button is-light">
                  Logout
                </button>
              </div>
            </>
          ) : (
            <>
              <Link to="/login" className="navbar-item">Login</Link>
              <Link to="/signup" className="navbar-item">Sign Up</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar; 