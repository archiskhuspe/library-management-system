import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import { useAuth } from './contexts/AuthContext';
import './App.css';
import BooksPage from './pages/BooksPage';
import MainLayout from './layouts/MainLayout'; // Import MainLayout
import HomePage from './pages/HomePage'; // Import the new HomePage component

// A component to handle protected routes
interface ProtectedRouteProps {
  children: React.ReactElement;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { authState } = useAuth();
  if (!authState.isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  // Wrap protected children with MainLayout
  return <MainLayout>{children}</MainLayout>;
};


function App() {
  const { authState } = useAuth();

  return (
    <BrowserRouter>
      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="colored"
      />
      {/* The Navbar is now part of MainLayout, so no global nav here */}
      <Routes>
        {/* Routes for Login and Signup do not use MainLayout */}
        <Route 
          path="/login" 
          element={!authState.isAuthenticated ? <LoginPage /> : <Navigate to="/" />} 
        />
        <Route 
          path="/signup" 
          element={!authState.isAuthenticated ? <SignupPage /> : <Navigate to="/" />}
        />
        
        {/* Protected routes now use MainLayout via ProtectedRoute */}
        <Route 
          path="/"
          element={
            <ProtectedRoute>
              <HomePage />
            </ProtectedRoute>
          } 
        />
        <Route
          path="/books"
          element={
            <ProtectedRoute>
              <BooksPage />
            </ProtectedRoute>
          }
        />
        {/* Add other protected routes here, they will automatically get the MainLayout */}
        
        {/* Example of a route that might not need the MainLayout, if any */}
        {/* <Route path="/some-public-page" element={<SomePublicPageComponent />} /> */}

        {/* Catch-all for undefined routes - consider a dedicated NotFoundPage */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
