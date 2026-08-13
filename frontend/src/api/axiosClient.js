import axios from 'axios';

// Initialize axios instance with base URL and headers
const axiosClient = axios.create({
    baseURL: 'http://localhost:8080',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Interceptor: Before sending the request, attach the token from localStorage to the Authorization header
axiosClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('authToken'); // Retrieve the token from localStorage
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default axiosClient;