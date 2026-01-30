import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import axios from 'axios';
import { format, startOfMonth, endOfMonth, startOfQuarter, endOfQuarter, startOfYear, endOfYear, subMonths } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  LineChart, Line, PieChart, Pie, Cell, BarChart, Bar,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import {
  TrendingUp, TrendingDown, DollarSign, Euro, Receipt,
  CreditCard, Calendar, FileText, AlertCircle
} from 'lucide-react';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const COLORES_GRAFICO = ['#3b82f6', '#ef4444', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316'];

const ResumenFinanciero = () => {
  const [filtroSeleccionado, setFiltroSeleccionado] = useState('mes');
  const [fechaDesde, setFechaDesde] = useState(format(startOfMonth(new Date()), 'yyyy-MM-dd'));
  const [fechaHasta, setFechaHasta] = useState(format(endOfMonth(new Date()), 'yyyy-MM-dd'));
  const [mostrarPersonalizado, setMostrarPersonalizado] = useState(false);

  // Aplicar filtro predefinido
  const aplicarFiltro = (tipo) => {
    const hoy = new Date();
    let desde, hasta;

    switch (tipo) {
      case 'mes':
        desde = startOfMonth(hoy);
        hasta = endOfMonth(hoy);
        break;
      case 'mesAnterior':
        const mesAnterior = subMonths(hoy, 1);
        desde = startOfMonth(mesAnterior);
        hasta = endOfMonth(mesAnterior);
        break;
      case 'trimestre':
        desde = startOfQuarter(hoy);
        hasta = endOfQuarter(hoy);
        break;
      case 'año':
        desde = startOfYear(hoy);
        hasta = endOfYear(hoy);
        break;
      default:
        return;
    }

    setFechaDesde(format(desde, 'yyyy-MM-dd'));
    setFechaHasta(format(hasta, 'yyyy-MM-dd'));
    setFiltroSeleccionado(tipo);
    setMostrarPersonalizado(false);
  };

  // Query para obtener resumen
  const { data: resumen, isLoading, error } = useQuery({
    queryKey: ['resumen-financiero', fechaDesde, fechaHasta],
    queryFn: async () => {
      const { data } = await axios.get(`${API_URL}/resumen-financiero`, {
        params: { desde: fechaDesde, hasta: fechaHasta }
      });
      return data;
    },
    enabled: !!fechaDesde && !!fechaHasta,
  });

  // Formatear moneda
  const formatCurrency = (value) => {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(value || 0);
  };

  // Formatear porcentaje
  const formatPercentage = (value) => {
    return `${(value || 0).toFixed(2)}%`;
  };

  // Componente KPI Card
  const KPICard = ({ titulo, valor, icon: Icon, color, subvalor, tendencia }) => (
    <div className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow">
      <div className="flex items-center justify-between">
        <div className="flex-1">
          <p className="text-sm text-gray-600 mb-1">{titulo}</p>
          <p className={`text-2xl font-bold ${color}`}>{valor}</p>
          {subvalor && <p className="text-sm text-gray-500 mt-1">{subvalor}</p>}
        </div>
        <div className={`p-3 rounded-full ${color.replace('text-', 'bg-').replace('600', '100')}`}>
          <Icon className={color} size={24} />
        </div>
      </div>
      {tendencia && (
        <div className={`mt-2 flex items-center text-sm ${tendencia >= 0 ? 'text-green-600' : 'text-red-600'}`}>
          {tendencia >= 0 ? <TrendingUp size={16} className="mr-1" /> : <TrendingDown size={16} className="mr-1" />}
          {Math.abs(tendencia).toFixed(2)}% vs período anterior
        </div>
      )}
    </div>
  );

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <div className="flex items-center text-red-800">
          <AlertCircle className="mr-2" size={20} />
          <span>Error al cargar el resumen financiero: {error.message}</span>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header y Filtros */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
          <div className="flex items-center gap-3">
            <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <img src="/assets/icons/estado-financiero.png" alt="Resumen Financiero" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Resumen Financiero</h1>
              <p className="text-gray-600 mt-1">
                Del {format(new Date(fechaDesde), 'dd/MM/yyyy', { locale: es })} al{' '}
                {format(new Date(fechaHasta), 'dd/MM/yyyy', { locale: es })}
              </p>
            </div>
          </div>

          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => aplicarFiltro('mes')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${filtroSeleccionado === 'mes'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
            >
              Este Mes
            </button>
            <button
              onClick={() => aplicarFiltro('mesAnterior')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${filtroSeleccionado === 'mesAnterior'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
            >
              Mes Anterior
            </button>
            <button
              onClick={() => aplicarFiltro('trimestre')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${filtroSeleccionado === 'trimestre'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
            >
              Este Trimestre
            </button>
            <button
              onClick={() => aplicarFiltro('año')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${filtroSeleccionado === 'año'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
            >
              Este Año
            </button>
            <button
              onClick={() => setMostrarPersonalizado(!mostrarPersonalizado)}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${mostrarPersonalizado
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
            >
              <Calendar size={18} className="inline mr-1" />
              Personalizado
            </button>
          </div>
        </div>

        {/* Filtro Personalizado */}
        {mostrarPersonalizado && (
          <div className="mt-4 p-4 bg-gray-50 rounded-lg border border-gray-200">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Fecha Desde</label>
                <input
                  type="date"
                  value={fechaDesde}
                  onChange={(e) => setFechaDesde(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Fecha Hasta</label>
                <input
                  type="date"
                  value={fechaHasta}
                  onChange={(e) => setFechaHasta(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div className="flex items-end">
                <button
                  onClick={() => setFiltroSeleccionado('personalizado')}
                  className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Aplicar Filtro
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* KPIs Principales */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <KPICard
          titulo="Ingresos Totales"
          valor={formatCurrency(resumen?.totalIngresos)}
          icon={TrendingUp}
          color="text-green-600"
          subvalor={`${resumen?.numFacturasEmitidas || 0} facturas emitidas`}
        />
        <KPICard
          titulo="Gastos Totales"
          valor={formatCurrency(resumen?.totalGastos)}
          icon={TrendingDown}
          color="text-red-600"
          subvalor={`${resumen?.numGastos || 0} gastos registrados`}
        />
        <KPICard
          titulo="Beneficio Neto"
          valor={formatCurrency(resumen?.beneficio)}
          icon={Euro}
          color={resumen?.beneficio >= 0 ? 'text-blue-600' : 'text-red-600'}
        />
        <KPICard
          titulo="Margen de Beneficio"
          valor={formatPercentage(resumen?.margenPorcentaje)}
          icon={Euro}
          color={resumen?.margenPorcentaje >= 0 ? 'text-purple-600' : 'text-red-600'}
        />
      </div>

      {/* Gráficos */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Evolución Mensual */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-bold text-gray-900 mb-4">Evolución Mensual</h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={resumen?.evolucionMensual || []}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="mes" />
              <YAxis />
              <Tooltip formatter={(value) => formatCurrency(value)} />
              <Legend />
              <Line type="monotone" dataKey="ingresos" stroke="#10b981" strokeWidth={2} name="Ingresos" />
              <Line type="monotone" dataKey="gastos" stroke="#ef4444" strokeWidth={2} name="Gastos" />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Distribución de Gastos */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-bold text-gray-900 mb-4">Distribución de Gastos por Categoría</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={resumen?.gastosPorCategoria || []}
                dataKey="total"
                nameKey="categoria"
                cx="50%"
                cy="50%"
                outerRadius={100}
                label={(entry) => `${entry.categoria}: ${formatCurrency(entry.total)}`}
              >
                {(resumen?.gastosPorCategoria || []).map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORES_GRAFICO[index % COLORES_GRAFICO.length]} />
                ))}
              </Pie>
              <Tooltip formatter={(value) => formatCurrency(value)} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Detalle IVA */}
      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center">
          <FileText className="mr-2" size={20} />
          Detalle de IVA
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
          <div className="p-4 bg-green-50 rounded-lg border border-green-200">
            <p className="text-sm text-gray-600">Base Imponible</p>
            <p className="text-xl font-bold text-green-700">{formatCurrency(resumen?.baseImponible)}</p>
          </div>
          <div className="p-4 bg-green-50 rounded-lg border border-green-200">
            <p className="text-sm text-gray-600">IVA Repercutido</p>
            <p className="text-xl font-bold text-green-700">{formatCurrency(resumen?.ivaRepercutido)}</p>
          </div>
          <div className="p-4 bg-red-50 rounded-lg border border-red-200">
            <p className="text-sm text-gray-600">Base Gastos</p>
            <p className="text-xl font-bold text-red-700">{formatCurrency(resumen?.baseGastos)}</p>
          </div>
          <div className="p-4 bg-red-50 rounded-lg border border-red-200">
            <p className="text-sm text-gray-600">IVA Soportado</p>
            <p className="text-xl font-bold text-red-700">{formatCurrency(resumen?.ivaSoportado)}</p>
          </div>
          <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
            <p className="text-sm text-gray-600">Liquidación IVA</p>
            <p className={`text-xl font-bold ${resumen?.liquidacionIva >= 0 ? 'text-blue-700' : 'text-red-700'}`}>
              {formatCurrency(resumen?.liquidacionIva)}
            </p>
          </div>
        </div>
      </div>

      {/* Tabla de Gastos por Categoría */}
      {resumen?.gastosPorCategoria && resumen.gastosPorCategoria.length > 0 && (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="p-6 border-b border-gray-200">
            <h3 className="text-lg font-bold text-gray-900">Gastos Detallados por Categoría</h3>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Categoría
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Cantidad
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Total
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    % del Total
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {resumen.gastosPorCategoria.map((cat, index) => (
                  <tr key={index} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div
                          className="w-3 h-3 rounded-full mr-3"
                          style={{ backgroundColor: COLORES_GRAFICO[index % COLORES_GRAFICO.length] }}
                        ></div>
                        <span className="text-sm font-medium text-gray-900">{cat.categoria}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-900">
                      {cat.cantidad}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium text-gray-900">
                      {formatCurrency(cat.total)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-600">
                      {formatPercentage((cat.total / resumen.totalGastos) * 100)}
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot className="bg-gray-50">
                <tr>
                  <td className="px-6 py-4 font-bold text-gray-900">TOTAL</td>
                  <td className="px-6 py-4 text-right font-bold text-gray-900">
                    {resumen.gastosPorCategoria.reduce((sum, cat) => sum + cat.cantidad, 0)}
                  </td>
                  <td className="px-6 py-4 text-right font-bold text-gray-900">
                    {formatCurrency(resumen.totalGastos)}
                  </td>
                  <td className="px-6 py-4 text-right font-bold text-gray-900">100%</td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>
      )}

      {/* Pendientes de Cobro y Pago */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Pendientes de Cobro */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="p-6 bg-green-50 border-b border-green-200">
            <h3 className="text-lg font-bold text-green-900 flex items-center">
              <CreditCard className="mr-2" size={20} />
              Pendientes de Cobro
            </h3>
            <p className="text-2xl font-bold text-green-700 mt-2">
              {formatCurrency(resumen?.totalPendienteCobro)}
            </p>
          </div>
          {resumen?.pendientesCobro && resumen.pendientesCobro.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Cliente</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Factura</th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Importe</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {resumen.pendientesCobro.map((pendiente, index) => (
                    <tr key={index} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm text-gray-900">{pendiente.cliente}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{pendiente.factura}</td>
                      <td className="px-6 py-4 text-sm text-right font-medium text-green-700">
                        {formatCurrency(pendiente.importe)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="p-6 text-center text-gray-500">
              <p>No hay facturas pendientes de cobro</p>
            </div>
          )}
        </div>

        {/* Pendientes de Pago */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="p-6 bg-red-50 border-b border-red-200">
            <h3 className="text-lg font-bold text-red-900 flex items-center">
              <CreditCard className="mr-2" size={20} />
              Pendientes de Pago
            </h3>
            <p className="text-2xl font-bold text-red-700 mt-2">
              {formatCurrency(resumen?.totalPendientePago)}
            </p>
          </div>
          {resumen?.pendientesPago && resumen.pendientesPago.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Proveedor</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Factura</th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Importe</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {resumen.pendientesPago.map((pendiente, index) => (
                    <tr key={index} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm text-gray-900">{pendiente.proveedor}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{pendiente.factura}</td>
                      <td className="px-6 py-4 text-sm text-right font-medium text-red-700">
                        {formatCurrency(pendiente.importe)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="p-6 text-center text-gray-500">
              <p>No hay facturas pendientes de pago</p>
            </div>
          )}
        </div>
      </div>

      {/* IVA Trimestral */}
      {resumen?.ivaTrimestral && resumen.ivaTrimestral.length > 0 && (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="p-6 border-b border-gray-200">
            <h3 className="text-lg font-bold text-gray-900">Liquidación IVA por Trimestre</h3>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trimestre</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">IVA Repercutido</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">IVA Soportado</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Liquidación</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {resumen.ivaTrimestral.map((trimestre, index) => (
                  <tr key={index} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">{trimestre.trimestre}</td>
                    <td className="px-6 py-4 text-sm text-right text-green-700">
                      {formatCurrency(trimestre.ivaRepercutido)}
                    </td>
                    <td className="px-6 py-4 text-sm text-right text-red-700">
                      {formatCurrency(trimestre.ivaSoportado)}
                    </td>
                    <td className={`px-6 py-4 text-sm text-right font-bold ${trimestre.liquidacion >= 0 ? 'text-blue-700' : 'text-red-700'
                      }`}>
                      {formatCurrency(trimestre.liquidacion)}
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

export default ResumenFinanciero;