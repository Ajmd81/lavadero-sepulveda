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

  // Paginación
  getActivosPaginated: async (page = 0, size = 10, sortBy = 'nombre', sortDir = 'asc') => {
    return await api.get('/proveedores/activos', {
      params: { page, size, sortBy, sortDir }
    });
  },
};

export default proveedorService;