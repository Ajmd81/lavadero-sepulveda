import api from './api';

export const gastoService = {
  getAll: () => api.get('/gastos'),
  getById: (id) => api.get(`/gastos/${id}`),
  create: (gastoData) => api.post('/gastos', gastoData),
  update: (id, gastoData) => api.put(`/gastos/${id}`, gastoData),
  delete: (id) => api.delete(`/gastos/${id}`),
  getByFechas: (fechaInicio, fechaFin) => 
    api.get('/gastos/rango', { params: { fechaInicio, fechaFin } }),
};

export const proveedorService = {
  getAll: () => api.get('/proveedores'),
  getById: (id) => api.get(`/proveedores/${id}`),
  create: (proveedorData) => api.post('/proveedores', proveedorData),
  update: (id, proveedorData) => api.put(`/proveedores/${id}`, proveedorData),
  delete: (id) => api.delete(`/proveedores/${id}`),
  search: (query) => api.get(`/proveedores/buscar`, { params: { q: query } }),
};

export const authService = {
  login: (credentials) => api.post('/auth/login', credentials),
  logout: () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
  },
  getCurrentUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },
  isAuthenticated: () => !!localStorage.getItem('authToken'),
};
