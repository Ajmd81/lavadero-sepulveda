import axios from 'axios';

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
    const token = localStorage.getItem('authToken');
    if (token) {
      // JWT estándar: "Bearer <token>"
      config.headers.Authorization = `Bearer ${token}`;
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

    // Redirigir al login si el JWT expiró o es inválido,
    // pero nunca en el propio endpoint de login (evita bucle)
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

export const authService = {
  login: (credentials) => api.post('/auth/login', credentials),

  // Con JWT stateless el logout real es eliminar el token en cliente.
  // Llamamos igualmente al backend para tener un punto de cierre limpio.
  logout: () => api.post('/auth/logout'),

  // Verifica el JWT contra el backend (firma + expiración + usuario activo en BD)
  verify: () => api.get('/auth/verify'),

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