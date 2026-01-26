import { useState, useEffect } from 'react';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import api from '../../services/api';

const Contabilidad = () => {
  const [periodo, setPeriodo] = useState('Este mes');
  const [fechaDesde, setFechaDesde] = useState('');
  const [fechaHasta, setFechaHasta] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [datos, setDatos] = useState(null);
  const [resumenMensual, setResumenMensual] = useState([]);
  const [resumenCliente, setResumenCliente] = useState([]);

  // Calcular rango de fechas según período
  const calcularFechas = (periodoSeleccionado) => {
    const hoy = new Date();
    let desde, hasta;

    switch (periodoSeleccionado) {
      case 'Este mes':
        desde = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
        hasta = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
        break;
      case 'Mes anterior':
        desde = new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1);
        hasta = new Date(hoy.getFullYear(), hoy.getMonth(), 0);
        break;
      case 'Este trimestre': {
        const trimestre = Math.floor(hoy.getMonth() / 3);
        desde = new Date(hoy.getFullYear(), trimestre * 3, 1);
        hasta = new Date(hoy.getFullYear(), trimestre * 3 + 3, 0);
        console.log(`DEBUG Trimestre: ${trimestre}, mes actual: ${hoy.getMonth()}`);
        console.log(`DEBUG Desde: ${desde.toISOString()}, Hasta: ${hasta.toISOString()}`);
        break;
      }
      case 'Este año':
        desde = new Date(hoy.getFullYear(), 0, 1);
        hasta = new Date(hoy.getFullYear(), 11, 31);
        break;
      default:
        return null;
    }

    const resultado = {
      desde: desde.toISOString().split('T')[0],
      hasta: hasta.toISOString().split('T')[0]
    };
    
    console.log(`Período: ${periodoSeleccionado}`, resultado);
    return resultado;
  };

  // Actualizar fechas cuando cambia el período
  useEffect(() => {
    if (periodo !== 'Personalizado') {
      const fechas = calcularFechas(periodo);
      if (fechas) {
        setFechaDesde(fechas.desde);
        setFechaHasta(fechas.hasta);
      }
    }
  }, [periodo]);

  // Cargar datos de contabilidad
  const cargarDatos = async () => {
    if (!fechaDesde || !fechaHasta) {
      setError('Debe seleccionar un rango de fechas válido');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await api.get('/contabilidad/resumen', {
        params: {
          desde: fechaDesde,
          hasta: fechaHasta
        }
      });

      if (response.data) {
        setDatos(response.data);
        procesarDatos(response.data);
      }
    } catch (err) {
      console.error('Error cargando datos de contabilidad:', err);
      setError('Error al cargar los datos: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  // Procesar datos para gráficos
  const procesarDatos = (datosRecibidos) => {
    if (datosRecibidos.resumenMensual) {
      const mensual = datosRecibidos.resumenMensual.map(item => ({
        mes: new Date(item.mes).toLocaleDateString('es-ES', { month: 'short', year: '2-digit' }),
        ingresos: parseFloat(item.total) || 0,
        base: parseFloat(item.base) || 0,
        iva: parseFloat(item.iva) || 0
      }));
      setResumenMensual(mensual);
    }

    if (datosRecibidos.resumenCliente) {
      const clientes = datosRecibidos.resumenCliente
        .sort((a, b) => parseFloat(b.total) - parseFloat(a.total))
        .slice(0, 10)
        .map(item => ({
          cliente: item.nombreCliente,
          total: parseFloat(item.total) || 0,
          facturas: item.numFacturas || 0
        }));
      setResumenCliente(clientes);
    }
  };

  // Exportar a Excel
  const exportarExcel = async () => {
    try {
      const response = await api.get('/contabilidad/exportar-excel', {
        params: {
          desde: fechaDesde,
          hasta: fechaHasta
        },
        responseType: 'blob'
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `contabilidad_${fechaDesde}_${fechaHasta}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (err) {
      console.error('Error exportando Excel:', err);
      setError('Error al exportar Excel: ' + err.message);
    }
  };

  // Exportar a PDF
  const exportarPDF = async () => {
    try {
      const response = await api.get('/contabilidad/exportar-pdf', {
        params: {
          desde: fechaDesde,
          hasta: fechaHasta
        },
        responseType: 'blob'
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `contabilidad_${fechaDesde}_${fechaHasta}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (err) {
      console.error('Error exportando PDF:', err);
      setError('Error al exportar PDF: ' + err.message);
    }
  };

  // Formatear moneda
  const formatearMoneda = (valor) => {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(valor || 0);
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-2xl font-bold mb-6">Contabilidad</h2>

      {/* Panel de Filtros */}
      <div className="bg-gray-50 rounded-lg p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Período</label>
            <select
              value={periodo}
              onChange={(e) => setPeriodo(e.target.value)}
              className="w-full border border-gray-300 rounded px-3 py-2"
            >
              <option>Este mes</option>
              <option>Mes anterior</option>
              <option>Este trimestre</option>
              <option>Este año</option>
              <option>Personalizado</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Desde</label>
            <input
              type="date"
              value={fechaDesde}
              onChange={(e) => setFechaDesde(e.target.value)}
              disabled={periodo !== 'Personalizado'}
              className="w-full border border-gray-300 rounded px-3 py-2 disabled:bg-gray-100"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Hasta</label>
            <input
              type="date"
              value={fechaHasta}
              onChange={(e) => setFechaHasta(e.target.value)}
              disabled={periodo !== 'Personalizado'}
              className="w-full border border-gray-300 rounded px-3 py-2 disabled:bg-gray-100"
            />
          </div>

          <div className="flex items-end gap-2">
            <button
              onClick={cargarDatos}
              disabled={loading}
              className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded disabled:opacity-50"
            >
              {loading ? 'Cargando...' : 'Cargar'}
            </button>
          </div>
        </div>

        {/* Botones de Exportación */}
        <div className="flex gap-2">
          <button
            onClick={exportarExcel}
            disabled={!datos}
            className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded disabled:opacity-50"
          >
            📊 Exportar Excel
          </button>
          <button
            onClick={exportarPDF}
            disabled={!datos}
            className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded disabled:opacity-50"
          >
            📄 Exportar PDF
          </button>
        </div>
      </div>

      {/* Mensajes */}
      {error && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      {/* Tarjetas de Resumen */}
      {datos && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-gradient-to-br from-green-50 to-green-100 rounded-lg p-4 border border-green-200">
            <p className="text-sm text-gray-600 mb-2">Ingresos Totales</p>
            <p className="text-2xl font-bold text-green-700">{formatearMoneda(datos.ingresosTotales)}</p>
          </div>

          <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg p-4 border border-blue-200">
            <p className="text-sm text-gray-600 mb-2">Base Imponible</p>
            <p className="text-2xl font-bold text-blue-700">{formatearMoneda(datos.baseImponible)}</p>
          </div>

          <div className="bg-gradient-to-br from-orange-50 to-orange-100 rounded-lg p-4 border border-orange-200">
            <p className="text-sm text-gray-600 mb-2">IVA Repercutido</p>
            <p className="text-2xl font-bold text-orange-700">{formatearMoneda(datos.ivaRepercutido)}</p>
          </div>

          <div className="bg-gradient-to-br from-purple-50 to-purple-100 rounded-lg p-4 border border-purple-200">
            <p className="text-sm text-gray-600 mb-2">Nº Facturas</p>
            <p className="text-2xl font-bold text-purple-700">{datos.numFacturas || 0}</p>
          </div>
        </div>
      )}

      {/* Gráficos */}
      {resumenMensual.length > 0 && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <div className="bg-gray-50 rounded-lg p-4">
            <h3 className="text-lg font-semibold mb-4">Ingresos Mensuales</h3>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={resumenMensual}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="mes" />
                <YAxis />
                <Tooltip formatter={(value) => formatearMoneda(value)} />
                <Legend />
                <Line type="monotone" dataKey="ingresos" stroke="#10b981" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </div>

          <div className="bg-gray-50 rounded-lg p-4">
            <h3 className="text-lg font-semibold mb-4">Desglose: Base e IVA</h3>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={resumenMensual}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="mes" />
                <YAxis />
                <Tooltip formatter={(value) => formatearMoneda(value)} />
                <Legend />
                <Bar dataKey="base" fill="#3b82f6" />
                <Bar dataKey="iva" fill="#f97316" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {/* Tablas de Detalle */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Tabla Resumen Mensual */}
        {resumenMensual.length > 0 && (
          <div className="bg-gray-50 rounded-lg p-4">
            <h3 className="text-lg font-semibold mb-4">Resumen Mensual</h3>
            <div className="overflow-x-auto">
              <table className="min-w-full border-collapse border border-gray-300">
                <thead className="bg-blue-600 text-white">
                  <tr>
                    <th className="border border-gray-300 px-3 py-2 text-left text-sm">Mes</th>
                    <th className="border border-gray-300 px-3 py-2 text-right text-sm">Ingresos</th>
                  </tr>
                </thead>
                <tbody>
                  {resumenMensual.map((item, idx) => (
                    <tr key={idx} className="hover:bg-white">
                      <td className="border border-gray-300 px-3 py-2 text-sm">{item.mes}</td>
                      <td className="border border-gray-300 px-3 py-2 text-right text-sm font-medium">
                        {formatearMoneda(item.ingresos)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Tabla Top Clientes */}
        {resumenCliente.length > 0 && (
          <div className="bg-gray-50 rounded-lg p-4">
            <h3 className="text-lg font-semibold mb-4">Top 10 Clientes</h3>
            <div className="overflow-x-auto">
              <table className="min-w-full border-collapse border border-gray-300">
                <thead className="bg-blue-600 text-white">
                  <tr>
                    <th className="border border-gray-300 px-3 py-2 text-left text-sm">Cliente</th>
                    <th className="border border-gray-300 px-3 py-2 text-center text-sm">Facturas</th>
                    <th className="border border-gray-300 px-3 py-2 text-right text-sm">Total</th>
                  </tr>
                </thead>
                <tbody>
                  {resumenCliente.map((item, idx) => (
                    <tr key={idx} className="hover:bg-white">
                      <td className="border border-gray-300 px-3 py-2 text-sm truncate">{item.cliente}</td>
                      <td className="border border-gray-300 px-3 py-2 text-center text-sm">{item.facturas}</td>
                      <td className="border border-gray-300 px-3 py-2 text-right text-sm font-medium">
                        {formatearMoneda(item.total)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>

      {/* Estado sin datos */}
      {!loading && !datos && (
        <div className="text-center py-12">
          <p className="text-gray-500">Selecciona un período y haz clic en "Cargar" para ver los datos</p>
        </div>
      )}
    </div>
  );
};

export default Contabilidad;
