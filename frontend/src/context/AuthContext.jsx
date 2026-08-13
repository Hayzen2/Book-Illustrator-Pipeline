import React, {
    createContext,
    useState,
    useEffect
} from 'react';

import { loginUser } from '../api/auth.js';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(
        localStorage.getItem('authToken')
    );
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (token) {
            const storedUser = localStorage.getItem('user');

            if (storedUser) {
                try {
                    setUser(JSON.parse(storedUser));
                } catch (error) {
                    console.error(
                        'Failed to parse stored user:',
                        error
                    );

                    localStorage.removeItem('user');
                }
            }
        }

        setLoading(false);
    }, [token]);

    const login = async (name, email) => {
        const response = await loginUser(name, email);

        console.log('Login API response:', response);

        const authData = response?.data;

        if (!authData?.token) {
            throw new Error(
                response?.message ||
                'Login failed: Invalid response from server.'
            );
        }

        const user = {
            name,
            email: authData.email,
        };

        setToken(authData.token);
        setUser(user);

        localStorage.setItem(
            'authToken',
            authData.token
        );

        localStorage.setItem(
            'user',
            JSON.stringify(user)
        );
    };

    const logout = () => {
        setToken(null);
        setUser(null);

        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
    };

    const value = {
        user,
        token,
        isAuthenticated: !!token,
        loading,
        login,
        logout,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};