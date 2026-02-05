import api from './api';

const API_URL = '/albaranes';

export const albaranService = {
  // Obtener todos los albaranes
  getAll: async () => {
    try {
      const response = await api.get(API_URL);
      return response.data;
    } catch (error) {
      console.error('Error en getAll:', error);
      return []; // Devolver array vacío en caso de error
    }
  },

  // Obtener albarán por ID
  getById: async (id) => {
    try {
      const response = await api.get(`${API_URL}/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error en getById:', error);
      throw error;
    }
  },

  // Obtener albaranes por cliente
  getByCliente: async (clienteId) => {
    try {
      const response = await api.get(`${API_URL}/cliente/${clienteId}`);
      return response.data;
    } catch (error) {
      console.error('Error en getByCliente:', error);
      return [];
    }
  },

  // Obtener albaranes por estado
  getByEstado: async (estado) => {
    try {
      const response = await api.get(`${API_URL}/estado/${estado}`);
      return response.data;
    } catch (error) {
      console.error('Error en getByEstado:', error);
      return [];
    }
  },

  // Obtener albaranes pendientes de facturar
  getPendientesFacturar: async (clienteId) => {
    try {
      const response = await api.get(`${API_URL}/pendientes-facturar/${clienteId}`);
      return response.data;
    } catch (error) {
      console.error('Error en getPendientesFacturar:', error);
      return [];
    }
  },

  // Crear albarán
  create: async (albaranData) => {
    try {
      const response = await api.post(API_URL, albaranData);
      return response.data;
    } catch (error) {
      console.error('Error en create:', error);
      throw error;
    }
  },

  // Actualizar albarán
  update: async (id, albaranData) => {
    try {
      const response = await api.put(`${API_URL}/${id}`, albaranData);
      return response.data;
    } catch (error) {
      console.error('Error en update:', error);
      throw error;
    }
  },

  // Cambiar estado
  cambiarEstado: async (id, estado) => {
    try {
      const response = await api.patch(`${API_URL}/${id}/estado`, null, {
        params: { estado }
      });
      return response.data;
    } catch (error) {
      console.error('Error en cambiarEstado:', error);
      throw error;
    }
  },

  // Eliminar albarán
  delete: async (id) => {
    try {
      const response = await api.delete(`${API_URL}/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error en delete:', error);
      throw error;
    }
  }
};

export default albaranService;