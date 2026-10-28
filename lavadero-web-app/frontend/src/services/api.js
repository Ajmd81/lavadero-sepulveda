import axios from 'axios';
import { tokenStorage } from './tokenStorage';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ─── REQUEST: adjuntar JWT ────────────────────────────────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = tokenStorage.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── RESPONSE: manejar 401 ────────────────────────────────────────────────────
// El backend responde 401 cuando el token falta, caduca, se revocó (logout / cambio de
// contraseña) o el usuario se desactivó. En todos esos casos hay que volver al login.
const RUTAS_SIN_REDIRECCION = ['/auth/login', '/auth/logout'];

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const url = error.config?.url ?? '';

    if (status === 401 && !RUTAS_SIN_REDIRECCION.some((r) => url.includes(r))) {
      tokenStorage.limpiar();
      if (!window.location.pathname.startsWith('/admin/login')) {
        window.location.href = '/admin/login?expirada=1';
      }
    }

    return Promise.reject(error);
  }
);

export default api;

// ─── authService ──────────────────────────────────────────────────────────────

export const authService = {
  login: (credentials) => api.post('/auth/login', credentials),

  // Revoca en el servidor el token de este dispositivo
  logout: () => api.post('/auth/logout'),

  // Cierra la sesión en todos los dispositivos
  logoutAll: () => api.post('/auth/logout-all'),

  // Renueva el token (solo funciona si el actual sigue siendo válido y no se superó el máximo de sesión)
  refresh: () => api.post('/auth/refresh'),

  // Verifica el JWT contra el backend (firma + expiración + revocación + usuario activo)
  verify: () => api.get('/auth/verify'),

  limpiarStorage: () => tokenStorage.limpiar(),

  getCurrentUser: () => tokenStorage.getUser(),

  isAuthenticated: () => tokenStorage.hayToken(),
};
