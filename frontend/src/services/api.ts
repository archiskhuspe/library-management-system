import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api/v1', // Your Spring Boot backend API base URL
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor to add the JWT token to requests
apiClient.interceptors.request.use(
  (config) => {
    // We'll retrieve the token from localStorage for now.
    // Later, this could come from a state management solution or an auth context.
    const token = localStorage.getItem('authToken'); 
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Optional: Interceptor to handle responses (e.g., for global error handling like 401)
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      // Handle 401 Unauthorized: e.g., redirect to login, clear token
      console.error('Unauthorized! Redirecting to login or clearing token...');
      // localStorage.removeItem('authToken');
      // window.location.href = '/login'; // Or use React Router for navigation
    }
    return Promise.reject(error);
  }
);

export default apiClient; 