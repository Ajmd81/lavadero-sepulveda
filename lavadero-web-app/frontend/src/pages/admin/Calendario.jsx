import { useState, useEffect } from 'react';
import citaService from '../../services/citaService';

const Calendario = () => {
  const [fecha, setFecha] = useState(new Date());
  const [citasDelMes, setCitasDelMes] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [citasSeleccionadas, setCitasSeleccionadas] = useState([]);
  const [diaSeleccionado, setDiaSeleccionado] = useState(null);
  const [todasLasCitas, setTodasLasCitas] = useState([]);

  useEffect(() => { cargarCitas(); }, []);
  useEffect(() => { agruparCitasPorMes(); }, [fecha, todasLasCitas]);

  const cargarCitas = async () => {
    setLoading(true);
    try {
      const response = await citaService.getAll();
      setTodasLasCitas(response.data || []);
      setError(null);
    } catch (err) {
      setError('Error al cargar las citas: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const agruparCitasPorMes = () => {
    const citasPorFecha = {};
    todasLasCitas.forEach(cita => {
      if (cita.fecha) {
        try {
          let fechaCita;
          if (typeof cita.fecha === 'string') {
            if (cita.fecha.includes('/')) {
              const p = cita.fecha.split('/');
              fechaCita = new Date(parseInt(p[2]), parseInt(p[1]) - 1, parseInt(p[0]));
            } else {
              const p = cita.fecha.split('T')[0].split('-');
              fechaCita = new Date(parseInt(p[0]), parseInt(p[1]) - 1, parseInt(p[2]));
            }
          } else return;
          if (fechaCita.getFullYear() === fecha.getFullYear() && fechaCita.getMonth() === fecha.getMonth()) {
            const dia = fechaCita.getDate();
            if (!citasPorFecha[dia]) citasPorFecha[dia] = [];
            citasPorFecha[dia].push(cita);
          }
        } catch { /* skip */ }
      }
    });
    setCitasDelMes(citasPorFecha);
  };

  const getDiasDelMes = () => {
    const year = fecha.getFullYear(), month = fecha.getMonth();
    const primerDia = new Date(year, month, 1);
    const ultimoDia = new Date(year, month + 1, 0);
    const dias = [];
    for (let i = primerDia.getDay(); i > 0; i--) dias.push({ fecha: new Date(year, month, -i + 1), esDelMesActual: false });
    for (let i = 1; i <= ultimoDia.getDate(); i++) dias.push({ fecha: new Date(year, month, i), esDelMesActual: true });
    const restantes = 42 - dias.length;
    for (let i = 1; i <= restantes; i++) dias.push({ fecha: new Date(year, month + 1, i), esDelMesActual: false });
    return dias;
  };

  const mesAnterior = () => setFecha(new Date(fecha.getFullYear(), fecha.getMonth() - 1, 1));
  const mesSiguiente = () => setFecha(new Date(fecha.getFullYear(), fecha.getMonth() + 1, 1));

  const seleccionarDia = (dia) => {
    setDiaSeleccionado(dia);
    setCitasSeleccionadas(citasDelMes[dia] || []);
  };

  const meses = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
  const diasSemana = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
  const diasDelMes = getDiasDelMes();
  const semanas = [];
  for (let i = 0; i < diasDelMes.length; i += 7) semanas.push(diasDelMes.slice(i, i + 7));

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center gap-3 mb-6">
        <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <img src="/assets/icons/calendario.png" alt="Calendario" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
        </div>
        <h1 className="text-3xl font-bold text-gray-900">Calendario de Citas</h1>
      </div>

      {error && <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">{error}</div>}
      {loading && <div className="text-center py-8"><p className="text-gray-500">Cargando citas...</p></div>}

      {!loading && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Calendario */}
          <div className="lg:col-span-2">
            <div className="bg-gray-50 rounded-lg p-4">
              <div className="flex justify-between items-center mb-6">
                <button onClick={mesAnterior} className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded">← Anterior</button>
                <h3 className="text-xl font-bold">{meses[fecha.getMonth()]} {fecha.getFullYear()}</h3>
                <button onClick={mesSiguiente} className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded">Siguiente →</button>
              </div>
              <div className="grid grid-cols-7 gap-1 mb-2">
                {diasSemana.map(dia => <div key={dia} className="text-center font-bold text-gray-700 py-2">{dia}</div>)}
              </div>
              <div className="grid grid-cols-7 gap-1">
                {semanas.map((semana, si) =>
                  semana.map((dia, di) => {
                    const diaNum = dia.fecha.getDate();
                    const tieneCitas = citasDelMes[diaNum] && dia.esDelMesActual;
                    const cantidadCitas = tieneCitas ? citasDelMes[diaNum].length : 0;
                    const esHoy = new Date().getDate() === diaNum && new Date().getMonth() === fecha.getMonth() && new Date().getFullYear() === fecha.getFullYear();
                    return (
                      <button
                        key={`${si}-${di}`}
                        onClick={() => seleccionarDia(diaNum)}
                        disabled={!dia.esDelMesActual}
                        className={`p-2 rounded text-sm h-20 relative transition-all ${!dia.esDelMesActual ? 'bg-gray-200 text-gray-400 cursor-not-allowed' :
                            diaSeleccionado === diaNum ? 'bg-purple-500 text-white font-bold border-2 border-purple-700' :
                              esHoy ? 'bg-blue-500 text-white font-bold border-2 border-blue-700' :
                                tieneCitas ? 'bg-green-100 hover:bg-green-200 border-2 border-green-500' :
                                  'bg-white hover:bg-gray-100 border border-gray-300'
                          }`}
                      >
                        <div className="font-bold">{diaNum}</div>
                        {tieneCitas && <div className="text-xs mt-1 font-semibold text-green-700">{cantidadCitas} {cantidadCitas === 1 ? 'cita' : 'citas'}</div>}
                      </button>
                    );
                  })
                )}
              </div>
            </div>
          </div>

          {/* Panel de detalle */}
          <div className="lg:col-span-1">
            <div className="bg-gray-50 rounded-lg p-4">
              <h4 className="text-lg font-bold mb-4">
                {diaSeleccionado ? `Citas del ${diaSeleccionado}/${fecha.getMonth() + 1}/${fecha.getFullYear()}` : 'Citas del día'}
              </h4>
              {!diaSeleccionado ? (
                <p className="text-gray-500 text-center py-8">Selecciona un día para ver sus citas</p>
              ) : citasSeleccionadas.length === 0 ? (
                <p className="text-gray-500 text-center py-8">No hay citas programadas para este día</p>
              ) : (
                <div className="space-y-3 max-h-96 overflow-y-auto">
                  {[...citasSeleccionadas].sort((a, b) => {
                    const ha = a.hora ? a.hora.substring(0, 5) : '00:00';
                    const hb = b.hora ? b.hora.substring(0, 5) : '00:00';
                    return ha.localeCompare(hb);
                  }).map((cita, idx) => (
                    <div key={idx} className="bg-white rounded p-3 border-l-4 border-blue-500 shadow-sm">
                      {/* Hora + ID en la misma fila */}
                      <div className="flex items-center justify-between mb-1">
                        <div className="font-bold text-blue-600">
                          {cita.hora ? cita.hora.substring(0, 5) : '—'}
                        </div>
                        <span className="text-xs font-mono text-gray-400 bg-gray-100 px-1.5 py-0.5 rounded">
                          #{cita.id}
                        </span>
                      </div>
                      <div className="text-sm font-semibold">{cita.nombre}</div>
                      <div className="text-xs text-gray-600">{cita.modeloVehiculo}</div>
                      <div className="text-xs text-gray-500 mt-1">{cita.tipoLavado}</div>
                      <div className="text-xs mt-2">
                        <span className={`px-2 py-1 rounded font-semibold ${cita.estado === 'CONFIRMADA' ? 'bg-green-100 text-green-800' :
                            cita.estado === 'CANCELADA' ? 'bg-red-100 text-red-800' :
                              'bg-yellow-100 text-yellow-800'
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
        </div>
      )}
    </div>
  );
};

export default Calendario;