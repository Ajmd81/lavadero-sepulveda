import api from './api';

const facturaRecibidaService = {
  getAll: () => api.get('/facturas-recibidas'),
  getById: (id) => api.get(`/facturas-recibidas/${id}`),
  create: (facturaData) => api.post('/facturas-recibidas', facturaData),
  update: (id, facturaData) => api.put(`/facturas-recibidas/${id}`, facturaData),
  delete: (id) => api.delete(`/facturas-recibidas/${id}`),
  getByProveedor: (proveedorId) => api.get(`/facturas-recibidas/proveedor/${proveedorId}`),
  getByFechas: (fechaInicio, fechaFin) =>
    api.get('/facturas-recibidas/rango', { params: { fechaInicio, fechaFin } }),
  getEstadisticas: (fechaInicio, fechaFin) =>
    api.get('/facturas-recibidas/estadisticas', { params: { fechaInicio, fechaFin } }),
};

export default facturaRecibidaService;