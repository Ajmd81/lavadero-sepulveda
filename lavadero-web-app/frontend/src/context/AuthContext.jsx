import { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const verificarSesion = async () => {
      const token = localStorage.getItem('authToken');

      if (!token) {
        setLoading(false);
        return;
      }

      try {
        // Verificar contra el backend: comprueba expiración y que el token sigue activo en BD
        const response = await authService.verify();
        setUser(response.data);
      } catch {
        // Token expirado, inválido o sesión cerrada desde servidor
        authService.limpiarStorage();
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    verificarSesion();
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

      if (status === 401) {
        return {
          success: false,
          status: 401,
          error: 'Usuario o contraseña incorrectos.',
        };
      }

      return {
        success: false,
        status: status ?? 0,
        error: data?.error || data?.message || 'Error de conexión. Inténtalo de nuevo.',
      };
    }
  };

  const logout = async () => {
    try {
      // Invalidar el token en BD — aunque alguien tenga el token, deja de ser válido
      await authService.logout();
    } catch {
      // Si falla la llamada al backend (sin red, etc.) limpiamos igualmente
    } finally {
      authService.limpiarStorage();
      setUser(null);
      window.location.href = '/login';
    }
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