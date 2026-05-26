import { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);
    setLoading(false);
  }, []);

  const login = async (credentials) => {
    try {
      const response = await authService.login(credentials);
      const { token, user } = response.data;

      localStorage.setItem('authToken', token);
      localStorage.setItem('user', JSON.stringify(user));
      setUser(user);

      return { success: true };
    } catch (error) {
      const status = error.response?.status;
      const data = error.response?.data;

      // 429 — demasiados intentos
      if (status === 429) {
        const segundos = data?.retryAfter ?? 60;
        const minutos = Math.ceil(segundos / 60);
        return {
          success: false,
          status: 429,
          retryAfter: segundos,
          error: `Demasiados intentos fallidos. Espera ${minutos} minuto${minutos !== 1 ? 's' : ''} antes de intentarlo de nuevo.`,
        };
      }

      // 401 — credenciales incorrectas
      if (status === 401) {
        return {
          success: false,
          status: 401,
          error: 'Usuario o contraseña incorrectos.',
        };
      }

      // Cualquier otro error (red, servidor caído, etc.)
      return {
        success: false,
        status: status ?? 0,
        error: data?.error || data?.message || 'Error de conexión. Inténtalo de nuevo.',
      };
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const value = {
    user,
    login,
    logout,
    isAuthenticated: !!user,
    loading,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return context;
};