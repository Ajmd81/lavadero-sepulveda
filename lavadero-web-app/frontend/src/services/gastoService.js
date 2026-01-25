import api from './api';

const gastoService = {
  getAll: () => api.get('/gastos'),
  getById: (id) => api.get(`/gastos/${id}`),
  create: (gastoData) => api.post('/gastos', gastoData),
  update: (id, gastoData) => api.put(`/gastos/${id}`, gastoData),
  delete: (id) => api.delete(`/gastos/${id}`),
  getByFechas: (fechaInicio, fechaFin) => 
    api.get('/gastos/rango', { params: { fechaInicio, fechaFin } }),
  getEstadisticas: (fechaInicio, fechaFin) => 
    api.get('/gastos/estadisticas', { params: { fechaInicio, fechaFin } }),
  getByCategoria: (categoria) => api.get(`/gastos/categoria/${categoria}`),
};

export default gastoService;
