import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import './HomePage.css'; // We will create this CSS file next

const HomePage: React.FC = () => {
  const { authState } = useAuth();

  return (
    <div className="homepage-container">
      <header className="homepage-header">
        <h1>Welcome to the Digital Library, {authState.user?.username || 'Guest'}!</h1>
        <p>Your gateway to a world of knowledge and stories.</p>
      </header>

      <section className="homepage-actions">
        <h2>Quick Actions</h2>
        <div className="action-cards-container">
          <Link to="/books" className="action-card">
            <h3>Browse Books</h3>
            <p>Explore our extensive collection of books across various genres.</p>
          </Link>
          {/* Placeholder for future features - My Borrows */}
          {/* <Link to="/my-borrows" className="action-card">
            <h3>My Borrows</h3>
            <p>View your currently borrowed books and borrowing history.</p>
          </Link> */}
          {/* Placeholder for future features - Profile */}
          {/* <Link to="/profile" className="action-card">
            <h3>My Profile</h3>
            <p>Manage your account details and preferences.</p>
          </Link> */}
        </div>
      </section>

      {/* Placeholder for a featured books section */}
      {/* 
      <section className="featured-books">
        <h2>Featured Books</h2>
        <div className="featured-books-list">
          <div className="featured-book-item">
            <h4>Book Title 1</h4>
            <p>Short description or author...</p>
          </div>
          <div className="featured-book-item">
            <h4>Book Title 2</h4>
            <p>Short description or author...</p>
          </div>
        </div>
      </section> 
      */}
    </div>
  );
};

export default HomePage; 