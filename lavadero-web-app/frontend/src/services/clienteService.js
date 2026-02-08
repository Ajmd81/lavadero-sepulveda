import api from './api';

const clienteService = {
  // CRUD Básico
  getAll: () => api.get('/clientes'),
  getById: (id) => api.get(`/clientes/${id}`),
  create: (clienteData) => api.post('/clientes', clienteData),
  update: (id, clienteData) => api.put(`/clientes/${id}`, clienteData),
  delete: (id) => api.delete(`/clientes/${id}`),

  // Búsqueda
  search: (query) => api.get('/clientes/buscar', { params: { q: query } }),

  // Relaciones
  getCitas: (id) => api.get(`/clientes/${id}/citas`),
  getFacturas: (id) => api.get(`/clientes/${id}/facturas`),
  getEstadisticas: (id) => api.get(`/clientes/${id}/estadisticas`),

  // Filtrados
  getActivos: () => api.get('/clientes?activos=true'),

  // Paginación
  getAllPaginated: async (page = 0, size = 10, sortBy = 'nombre', sortDir = 'asc') => {
    return await api.get('/clientes', {
      params: { page, size, sortBy, sortDir }
    });
  },
};

export default clienteService;
