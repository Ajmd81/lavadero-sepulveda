import { useQuery } from '@tanstack/react-query';
import axios from 'axios';
import { TrendingUp, TrendingDown, AlertCircle } from 'lucide-react';
import CustomIcon from '../../components/CustomIcon';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const Dashboard = () => {
  // Query para obtener estadísticas
  const { data: stats, isLoading, error } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: async () => {
      const { data } = await axios.get(`${API_URL}/dashboard/stats`);
      return data;
    }
  });

  // KPI Card con icono personalizado
  const KPICard = ({ customIcon, titulo, valor, cambio, isPositive, color = "blue" }) => (
    <div className={`bg-white rounded-xl shadow-lg hover:shadow-xl transition-all p-6 border-l-4 border-${color}-500`}>
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center gap-3 mb-3">
            <div className={`p-3 bg-${color}-100 rounded-lg`}>
              <CustomIcon name={customIcon} size={32} />
            </div>
            <div>
              <p className="text-sm text-gray-600 font-medium">{titulo}</p>
              <p className="text-3xl font-bold text-gray-900">{valor}</p>
            </div>
          </div>
          
          {cambio && (
            <div className={`flex items-center gap-1 text-sm ${
              isPositive ? 'text-green-600' : 'text-red-600'
            }`}>
              {isPositive ? <TrendingUp size={16} /> : <TrendingDown size={16} />}
              <span className="font-medium">{cambio}</span>
              <span className="text-gray-500">vs mes anterior</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );

  // Acceso rápido con iconos personalizados
  const AccesoRapido = ({ customIcon, titulo, descripcion, to }) => (
    <a
      href={to}
      className="block bg-white rounded-lg shadow hover:shadow-lg transition-all p-6 group"
    >
      <div className="flex items-center gap-4">
        <div className="p-3 bg-blue-100 rounded-lg group-hover:bg-blue-200 transition-colors">
          <CustomIcon name={customIcon} size={40} />
        </div>
        <div>
          <h3 className="font-bold text-gray-900 group-hover:text-blue-600 transition-colors">
            {titulo}
          </h3>
          <p className="text-sm text-gray-600">{descripcion}</p>
        </div>
      </div>
    </a>
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
          <span>Error al cargar el dashboard: {error.message}</span>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header con Logo */}
      <div className="bg-white rounded-xl shadow-lg p-6">
        <div className="flex items-center gap-4">
          <CustomIcon name="logo" size={80} />
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
            <p className="text-gray-600">Panel de control - Lavadero Sepúlveda</p>
          </div>
        </div>
      </div>

      {/* KPIs Principales */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <KPICard
          customIcon="cliente"
          titulo="Total Clientes"
          valor={stats?.totalClientes || 0}
          cambio="+12.5%"
          isPositive={true}
          color="blue"
        />
        <KPICard
          customIcon="citas"
          titulo="Citas Hoy"
          valor={stats?.citasHoy || 0}
          cambio="+5.2%"
          isPositive={true}
          color="green"
        />
        <KPICard
          customIcon="invoice"
          titulo="Facturas del Mes"
          valor={stats?.facturasDelMes || 0}
          cambio="-2.1%"
          isPositive={false}
          color="purple"
        />
        <KPICard
          customIcon="estadoFinanciero"
          titulo="Ingresos del Mes"
          valor={`€${stats?.ingresosDelMes || 0}`}
          cambio="+18.3%"
          isPositive={true}
          color="orange"
        />
      </div>

      {/* Accesos Rápidos */}
      <div>
        <h2 className="text-xl font-bold text-gray-900 mb-4">Accesos Rápidos</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <AccesoRapido
            customIcon="cliente"
            titulo="Gestión de Clientes"
            descripcion="Ver y gestionar clientes"
            to="/admin/clientes"
          />
          <AccesoRapido
            customIcon="citas"
            titulo="Nueva Cita"
            descripcion="Programar una nueva cita"
            to="/admin/citas"
          />
          <AccesoRapido
            customIcon="facturacion"
            titulo="Facturación"
            descripcion="Emitir y gestionar facturas"
            to="/admin/facturacion"
          />
          <AccesoRapido
            customIcon="proveedor"
            titulo="Proveedores"
            descripcion="Gestión de proveedores"
            to="/admin/proveedores"
          />
          <AccesoRapido
            customIcon="contabilidad"
            titulo="Contabilidad"
            descripcion="Reportes contables"
            to="/admin/contabilidad"
          />
          <AccesoRapido
            customIcon="estadoFinanciero"
            titulo="Resumen Financiero"
            descripcion="Estado financiero actual"
            to="/admin/resumen-financiero"
          />
        </div>
      </div>

      {/* Gráfico con Icono */}
      <div className="bg-white rounded-xl shadow-lg p-6">
        <div className="flex items-center gap-3 mb-6">
          <CustomIcon name="analisis" size={40} />
          <div>
            <h2 className="text-xl font-bold text-gray-900">Análisis de Rendimiento</h2>
            <p className="text-gray-600">Últimos 7 días</p>
          </div>
        </div>
        
        {/* Aquí iría tu gráfico (Recharts, Chart.js, etc.) */}
        <div className="h-64 bg-gray-100 rounded-lg flex items-center justify-center">
          <p className="text-gray-500">Gráfico de análisis</p>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
