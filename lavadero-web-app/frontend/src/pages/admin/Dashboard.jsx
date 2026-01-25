import { useQuery } from '@tanstack/react-query';
import { Users, Calendar, FileText, DollarSign, TrendingUp, AlertCircle } from 'lucide-react';
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

  const today = format(new Date(), 'yyyy-MM-dd');
  const citasHoy = citas?.data?.filter(c => c.fecha === today) || [];
  const totalClientes = clientes?.data?.length || 0;
  const totalFacturas = facturas?.data?.length || 0;
  
  const facturacionMes = facturas?.data
    ?.filter(f => f.fecha?.startsWith(format(new Date(), 'yyyy-MM')))
    ?.reduce((sum, f) => sum + (f.total || 0), 0) || 0;

  const stats = [
    {
      icon: Calendar,
      label: 'Citas Hoy',
      value: citasHoy.length,
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
      icon: DollarSign,
      label: 'Facturación Mes',
      value: `${facturacionMes.toFixed(2)}€`,
      color: 'bg-orange-500',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat, index) => (
          <div key={index} className="bg-white rounded-lg shadow p-6">
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

      {/* Citas de Hoy */}
      <div className="bg-white rounded-lg shadow">
        <div className="p-6 border-b">
          <h2 className="text-xl font-bold text-gray-800">Citas de Hoy</h2>
        </div>
        <div className="p-6">
          {loadingCitas ? (
            <p className="text-gray-500">Cargando citas...</p>
          ) : citasHoy.length === 0 ? (
            <p className="text-gray-500">No hay citas programadas para hoy</p>
          ) : (
            <div className="space-y-3">
              {citasHoy.map((cita) => (
                <div key={cita.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-semibold text-gray-800">{cita.nombre}</p>
                    <p className="text-sm text-gray-600">{cita.modeloVehiculo} - {cita.tipoLavado}</p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-blue-600">{cita.hora}</p>
                    <span className={`inline-block px-2 py-1 text-xs rounded ${
                      cita.estado === 'COMPLETADA' ? 'bg-green-100 text-green-800' :
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
      <div className="bg-white rounded-lg shadow">
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
