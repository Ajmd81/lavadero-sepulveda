import api from './api';

const facturaService = {
  // CRUD Básico
  getAll: () => api.get('/facturas'),
  getById: (id) => api.get(`/facturas/${id}`),
  create: (facturaData) => api.post('/facturas', facturaData),
  createManual: (facturaData, params) => 
    api.post('/facturas/manual', facturaData, { params }),
  update: (id, facturaData) => 
    api.put(`/facturas/${id}`, facturaData),
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

  // Operaciones - Pagos
  marcarComoPagada: (id, dataPago) => 
    api.put(`/facturas/${id}/pagar`, dataPago),

  // Otros
  getResumen: () => api.get('/facturas/resumen'),
  getEmisor: () => api.get('/facturas/emisor'),

  // Número de factura
  getByNumero: (numero) => api.get(`/facturas/numero/${numero}`),
};

export default facturaService;