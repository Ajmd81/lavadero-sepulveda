import api from './api';

const facturaService = {
  getAll: () => api.get('/facturas'),
  getById: (id) => api.get(`/facturas/${id}`),
  create: (facturaData) => api.post('/facturas', facturaData),
  update: (id, facturaData) => api.put(`/facturas/${id}`, facturaData),
  delete: (id) => api.delete(`/facturas/${id}`),
  generarPdf: (id) => api.get(`/facturas/${id}/pdf`, { responseType: 'blob' }),
  getByCliente: (clienteId) => api.get(`/facturas/cliente/${clienteId}`),
  getByFechas: (fechaInicio, fechaFin) =>
    api.get('/facturas/rango', { params: { fechaInicio, fechaFin } }),
  getEstadisticas: (fechaInicio, fechaFin) =>
    api.get('/facturas/estadisticas', { params: { fechaInicio, fechaFin } }),
};

export default facturaService;