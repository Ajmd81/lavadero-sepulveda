import { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import { authService } from '../services/api';
import { tokenStorage } from '../services/tokenStorage';

const AuthContext = createContext(null);

// ─── Política de sesión ─────────────────────────────────────────────────────────
// - Sin actividad (ratón, teclado, toques, scroll) durante INACTIVIDAD_MS → se cierra sesión.
// - Con actividad, el token (30 min en el backend) se renueva cuando le quedan < RENOVAR_ANTES_MS.
// - El backend corta igualmente a las 8 h desde el login (máximo absoluto).
const INACTIVIDAD_MS = 30 * 60 * 1000;
const RENOVAR_ANTES_MS = 5 * 60 * 1000;
const INTERVALO_COMPROBACION_MS = 30 * 1000;
const EVENTOS_ACTIVIDAD = ['mousemove', 'mousedown', 'keydown', 'touchstart', 'scroll'];

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const ultimaActividad = useRef(Date.now());
  const renovando = useRef(false);

  const cerrarSesionLocal = useCallback((motivo) => {
    tokenStorage.limpiar();
    setUser(null);
    const sufijo = motivo ? `?${motivo}=1` : '';
    window.location.href = `/admin/login${sufijo}`;
  }, []);

  // ─── Verificar sesión al cargar ─────────────────────────────────────────────
  useEffect(() => {
    const verificarSesion = async () => {
      if (!tokenStorage.hayToken()) {
        setLoading(false);
        return;
      }
      try {
        const response = await authService.verify();
        setUser(response.data);
      } catch {
        tokenStorage.limpiar();
        setUser(null);
      } finally {
        setLoading(false);
      }
    };
    verificarSesion();
  }, []);

  // ─── Inactividad y renovación del token ─────────────────────────────────────
  useEffect(() => {
    if (!user) return undefined;

    const marcarActividad = () => {
      ultimaActividad.current = Date.now();
    };
    EVENTOS_ACTIVIDAD.forEach((ev) => window.addEventListener(ev, marcarActividad, { passive: true }));

    const comprobar = async () => {
      const ahora = Date.now();

      if (ahora - ultimaActividad.current > INACTIVIDAD_MS) {
        try {
          await authService.logout();
        } catch {
          /* el token puede haber caducado ya */
        }
        cerrarSesionLocal('inactividad');
        return;
      }

      const expiraEn = tokenStorage.getExpiresAt() - ahora;
      if (expiraEn < RENOVAR_ANTES_MS && !renovando.current) {
        renovando.current = true;
        try {
          const { data } = await authService.refresh();
          tokenStorage.guardar(data);
        } catch {
          // 401: máximo de sesión alcanzado o token revocado → el interceptor ya redirige al login
        } finally {
          renovando.current = false;
        }
      }
    };

    const intervalo = setInterval(comprobar, INTERVALO_COMPROBACION_MS);
    return () => {
      clearInterval(intervalo);
      EVENTOS_ACTIVIDAD.forEach((ev) => window.removeEventListener(ev, marcarActividad));
    };
  }, [user, cerrarSesionLocal]);

  // ─── Login ──────────────────────────────────────────────────────────────────
  const login = async (credentials) => {
    try {
      const response = await authService.login(credentials);
      tokenStorage.guardar(response.data); // { token, user, expiresAt }
      ultimaActividad.current = Date.now();
      setUser(response.data.user);
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
        return { success: false, status: 401, error: 'Usuario o contraseña incorrectos.' };
      }

      if (status === 400) {
        return { success: false, status: 400, error: data?.error || 'Revisa el usuario y la contraseña.' };
      }

      return {
        success: false,
        status: status ?? 0,
        error: data?.error || data?.message || 'Error de conexión. Inténtalo de nuevo.',
      };
    }
  };

  // ─── Logout ─────────────────────────────────────────────────────────────────
  // Revoca el token en el servidor: aunque alguien lo hubiera copiado, deja de valer.
  const logout = async () => {
    try {
      await authService.logout();
    } catch {
      // Sin red, etc.: limpiamos igualmente
    } finally {
      cerrarSesionLocal();
    }
  };

  /** Cierra la sesión en todos los dispositivos (ordenador del lavadero, móvil...). */
  const logoutEnTodos = async () => {
    try {
      await authService.logoutAll();
    } finally {
      cerrarSesionLocal();
    }
  };

  /** Tras cambiar la contraseña el backend invalida todas las sesiones y devuelve un token nuevo. */
  const actualizarToken = (data) => tokenStorage.guardar(data);

  const value = {
    user,
    login,
    logout,
    logoutEnTodos,
    actualizarToken,
    isAuthenticated: !!user,
    loading,
  };

  return <AuthContext.Provider value={value}>{!loading && children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return context;
};
