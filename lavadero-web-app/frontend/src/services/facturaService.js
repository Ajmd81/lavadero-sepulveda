import api from './api';

const facturaService = {
  // CRUD Básico
  getAll: () => api.get('/facturas'),
  getById: (id) => api.get(`/facturas/${id}`),
  create: (facturaData) => api.post('/facturas', facturaData),
  createManual: (facturaData, params) => 
    api.post('/facturas/manual', facturaData, { params }),
  delete: (id) => api.delete(`/facturas/${id}`),

  // Búsquedas
  getByCliente: (clienteId) => api.get(`/facturas/cliente/${clienteId}`),
  getByFechas: (fechaInicio, fechaFin) =>
    api.get('/facturas/fecha', { params: { fechaInicio, fechaFin } }),
  getByEstado: (estado) => api.get(`/facturas/estado/${estado}`),
  buscar: (termino) => api.get('/facturas/buscar', { params: { q: termino } }),

  // Estados y filtros
  getPendientes: () => api.get('/facturas/pendientes'),
  getHoy: () => api.get('/facturas/hoy'),

  // Operaciones
  marcarComoPagada: (id) => api.put(`/facturas/${id}/pagar`),
  getResumen: () => api.get('/facturas/resumen'),
  getEmisor: () => api.get('/facturas/emisor'),

  // Número de factura
  getByNumero: (numero) => api.get(`/facturas/numero/${numero}`),
};

export default facturaService;