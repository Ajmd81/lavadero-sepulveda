import { useQuery } from '@tanstack/react-query';
import { Users, Calendar, FileText, Euro, TrendingUp, AlertCircle } from 'lucide-react';

import citaService from '../../services/citaService';
import clienteService from '../../services/clienteService';
import facturaService from '../../services/facturaService';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

const Dashboard = () => {
  const { data: citas, isLoading: loadingCitas } = useQuery({
    queryKey: ['citas-dashboard'],
    queryFn: () => citaService.getAll(),
  });

  const { data: clientes } = useQuery({
    queryKey: ['clientes-dashboard'],
    queryFn: () => clienteService.getAll(),
  });

  const { data: facturas } = useQuery({
    queryKey: ['facturas-dashboard'],
    queryFn: () => facturaService.getAll(),
  });

  const formatDate = (dateString) => {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      // Verificar si la fecha es válida
      if (isNaN(date.getTime())) return dateString;
      return format(date, 'dd/MM/yyyy');
    } catch (error) {
      return dateString;
    }
  };

  const today = format(new Date(), 'yyyy-MM-dd');
  const now = new Date(); // Fecha y hora actual completa para comparación precisa si fuera necesario

  // Filtrar citas pendientes de HOY
  const proximasCitas = citas?.data
    ?.filter(c => c.fecha === today && c.estado !== 'COMPLETADA' && c.estado !== 'CANCELADA')
    ?.sort((a, b) => {
      // Ordenar primero por fecha
      if (a.fecha !== b.fecha) return a.fecha.localeCompare(b.fecha);
      // Luego por hora si la fecha es igual
      return a.hora.localeCompare(b.hora);
    })
    ?.slice(0, 5) // Mostrar solo las próximas 5
    || [];

  const totalClientes = clientes?.data?.length || 0;
  const totalFacturas = facturas?.data?.length || 0;

  const facturacionMes = facturas?.data
    ?.filter(f => f.fecha?.startsWith(format(new Date(), 'yyyy-MM')))
    ?.reduce((sum, f) => sum + (f.total || 0), 0) || 0;

  const stats = [
    {
      icon: Calendar,
      label: 'Próximas Citas', // Changed label
      value: proximasCitas.length,
      color: 'bg-blue-500',
    },
    {
      icon: Users,
      label: 'Total Clientes',
      value: totalClientes,
      color: 'bg-green-500',
    },
    {
      icon: FileText,
      label: 'Facturas',
      value: totalFacturas,
      color: 'bg-purple-500',
    },
    {
      icon: Euro,
      label: 'Facturación Mes',
      value: `${facturacionMes.toFixed(2)}€`,
      color: 'bg-orange-500',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white rounded-lg shadow p-6" style={{ marginBottom: '40px' }}>
        <div className="flex items-center gap-3">
          <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <img src="/assets/icons/panel.png" alt="Dashboard" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
          </div>
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        </div>
      </div>
      {/* Stats Grid */}
      {/* Stats Grid */}
      <div className="grid grid-cols-1 gap-6">
        {stats.map((stat, index) => (
          <div
            key={index}
            className={`bg-white rounded-lg shadow p-6 ${stat.label === 'Próximas Citas' ? 'pt-12' : ''
              } ${stat.label === 'Facturación Mes' ? 'pb-12' : ''
              }`}
            style={stat.label === 'Facturación Mes' ? { marginBottom: '40px' } : {}}
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">{stat.label}</p>
                <p className="text-2xl font-bold text-gray-800">{stat.value}</p>
              </div>
              <div className={`${stat.color} p-3 rounded-lg`}>
                <stat.icon className="text-white" size={24} />
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Próximas Citas */}
      <div className="bg-white rounded-lg shadow">
        <div className="p-6 border-b">
          <h2 className="text-xl font-bold text-gray-800">Próximas Citas</h2>
        </div>
        <div className="p-6">
          {loadingCitas ? (
            <p className="text-gray-500">Cargando citas...</p>
          ) : proximasCitas.length === 0 ? (
            <p className="text-gray-500">No hay citas próximas programadas</p>
          ) : (
            <div className="space-y-3">
              {proximasCitas.map((cita) => (
                <div key={cita.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-semibold text-gray-800">{cita.nombre}</p>
                    <p className="text-sm text-gray-600">
                      {formatDate(cita.fecha)} - {cita.modeloVehiculo} ({cita.tipoLavado})
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-blue-600">{cita.hora}</p>
                    <span className={`inline-block px-2 py-1 text-xs rounded ${cita.estado === 'COMPLETADA' ? 'bg-green-100 text-green-800' :
                      cita.estado === 'PENDIENTE' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                      {cita.estado}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Alertas y Notificaciones */}
      <div className="bg-white rounded-lg shadow" style={{ marginTop: '30px' }}>
        <div className="p-6 border-b">
          <h2 className="text-xl font-bold text-gray-800">Avisos Importantes</h2>
        </div>
        <div className="p-6">
          <div className="flex items-start gap-3 p-4 bg-blue-50 rounded-lg">
            <AlertCircle className="text-blue-600 flex-shrink-0 mt-1" size={20} />
            <div>
              <p className="font-semibold text-blue-900">Sistema funcionando correctamente</p>
              <p className="text-sm text-blue-700">Todas las funcionalidades están operativas</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
