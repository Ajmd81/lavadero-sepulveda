import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Users, Calendar, FileText, Euro, AlertCircle, CheckCircle, Loader2, RefreshCw } from 'lucide-react';
import { format } from 'date-fns';
import citaService from '../../services/citaService';
import clienteService from '../../services/clienteService';
import facturaService from '../../services/facturaService';

// Traduce un error de axios/fetch a un mensaje entendible
const describirError = (error) => {
  if (!error) return 'Error desconocido';
  const status = error.response?.status;
  if (!error.response) return 'Sin conexión con el servidor';
  if (status === 401 || status === 403) return 'Sesión caducada o sin permisos. Vuelve a iniciar sesión';
  if (status === 404) return 'Servicio no encontrado (404)';
  if (status === 429) return 'Demasiadas peticiones. Espera unos segundos';
  if (status >= 500) return `Error interno del servidor (${status})`;
  return `Error ${status}`;
};

const Dashboard = () => {
  // Reloj que se actualiza cada minuto para que las citas pasadas desaparezcan solas
  const [ahora, setAhora] = useState(() => new Date());

  useEffect(() => {
    const intervalo = setInterval(() => setAhora(new Date()), 60 * 1000);
    return () => clearInterval(intervalo);
  }, []);

  const citasQuery = useQuery({
    queryKey: ['citas-dashboard'],
    queryFn: () => citaService.getAll(),
  });

  const clientesQuery = useQuery({
    queryKey: ['clientes-dashboard'],
    queryFn: () => clienteService.getCount(),
  });

  const facturasQuery = useQuery({
    queryKey: ['facturas-dashboard'],
    queryFn: () => facturaService.getAll(0, 1000),
  });

  const { data: citas, isLoading: loadingCitas } = citasQuery;
  const { data: clientes } = clientesQuery;
  const { data: facturas } = facturasQuery;

  // ─── Estado de conexión para "Avisos Importantes" ───────────────────────
  const servicios = [
    { nombre: 'Citas', query: citasQuery },
    { nombre: 'Clientes', query: clientesQuery },
    { nombre: 'Facturas', query: facturasQuery },
  ];

  const fallos = servicios.filter(s => s.query.isError);
  // Carga inicial (no los refrescos en segundo plano, para que el aviso no parpadee)
  const cargandoInicial = servicios.some(s => s.query.isPending);
  // Reintento en curso de algún servicio que había fallado
  const reintentando = fallos.some(s => s.query.isFetching);

  const reintentar = () => {
    fallos.forEach(s => s.query.refetch());
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return dateString;
      return format(date, 'dd/MM/yyyy');
    } catch {
      return dateString;
    }
  };

  const today = format(ahora, 'yyyy-MM-dd');
  const horaActual = format(ahora, 'HH:mm');

  const citasArray = Array.isArray(citas?.data)
    ? citas.data
    : (citas?.data?.content && Array.isArray(citas.data.content)
      ? citas.data.content
      : []);

  const facturasArray = Array.isArray(facturas?.data)
    ? facturas.data
    : (facturas?.data?.content && Array.isArray(facturas.data.content)
      ? facturas.data.content
      : []);

  // Total de clientes: acepta el formato del endpoint paginado y el de /count
  const totalClientes =
    clientes?.data?.totalItems ??     // GET /api/clientes (paginado del backend)
    clientes?.data?.total ??          // GET /api/clientes/count
    clientes?.data?.totalElements ??  // Page de Spring sin envolver
    (Array.isArray(clientes?.data?.content) ? clientes.data.content.length : 0);

  const totalFacturas = facturasArray.length || 0;

  // "HH:mm:ss" o "HH:mm" → "HH:mm" para comparar como texto
  const normalizarHora = (hora) => (hora ? String(hora).slice(0, 5) : '');

  // Citas de hoy que aún no han llegado a su hora y siguen pendientes de realizar
  const ESTADOS_EXCLUIDOS = ['COMPLETADA', 'CANCELADA', 'NO_PRESENTADO', 'EN_PROCESO'];

  const proximasCitas = (citasArray || [])
    .filter(c =>
      c.fecha === today &&
      !ESTADOS_EXCLUIDOS.includes(c.estado) &&
      normalizarHora(c.hora) >= horaActual
    )
    .sort((a, b) => normalizarHora(a.hora).localeCompare(normalizarHora(b.hora)))
    .slice(0, 5);

  const mesActual = new Date().getMonth() + 1;
  const anioActual = new Date().getFullYear();

  const facturacionMes = (facturasArray || [])
    .filter(f => {
      if (!f.fecha) return false;
      let fechaStr = f.fecha;
      if (fechaStr instanceof Date) fechaStr = format(fechaStr, 'yyyy-MM-dd');
      let mes, anio;
      if (fechaStr.includes('/')) {
        const partes = fechaStr.split('/');
        mes = parseInt(partes[1]);
        anio = parseInt(partes[2]);
      } else if (fechaStr.includes('-')) {
        const fechaSolo = fechaStr.split('T')[0];
        const partes = fechaSolo.split('-');
        anio = parseInt(partes[0]);
        mes = parseInt(partes[1]);
      } else {
        return false;
      }
      return mes === mesActual && anio === anioActual;
    })
    .reduce((sum, f) => {
      const total = typeof f.total === 'number' ? f.total : parseFloat(f.total) || 0;
      return sum + total;
    }, 0);

  const stats = [
    {
      icon: Calendar,
      label: 'Próximas Citas',
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
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center gap-3">
          <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <img src="/assets/icons/panel.png" alt="Dashboard" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
          </div>
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6">
        {stats.map((stat, index) => (
          <div
            key={index}
            className="bg-white rounded-lg shadow p-6"
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
                    <span className={`inline-block px-2 py-1 text-xs rounded ${
                      cita.estado === 'COMPLETADA' ? 'bg-green-100 text-green-800' :
                      cita.estado === 'PENDIENTE'  ? 'bg-yellow-100 text-yellow-800' :
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

      <div className="bg-white rounded-lg shadow">
        <div className="p-6 border-b">
          <h2 className="text-xl font-bold text-gray-800">Avisos Importantes</h2>
        </div>
        <div className="p-6 space-y-3">
          {fallos.length > 0 ? (
            <>
              {fallos.map(({ nombre, query }) => (
                <div key={nombre} className="flex items-start gap-3 p-4 bg-red-50 rounded-lg">
                  <AlertCircle className="text-red-600 flex-shrink-0 mt-1" size={20} />
                  <div>
                    <p className="font-semibold text-red-900">Fallo de conexión: {nombre}</p>
                    <p className="text-sm text-red-700">{describirError(query.error)}</p>
                  </div>
                </div>
              ))}
              <button
                type="button"
                onClick={reintentar}
                disabled={reintentando}
                className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 disabled:opacity-60"
              >
                <RefreshCw size={16} className={reintentando ? 'animate-spin' : ''} />
                {reintentando ? 'Reintentando...' : 'Reintentar'}
              </button>
            </>
          ) : cargandoInicial ? (
            <div className="flex items-start gap-3 p-4 bg-gray-50 rounded-lg">
              <Loader2 className="text-gray-500 flex-shrink-0 mt-1 animate-spin" size={20} />
              <div>
                <p className="font-semibold text-gray-800">Comprobando conexión...</p>
                <p className="text-sm text-gray-600">Verificando el estado de los servicios</p>
              </div>
            </div>
          ) : (
            <div className="flex items-start gap-3 p-4 bg-green-50 rounded-lg">
              <CheckCircle className="text-green-600 flex-shrink-0 mt-1" size={20} />
              <div>
                <p className="font-semibold text-green-900">Todo funciona correctamente</p>
                <p className="text-sm text-green-700">Citas, clientes y facturas conectados sin incidencias</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;