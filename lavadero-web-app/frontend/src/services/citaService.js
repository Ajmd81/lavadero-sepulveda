import api from './api';

const citaService = {
  // CRUD Básico
  getAll: () => api.get('/api/citas'),
  getById: (id) => api.get(`/api/citas/${id}`),
  create: (citaData) => api.post('/api/citas', citaData),
  update: (id, citaData) => api.put(`/api/citas/${id}`, citaData),
  delete: (id) => api.delete(`/api/citas/${id}`),

  // Búsquedas por fecha
  getByFecha: (fecha) => api.get(`/api/citas/fecha/${fecha}`),
  getByRango: (fechaInicio, fechaFin) =>
    api.get('/api/citas/rango', { params: { inicio: fechaInicio, fin: fechaFin } }),
  
  // Estados
  getByEstado: (estado) => api.get(`/api/citas/estado/${estado}`),
  getPendientes: () => api.get('/api/citas/pendientes'),
  getNoFacturadas: () => api.get('/api/citas/no-facturadas'),
  getEnProceso: () => api.get('/api/citas/en-proceso'),
  getHoy: () => api.get('/api/citas/hoy'),

  // Cliente
  getByClienteId: (clienteId) => api.get(`/api/citas/cliente-id/${clienteId}`),
  getByClienteTelefono: (telefono) => api.get(`/api/citas/cliente/${telefono}`),

  // Disponibilidad
  checkDisponibilidad: (fecha, hora) =>
    api.get('/api/citas/verificar-disponibilidad', { params: { fecha, hora } }),
  getHorariosDisponibles: (fecha) =>
    api.get('/api/citas/horarios-disponibles', { params: { fecha } }),
  getDisponibilidadMensual: (mes, anio, tipoLavado) =>
    api.get('/api/citas/disponibilidad-mensual', { params: { mes, anio, tipoLavado } }),
  
  // Citas por fecha (agrupadas)
  getCitasPorFecha: () => api.get('/api/citas/por-fecha'),

  // Servicios
  getTiposLavado: () => api.get('/api/tipos-lavado'),

  // Estadísticas
  getEstadisticas: (fecha) =>
    api.get('/api/citas/estadisticas', { params: { fecha } }),

  // Migraciones
  migrarEmail: (datos) => api.post('/api/citas/migrar-email', datos),
};

export default citaService;
