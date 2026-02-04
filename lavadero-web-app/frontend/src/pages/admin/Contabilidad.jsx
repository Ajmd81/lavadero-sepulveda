import React, { useState, useEffect } from 'react';
import { format, parseISO, startOfMonth, endOfMonth } from 'date-fns';
import { es } from 'date-fns/locale';
import facturaService from '../../services/facturaService';
import facturaRecibidaService from '../../services/facturaRecibidaService';
import gastoService from '../../services/gastoService';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const Contabilidad = () => {
  const [facturas, setFacturas] = useState([]);
  const [facturasRecibidas, setFacturasRecibidas] = useState([]);
  const [gastos, setGastos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [fechaInicio, setFechaInicio] = useState('');
  const [fechaFin, setFechaFin] = useState('');

  useEffect(() => {
    cargarTodosDatos();
  }, []);

  const cargarTodosDatos = async () => {
    try {
      setLoading(true);
      setError(null);
      
      console.log('📊 Cargando datos para contabilidad...');
      
      const [facturasResp, facturasRecibidasResp, gastosResp] = await Promise.all([
        facturaService.getAll(0, 1000),
        facturaRecibidaService.getAll(),
        gastoService.getAll()
      ]);
      
      // Procesar facturas emitidas
      let facturasData = [];
      if (Array.isArray(facturasResp)) {
        facturasData = facturasResp;
      } else if (facturasResp?.content && Array.isArray(facturasResp.content)) {
        facturasData = facturasResp.content;
      } else if (facturasResp?.data) {
        facturasData = Array.isArray(facturasResp.data) ? facturasResp.data : facturasResp.data.content || [];
      }

      // Procesar facturas recibidas
      let facturasRecibidasData = facturasRecibidasResp?.data || [];
      if (facturasRecibidasData?.content && Array.isArray(facturasRecibidasData.content)) {
        facturasRecibidasData = facturasRecibidasData.content;
      }
      if (!Array.isArray(facturasRecibidasData)) {
        facturasRecibidasData = [];
      }

      // Procesar gastos
      let gastosData = gastosResp?.data || [];
      if (gastosData?.content && Array.isArray(gastosData.content)) {
        gastosData = gastosData.content;
      }
      if (!Array.isArray(gastosData)) {
        gastosData = [];
      }

      console.log(`✅ ${facturasData.length} facturas, ${facturasRecibidasData.length} facturas recibidas, ${gastosData.length} gastos`);
      
      setFacturas(facturasData);
      setFacturasRecibidas(facturasRecibidasData);
      setGastos(gastosData);
      
    } catch (error) {
      console.error('❌ Error cargando datos:', error);
      setError('Error al cargar los datos');
    } finally {
      setLoading(false);
    }
  };

  const parsearFecha = (fechaStr) => {
    if (!fechaStr) return null;
    
    try {
      if (fechaStr.includes('/')) {
        const [dia, mes, anio] = fechaStr.split('/');
        return new Date(parseInt(anio), parseInt(mes) - 1, parseInt(dia));
      }
      if (fechaStr.includes('-')) {
        return parseISO(fechaStr);
      }
      return null;
    } catch (error) {
      return null;
    }
  };

  const filtrarPorFecha = (items) => {
    if (!fechaInicio && !fechaFin) return items;
    
    return items.filter(item => {
      const fechaItem = parsearFecha(item.fecha || item.fechaFactura);
      if (!fechaItem) return false;

      // Normalizar todas las fechas a medianoche para comparación justa
      const fechaItemNormalizada = new Date(fechaItem.getFullYear(), fechaItem.getMonth(), fechaItem.getDate());
      
      if (fechaInicio && fechaFin) {
        const inicio = new Date(fechaInicio);
        const fin = new Date(fechaFin);
        fin.setHours(23, 59, 59, 999); // Incluir todo el día final
        return fechaItemNormalizada >= inicio && fechaItemNormalizada <= fin;
      } else if (fechaInicio) {
        const inicio = new Date(fechaInicio);
        return fechaItemNormalizada >= inicio;
      } else if (fechaFin) {
        const fin = new Date(fechaFin);
        fin.setHours(23, 59, 59, 999); // Incluir todo el día final
        return fechaItemNormalizada <= fin;
      }
      
      return true;
    });
  };

  const facturasFiltradas = filtrarPorFecha(facturas);
  const facturasRecibidasFiltradas = filtrarPorFecha(facturasRecibidas);
  const gastosFiltrados = filtrarPorFecha(gastos);

  // Calcular estadísticas
  const ingresosTotales = facturasFiltradas.reduce((sum, f) => {
    const total = typeof f.total === 'number' ? f.total : parseFloat(f.total || 0);
    return sum + total;
  }, 0);

  const baseImponible = facturasFiltradas.reduce((sum, f) => {
    const base = typeof f.baseImponible === 'number' ? f.baseImponible : parseFloat(f.baseImponible || 0);
    return sum + base;
  }, 0);

  const ivaRepercutido = ingresosTotales - baseImponible;

  // Gastos totales
  const gastosFacturasRecibidas = facturasRecibidasFiltradas.reduce((sum, f) => {
    const total = typeof f.total === 'number' ? f.total : parseFloat(f.total || 0);
    return sum + total;
  }, 0);

  const gastosSimples = gastosFiltrados.reduce((sum, g) => {
    const importe = typeof g.importe === 'number' ? g.importe : parseFloat(g.importe || 0);
    return sum + importe;
  }, 0);

  const totalGastos = gastosFacturasRecibidas + gastosSimples;

  const baseFacturasRecibidas = facturasRecibidasFiltradas.reduce((sum, f) => {
    const base = typeof f.baseImponible === 'number' ? f.baseImponible : parseFloat(f.baseImponible || 0);
    return sum + base;
  }, 0);

  // Base Gastos = Base Facturas Recibidas + Gastos Simples (sin IVA)
  const baseGastos = baseFacturasRecibidas + gastosSimples;
  
  // IVA Soportado = Solo de facturas recibidas
  const ivaSoportado = gastosFacturasRecibidas - baseFacturasRecibidas;

  // Agrupar por mes
  const facturasPorMes = facturasFiltradas.reduce((acc, factura) => {
    const fechaFactura = parsearFecha(factura.fecha);
    if (!fechaFactura) return acc;
    
    const mes = format(fechaFactura, 'yyyy-MM');
    if (!acc[mes]) {
      acc[mes] = {
        mes,
        ingresos: 0,
        gastos: 0,
        cantidad: 0
      };
    }
    
    const total = typeof factura.total === 'number' ? factura.total : parseFloat(factura.total || 0);
    acc[mes].ingresos += total;
    acc[mes].cantidad += 1;
    
    return acc;
  }, {});

  // Agregar gastos al agrupamiento por mes
  [...facturasRecibidasFiltradas, ...gastosFiltrados].forEach(item => {
    const fechaItem = parsearFecha(item.fecha || item.fechaFactura);
    if (!fechaItem) return;
    
    const mes = format(fechaItem, 'yyyy-MM');
    if (!facturasPorMes[mes]) {
      facturasPorMes[mes] = {
        mes,
        ingresos: 0,
        gastos: 0,
        cantidad: 0
      };
    }
    
    const importe = typeof item.total === 'number' ? item.total : 
                   typeof item.importe === 'number' ? item.importe :
                   parseFloat(item.total || item.importe || 0);
    facturasPorMes[mes].gastos += importe;
  });

  const datosMensuales = Object.values(facturasPorMes)
    .sort((a, b) => a.mes.localeCompare(b.mes))
    .map(item => ({
      mes: format(new Date(item.mes + '-01'), 'MMM yyyy', { locale: es }),
      ingresos: item.ingresos,
      gastos: item.gastos,
      cantidad: item.cantidad
    }));

  // Top 10 clientes
  const facturasPorCliente = facturasFiltradas.reduce((acc, factura) => {
    const cliente = factura.clienteNombre || 'Sin nombre';
    if (!acc[cliente]) {
      acc[cliente] = {
        cliente,
        total: 0,
        cantidad: 0
      };
    }
    
    const total = typeof factura.total === 'number' ? factura.total : parseFloat(factura.total || 0);
    acc[cliente].total += total;
    acc[cliente].cantidad += 1;
    
    return acc;
  }, {});

  const top10Clientes = Object.values(facturasPorCliente)
    .sort((a, b) => b.total - a.total)
    .slice(0, 10);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        <span className="ml-3 text-gray-600">Cargando datos financieros...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-6">
        <div className="flex items-start">
          <svg className="w-6 h-6 text-red-600 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div className="ml-3">
            <h3 className="text-red-800 font-semibold">Error al cargar datos</h3>
            <p className="text-red-600 mt-1">{error}</p>
            <button
              onClick={cargarTodosDatos}
              className="mt-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
            >
              Reintentar
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-800">Resumen Contable</h1>

      {/* Filtros de fecha */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-lg font-semibold mb-4">Filtros</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Fecha Inicio</label>
            <input
              type="date"
              value={fechaInicio}
              onChange={(e) => setFechaInicio(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Fecha Fin</label>
            <input
              type="date"
              value={fechaFin}
              onChange={(e) => setFechaFin(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
            />
          </div>
          <div className="flex items-end">
            <button
              onClick={() => {
                setFechaInicio('');
                setFechaFin('');
              }}
              className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
            >
              Limpiar Filtros
            </button>
          </div>
        </div>
      </div>

      {/* Fila 1: Principales */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-gradient-to-r from-blue-500 to-blue-600 rounded-lg shadow p-6 text-white">
          <h3 className="text-sm font-semibold opacity-90">Ingresos Totales</h3>
          <p className="text-3xl font-bold mt-2">€{ingresosTotales.toFixed(2)}</p>
          <p className="text-sm mt-2 opacity-75">{facturasFiltradas.length} facturas</p>
        </div>

        <div className="bg-gradient-to-r from-red-500 to-red-600 rounded-lg shadow p-6 text-white">
          <h3 className="text-sm font-semibold opacity-90">Gastos Totales</h3>
          <p className="text-3xl font-bold mt-2">€{totalGastos.toFixed(2)}</p>
          <p className="text-sm mt-2 opacity-75">{facturasRecibidasFiltradas.length + gastosFiltrados.length} gastos</p>
        </div>

        <div className="bg-gradient-to-r from-green-500 to-green-600 rounded-lg shadow p-6 text-white">
          <h3 className="text-sm font-semibold opacity-90">Beneficio Neto</h3>
          <p className="text-3xl font-bold mt-2">€{(ingresosTotales - totalGastos).toFixed(2)}</p>
          <p className="text-sm mt-2 opacity-75">Ingresos - Gastos</p>
        </div>
      </div>

      {/* Fila 2: Bases Imponibles */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-gradient-to-r from-teal-500 to-teal-600 rounded-lg shadow p-6 text-white">
          <h3 className="text-sm font-semibold opacity-90">Base Imponible Ventas</h3>
          <p className="text-3xl font-bold mt-2">€{baseImponible.toFixed(2)}</p>
          <p className="text-sm mt-2 opacity-75">Sin IVA</p>
        </div>

        <div className="bg-gradient-to-r from-amber-500 to-amber-600 rounded-lg shadow p-6 text-white">
          <h3 className="text-sm font-semibold opacity-90">Base Gastos</h3>
          <p className="text-3xl font-bold mt-2">€{baseGastos.toFixed(2)}</p>
          <p className="text-sm mt-2 opacity-75">Sin IVA</p>
        </div>

        <div className="bg-gradient-to-r from-indigo-500 to-indigo-600 rounded-lg shadow p-6 text-white">
          <h3 className="text-sm font-semibold opacity-90">Liquidación IVA</h3>
          <p className="text-3xl font-bold mt-2">€{(ivaRepercutido - ivaSoportado).toFixed(2)}</p>
          <p className="text-sm mt-2 opacity-75">{(ivaRepercutido - ivaSoportado) >= 0 ? 'A ingresar' : 'A compensar'}</p>
        </div>
      </div>

      {/* Fila 3: IVA */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-gradient-to-r from-orange-500 to-orange-600 rounded-lg shadow p-6 text-white">
          <h3 className="text-sm font-semibold opacity-90">IVA Repercutido</h3>
          <p className="text-3xl font-bold mt-2">€{ivaRepercutido.toFixed(2)}</p>
          <p className="text-sm mt-2 opacity-75">De ventas</p>
        </div>

        <div className="bg-gradient-to-r from-purple-500 to-purple-600 rounded-lg shadow p-6 text-white">
          <h3 className="text-sm font-semibold opacity-90">IVA Soportado</h3>
          <p className="text-3xl font-bold mt-2">€{ivaSoportado.toFixed(2)}</p>
          <p className="text-sm mt-2 opacity-75">De compras</p>
        </div>

        <div className="bg-gradient-to-r from-cyan-500 to-cyan-600 rounded-lg shadow p-6 text-white">
          <h3 className="text-sm font-semibold opacity-90">Margen Beneficio</h3>
          <p className="text-3xl font-bold mt-2">{ingresosTotales > 0 ? ((ingresosTotales - totalGastos) / ingresosTotales * 100).toFixed(2) : 0}%</p>
          <p className="text-sm mt-2 opacity-75">Beneficio / Ingresos</p>
        </div>
      </div>

      {/* Gráfico de ingresos y gastos por mes */}
      {datosMensuales.length > 0 && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">Evolución de Ingresos y Gastos</h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={datosMensuales}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="mes" />
              <YAxis />
              <Tooltip formatter={(value) => `€${value.toFixed(2)}`} />
              <Legend />
              <Bar dataKey="ingresos" fill="#3B82F6" name="Ingresos" />
              <Bar dataKey="gastos" fill="#EF4444" name="Gastos" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Top 10 clientes */}
      {top10Clientes.length > 0 && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">Top 10 Clientes</h2>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Cliente</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Facturas</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {top10Clientes.map((cliente, index) => (
                  <tr key={index} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {cliente.cliente}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {cliente.cantidad}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-semibold">
                      €{cliente.total.toFixed(2)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default Contabilidad;