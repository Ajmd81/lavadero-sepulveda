import api from './api';

const proveedorService = {
  getAll: () => api.get('/proveedores/todos'),
  getById: (id) => api.get(`/proveedores/${id}`),
  create: (proveedorData) => api.post('/proveedores', proveedorData),
  update: (id, proveedorData) => api.put(`/proveedores/${id}`, proveedorData),
  delete: (id) => api.delete(`/proveedores/${id}`),
  search: (query) => api.get('/proveedores/buscar', { params: { termino: query } }),
  getActivos: () => api.get('/proveedores'),
};

export default proveedorService;