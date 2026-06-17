import React, { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react'; // Explicit type import for ReactNode
import { jwtDecode } from 'jwt-decode'; 

// Define the shape of the user object we might get from the token or API
interface User {
  id: number;
  username: string;
  email: string;
  roles: string[];
}

// Define the shape of our authentication state
interface AuthState {
  token: string | null;
  user: User | null;
  isAuthenticated: boolean;
  roles: string[];
}

// Define the shape of the context value
interface AuthContextType {
  authState: AuthState;
  login: (token: string, userData?: User) => void; // userData is from backend JwtResponse, now optional
  logout: () => void;
}


const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

const initialAuthState: AuthState = {
    token: null,
    user: null,
    isAuthenticated: false,
    roles: [],
};

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [authState, setAuthState] = useState<AuthState>(initialAuthState);

  useEffect(() => {
    const token = localStorage.getItem('authToken');
    const storedUser = localStorage.getItem('authUser');

    if (token && storedUser) {
      try {
        const decodedToken = jwtDecode<any>(token); 
        
        if (decodedToken.exp * 1000 > Date.now()) {
          const parsedUser: User = JSON.parse(storedUser);
          setAuthState({
            token: token,
            user: parsedUser,
            isAuthenticated: true,
            roles: parsedUser.roles || [],
          });
        } else {
          localStorage.removeItem('authToken');
          localStorage.removeItem('authUser');
        }
      } catch (error) {
        console.error("Failed to initialize auth state from localStorage:", error);
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUser');
        setAuthState(initialAuthState); // Reset to initial if error
      }
    } else {
        // If token or user is missing, ensure logged out state
        setAuthState(initialAuthState);
    }
  }, []);

  const login = (token: string, userData?: User) => { // userData is optional to match type
    localStorage.setItem('authToken', token);
    
    let userToStore: User | null = null; 
    let rolesToStore: string[] = [];

    if (userData) { 
        userToStore = userData;
        rolesToStore = userData.roles || [];
        localStorage.setItem('authUser', JSON.stringify(userData));
    } else { 
        try {
            const decodedToken = jwtDecode<any>(token);
            userToStore = { 
                id: decodedToken.id || 0, 
                username: decodedToken.sub, 
                email: decodedToken.email || '', 
                roles: decodedToken.roles || [] 
            };
            rolesToStore = decodedToken.roles || [];
        } catch (e) {
            console.error("Failed to decode token during login fallback:", e);
            logout(); 
            return;
        }
    }

    if (userToStore) { 
        setAuthState({
          token: token,
          user: userToStore,
          isAuthenticated: true,
          roles: rolesToStore,
        });
    }
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('authUser');
    setAuthState(initialAuthState);
    // Optionally, redirect to login page or homepage
    // Example: window.location.href = '/login'; 
  };

  return (
    <AuthContext.Provider value={{ authState, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}; 