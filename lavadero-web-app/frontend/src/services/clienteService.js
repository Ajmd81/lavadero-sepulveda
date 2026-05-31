import api from './api';

const clienteService = {
  getAll: () => api.get('/clientes'),
  getById: (id) => api.get(`/clientes/${id}`),
  create: (clienteData) => api.post('/clientes', clienteData),
  update: (id, clienteData) => api.put(`/clientes/${id}`, clienteData),
  delete: (id) => api.delete(`/clientes/${id}`),

  search: (query) => api.get('/clientes/buscar', { params: { q: query } }),

  getCitas: (id) => api.get(`/clientes/${id}/citas`),
  getFacturas: (id) => api.get(`/clientes/${id}/facturas`),
  getEstadisticas: (id) => api.get(`/clientes/${id}/estadisticas`),

  getActivos: () => api.get('/clientes?activos=true'),

  getAllPaginated: (page = 0, size = 10, sortBy = 'nombre', sortDir = 'asc') =>
    api.get('/clientes', { params: { page, size, sortBy, sortDir } }),

  // Solo pide 1 registro — usa totalElements para contar sin cargar todo
  getCount: () => api.get('/clientes', { params: { page: 0, size: 1 } }),
};

export default clienteService;