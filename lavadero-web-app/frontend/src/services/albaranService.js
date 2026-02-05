import api from './api';

const API_URL = '/albaranes';

export const albaranService = {
  // Obtener todos los albaranes
  getAll: async () => {
    const response = await api.get(API_URL);
    return response.data;
  },

  // Obtener albarán por ID
  getById: async (id) => {
    const response = await api.get(`${API_URL}/${id}`);
    return response.data;
  },

  // Obtener albaranes por cliente
  getByCliente: async (clienteId) => {
    const response = await api.get(`${API_URL}/cliente/${clienteId}`);
    return response.data;
  },

  // Obtener albaranes por estado
  getByEstado: async (estado) => {
    const response = await api.get(`${API_URL}/estado/${estado}`);
    return response.data;
  },

  // Obtener albaranes pendientes de facturar
  getPendientesFacturar: async (clienteId) => {
    const response = await api.get(`${API_URL}/pendientes-facturar/${clienteId}`);
    return response.data;
  },

  // Crear albarán
  create: async (albaranData) => {
    const response = await api.post(API_URL, albaranData);
    return response.data;
  },

  // Actualizar albarán
  update: async (id, albaranData) => {
    const response = await api.put(`${API_URL}/${id}`, albaranData);
    return response.data;
  },

  // Cambiar estado
  cambiarEstado: async (id, estado) => {
    const response = await api.patch(`${API_URL}/${id}/estado`, null, {
      params: { estado }
    });
    return response.data;
  },

  // Eliminar albarán
  delete: async (id) => {
    const response = await api.delete(`${API_URL}/${id}`);
    return response.data;
  }
};