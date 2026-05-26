import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ─── REQUEST: adjuntar token ──────────────────────────────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = token; // el token ya incluye el prefijo "Bearer-token-..."
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── RESPONSE: manejar 401 ────────────────────────────────────────────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const url = error.config?.url ?? '';

    // 401 en cualquier endpoint salvo el propio login (evita bucle de redirección)
    if (status === 401 && !url.includes('/auth/login')) {
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }

    return Promise.reject(error);
  }
);

export default api;


// ─── authService ──────────────────────────────────────────────────────────────
// Centralizado aquí para que api.js y services/index.js no dupliquen lógica.

export const authService = {
  login: (credentials) => api.post('/auth/login', credentials),

  // Llama al backend para invalidar el token en BD
  logout: () => api.post('/auth/logout'),

  // Verifica el token activo contra el backend (expiración + existencia en BD)
  verify: () => api.get('/auth/verify'),

  // Solo limpia el storage local — úsalo cuando el backend no está disponible
  limpiarStorage: () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
  },

  getCurrentUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  isAuthenticated: () => !!localStorage.getItem('authToken'),
};