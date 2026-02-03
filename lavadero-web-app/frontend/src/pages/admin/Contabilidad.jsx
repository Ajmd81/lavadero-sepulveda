import { useState, useEffect } from 'react';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import facturaService from '../../services/facturaService';

const Contabilidad = () => {
  const [periodo, setPeriodo] = useState('Este mes');
  const [fechaDesde, setFechaDesde] = useState('');
  const [fechaHasta, setFechaHasta] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [todasFacturas, setTodasFacturas] = useState([]);
  const [datos, setDatos] = useState(null);
  const [resumenMensual, setResumenMensual] = useState([]);
  const [resumenCliente, setResumenCliente] = useState([]);

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
        break;
      }
      case 'Este año':
        desde = new Date(hoy.getFullYear(), 0, 1);
        hasta = new Date(hoy.getFullYear(), 11, 31);
        break;
      default:
        return null;
    }

    return {
      desde: desde.toISOString().split('T')[0],
      hasta: hasta.toISOString().split('T')[0]
    };
  };

  useEffect(() => {
    if (periodo !== 'Personalizado') {
      const fechas = calcularFechas(periodo);
      if (fechas) {
        setFechaDesde(fechas.desde);
        setFechaHasta(fechas.hasta);
      }
    }
  }, [periodo]);

  useEffect(() => {
    cargarTodasFacturas();
  }, []);

  const cargarTodasFacturas = async () => {
    try {
      const response = await facturaService.getAll(0, 1000);
      const facturas = response.data.content || response.data || [];
      setTodasFacturas(facturas);
    } catch (err) {
      console.error('Error cargando facturas:', err);
      setTodasFacturas([]);
    }
  };

  const parsearFecha = (fechaStr) => {
    if (!fechaStr) return null;
    
    if (fechaStr instanceof Date) {
      return fechaStr;
    }
    
    if (fechaStr.includes('/')) {
      const [dia, mes, anio] = fechaStr.split('/');
      return new Date(parseInt(anio), parseInt(mes) - 1, parseInt(dia));
    } else if (fechaStr.includes('-')) {
      const fechaSolo = fechaStr.split('T')[0];
      const [anio, mes, dia] = fechaSolo.split('-');
      return new Date(parseInt(anio), parseInt(mes) - 1, parseInt(dia));
    }
    
    return null;
  };

  const cargarDatos = async () => {
    if (!fechaDesde || !fechaHasta) {
      setError('Debe seleccionar un rango de fechas válido');
      return;
    }

    setLoading(true);
    setError(null);
    
    try {
      const desde = new Date(fechaDesde);
      const hasta = new Date(fechaHasta);
      
      const facturasFiltradas = todasFacturas.filter(f => {
        const fechaFactura = parsearFecha(f.fecha);
        return fechaFactura && fechaFactura >= desde && fechaFactura <= hasta;
      });

      const ingresosTotales = facturasFiltradas.reduce((sum, f) => {
        const total = typeof f.total === 'number' ? f.total : parseFloat(f.total) || 0;
        return sum + total;
      }, 0);

      const baseImponible = facturasFiltradas.reduce((sum, f) => {
        const base = typeof f.baseImponible === 'number' ? f.baseImponible : parseFloat(f.baseImponible) || 0;
        return sum + base;
      }, 0);

      const ivaRepercutido = facturasFiltradas.reduce((sum, f) => {
        const iva = typeof f.importeIva === 'number' ? f.importeIva : parseFloat(f.importeIva) || 0;
        return sum + iva;
      }, 0);

      const datosCalculados = {
        ingresosTotales,
        baseImponible,
        ivaRepercutido,
        numFacturas: facturasFiltradas.length
      };

      setDatos(datosCalculados);
      procesarDatos(facturasFiltradas);
      
    } catch (err) {
      console.error('Error procesando datos:', err);
      setError('Error al procesar los datos: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const procesarDatos = (facturas) => {
    const facturasPorMes = {};
    
    facturas.forEach(f => {
      const fechaFactura = parsearFecha(f.fecha);
      if (fechaFactura) {
        const mesKey = `${fechaFactura.getFullYear()}-${String(fechaFactura.getMonth() + 1).padStart(2, '0')}`;
        
        if (!facturasPorMes[mesKey]) {
          facturasPorMes[mesKey] = {
            mes: mesKey,
            total: 0,
            base: 0,
            iva: 0
          };
        }
        
        const total = typeof f.total === 'number' ? f.total : parseFloat(f.total) || 0;
        const base = typeof f.baseImponible === 'number' ? f.baseImponible : parseFloat(f.baseImponible) || 0;
        const iva = typeof f.importeIva === 'number' ? f.importeIva : parseFloat(f.importeIva) || 0;
        
        facturasPorMes[mesKey].total += total;
        facturasPorMes[mesKey].base += base;
        facturasPorMes[mesKey].iva += iva;
      }
    });

    const mensual = Object.values(facturasPorMes)
      .sort((a, b) => a.mes.localeCompare(b.mes))
      .map(item => {
        const [anio, mes] = item.mes.split('-');
        const fecha = new Date(parseInt(anio), parseInt(mes) - 1, 1);
        return {
          mes: fecha.toLocaleDateString('es-ES', { month: 'short', year: '2-digit' }),
          ingresos: item.total,
          base: item.base,
          iva: item.iva
        };
      });
    
    setResumenMensual(mensual);

    const facturasPorCliente = {};
    
    facturas.forEach(f => {
      const nombreCliente = f.clienteNombre || 'Sin nombre';
      
      if (!facturasPorCliente[nombreCliente]) {
        facturasPorCliente[nombreCliente] = {
          nombreCliente,
          total: 0,
          numFacturas: 0
        };
      }
      
      const total = typeof f.total === 'number' ? f.total : parseFloat(f.total) || 0;
      facturasPorCliente[nombreCliente].total += total;
      facturasPorCliente[nombreCliente].numFacturas++;
    });

    const clientes = Object.values(facturasPorCliente)
      .sort((a, b) => b.total - a.total)
      .slice(0, 10)
      .map(item => ({
        cliente: item.nombreCliente,
        total: item.total,
        facturas: item.numFacturas
      }));
    
    setResumenCliente(clientes);
  };

  const formatearMoneda = (valor) => {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(valor || 0);
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center gap-3 mb-6">
        <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <img src="/assets/icons/contabilidad.png" alt="Contabilidad" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
        </div>
        <h1 className="text-3xl font-bold text-gray-900">Contabilidad</h1>
      </div>

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
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

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

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
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

      {!loading && !datos && (
        <div className="text-center py-12">
          <p className="text-gray-500">Selecciona un período y haz clic en "Cargar" para ver los datos</p>
        </div>
      )}
    </div>
  );
};

export default Contabilidad;