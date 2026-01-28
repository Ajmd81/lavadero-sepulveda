import api from './api';

const modelosFiscalesService = {
    // Obtener datos de un modelo fiscal (303, 130, 111, etc.)
    // Obtener datos de un modelo fiscal (303, 130)
    getModelo: (modelo, params) => {
        // params trae: anio, trimestre
        return api.get(`/modelos-fiscales/${modelo}`, { params });
    },

    // Generar PDF del modelo
    generarPDF: (modelo, params) =>
        api.get(`/modelos-fiscales/${modelo}/pdf`, {
            params,
            responseType: 'blob'
        })
};

export default modelosFiscalesService;
