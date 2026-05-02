import { createContext, useState, useEffect, useContext } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import api from '../configs/api';

export const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const location = useLocation();

  const login = () => {
    setIsAuthenticated(true);
  };

  const logout = async () => {
    try {
      await api.post('/logout');
    } catch (error) {
      console.error("Logout failed:", error.message);
    } finally {
      setIsAuthenticated(false);
      setUserData(null);
      navigate('/login');
    }
  };

  useEffect(() => {
    const checkAuthStatus = async () => {
      try {
        const response = await api.get('/user');

        if (response.status === 401) {
          setIsAuthenticated(false);
          setUserData(null);
          return;
        }

        if (response.status === 200) {
          const user = response.data.user;
          setUserData(user);
          setIsAuthenticated(true);
        }
      } catch (error) {
        console.error("Authentication check failed:", error.message);
        setIsAuthenticated(false);
        setUserData(null);
      } finally {
        setLoading(false);
      }
    };

    checkAuthStatus();
  }, [isAuthenticated]);

  useEffect(() => {
    if (loading) return;

    if (!isAuthenticated && location.pathname !== '/login' && location.pathname !== '/') {
      navigate('/login');
    } else if (isAuthenticated && location.pathname === '/login') {
      if (userData?.role === 'user') {
        navigate('/user/subscriptions');
      } else {
        navigate('/admin/customers');
      }
    } else if (isAuthenticated && userData?.role === 'user' && location.pathname.startsWith('/admin')) {
      navigate('/user/subscriptions');
    }
  }, [isAuthenticated, location.pathname, navigate, loading, userData]);

  return (
    <AuthContext.Provider value={{ isAuthenticated, userData, login, logout }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
