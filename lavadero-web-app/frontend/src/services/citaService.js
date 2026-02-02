import api from './api';

const citaService = {
  // CRUD Básico
  getAll: () => api.get('/citas'),
  getById: (id) => api.get(`/citas/${id}`),
  create: (citaData) => api.post('/citas', citaData),
  update: (id, citaData) => api.put(`/citas/${id}`, citaData),
  delete: (id) => api.delete(`/citas/${id}`),

  // Búsquedas por fecha
  getByFecha: (fecha) => api.get(`/citas/fecha/${fecha}`),
  getByRango: (fechaInicio, fechaFin) =>
    api.get('/citas/rango', { params: { inicio: fechaInicio, fin: fechaFin } }),
  
  // Estados
  getByEstado: (estado) => api.get(`/citas/estado/${estado}`),
  getPendientes: () => api.get('/citas/pendientes'),
  getNoFacturadas: () => api.get('/citas/no-facturadas'),
  getEnProceso: () => api.get('/citas/en-proceso'),
  getHoy: () => api.get('/citas/hoy'),
  
  // ⭐ NUEVO: Cambiar estado de una cita
  cambiarEstado: (id, nuevoEstado) => api.put(`/citas/${id}/estado/${nuevoEstado}`),

  // Cliente
  getByClienteId: (clienteId) => api.get(`/citas/cliente-id/${clienteId}`),
  getByClienteTelefono: (telefono) => api.get(`/citas/cliente/${telefono}`),

  // Disponibilidad
  checkDisponibilidad: (fecha, hora) =>
    api.get('/citas/verificar-disponibilidad', { params: { fecha, hora } }),
  getHorariosDisponibles: (fecha) =>
    api.get('/citas/horarios-disponibles', { params: { fecha } }),
  getDisponibilidadMensual: (mes, anio, tipoLavado) =>
    api.get('/citas/disponibilidad-mensual', { params: { mes, anio, tipoLavado } }),
  
  // Citas por fecha (agrupadas)
  getCitasPorFecha: () => api.get('/citas/por-fecha'),

  // Servicios
  getTiposLavado: () => api.get('/tipos-lavado'),

  // Estadísticas
  getEstadisticas: (fecha) =>
    api.get('/citas/estadisticas', { params: { fecha } }),

  // Migraciones
  migrarEmail: (datos) => api.post('/citas/migrar-email', datos),
};

export default citaService;