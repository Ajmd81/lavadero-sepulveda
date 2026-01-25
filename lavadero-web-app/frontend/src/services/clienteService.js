import api from './api';

const clienteService = {
  getAll: () => api.get('/clientes'),
  getById: (id) => api.get(`/clientes/${id}`),
  create: (clienteData) => api.post('/clientes', clienteData),
  update: (id, clienteData) => api.put(`/clientes/${id}`, clienteData),
  delete: (id) => api.delete(`/clientes/${id}`),
  search: (query) => api.get(`/clientes/buscar`, { params: { q: query } }),
  getEstadisticas: (id) => api.get(`/clientes/${id}/estadisticas`),
  getCitas: (id) => api.get(`/clientes/${id}/citas`),
  getFacturas: (id) => api.get(`/clientes/${id}/facturas`),
};

export default clienteService;
