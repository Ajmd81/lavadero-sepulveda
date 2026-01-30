import api from './api';

const proveedorService = {
  // CRUD Básico
  getAll: () => api.get('/proveedores'),
  getActivos: () => api.get('/proveedores'), // Mismo que getAll pero filtrado
  getTodos: () => api.get('/proveedores/todos'),
  getById: (id) => api.get(`/proveedores/${id}`),
  create: (proveedorData) => api.post('/proveedores', proveedorData),
  update: (id, proveedorData) => api.put(`/proveedores/${id}`, proveedorData),
  delete: (id) => api.delete(`/proveedores/${id}`),

  // Búsqueda
  search: (query) => api.get('/proveedores/buscar', { params: { termino: query } }),

  // Estados
  activar: (id) => api.put(`/proveedores/${id}/activar`),
  desactivar: (id) => api.put(`/proveedores/${id}/desactivar`),
};

export default proveedorService;