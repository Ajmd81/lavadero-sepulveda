import api from './api';

const citaService = {
  // Obtener todas las citas
  getAll: () => api.get('/citas'),

  // Obtener cita por ID
  getById: (id) => api.get(`/citas/${id}`),

  // Crear nueva cita
  create: (citaData) => api.post('/citas', citaData),

  // Actualizar cita
  update: (id, citaData) => api.put(`/citas/${id}`, citaData),

  // Eliminar cita
  delete: (id) => api.delete(`/citas/${id}`),

  // Obtener citas por fecha
  getByFecha: (fecha) => api.get(`/citas/fecha/${fecha}`),

  // Obtener citas por estado
  getByEstado: (estado) => api.get(`/citas/estado/${estado}`),

  // Verificar disponibilidad
  checkDisponibilidad: (fecha, hora) =>
    api.get(`/citas/disponibilidad`, { params: { fecha, hora } }),

  // Cambiar estado de cita
  cambiarEstado: (id, estado) =>
    api.put(`/citas/${id}/estado/${estado}`),

  // Marcar como facturada
  marcarFacturada: (id, facturaId) =>
    api.patch(`/citas/${id}/facturar`, { facturaId }),

  // Obtener estadísticas
  getEstadisticas: (fechaInicio, fechaFin) =>
    api.get('/citas/estadisticas', { params: { fechaInicio, fechaFin } }),

  // Obtener tipos de lavado
  getTiposLavado: () => api.get('/tipos-lavado'),
};

export default citaService;
