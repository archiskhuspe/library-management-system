import React from 'react';
import Navbar from '../components/layout/Navbar';
import './MainLayout.css'; // We'll create this CSS file next

interface MainLayoutProps {
  children: React.ReactNode;
}

const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  return (
    <div className="main-layout">
      <Navbar />
      <main className="main-content">
        {children}
      </main>
      {/* You could add a Footer component here if needed */}
    </div>
  );
};

export default MainLayout; 