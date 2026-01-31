import axios from 'axios';

const API_BASE = 'https://lavadero-sepulveda-production.up.railway.app/api/enums';

const enumsService = {
  obtenerCategoriasGasto: async () => {
    try {
      const response = await axios.get(`${API_BASE}/categorias-gasto`);
      return response;
    } catch (error) {
      console.error('Error al obtener categorías de gasto:', error);
      throw error;
    }
  },

  obtenerMetodosPago: async () => {
    try {
      const response = await axios.get(`${API_BASE}/metodos-pago`);
      return response;
    } catch (error) {
      console.error('Error al obtener métodos de pago:', error);
      throw error;
    }
  },

  obtenerEstadosFactura: async () => {
    try {
      const response = await axios.get(`${API_BASE}/estados-factura`);
      return response;
    } catch (error) {
      console.error('Error al obtener estados de factura:', error);
      throw error;
    }
  }
};

export default enumsService;
