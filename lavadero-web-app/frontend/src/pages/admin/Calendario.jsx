import { useState, useEffect } from 'react';
import citaService from '../../services/citaService';

const Calendario = () => {
  const [fecha, setFecha] = useState(new Date());
  const [citas, setCitas] = useState([]);
  const [citasDelMes, setCitasDelMes] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [citasSeleccionadas, setCitasSeleccionadas] = useState([]);

  // Cargar citas al montar el componente
  useEffect(() => {
    cargarCitas();
  }, [fecha]);

  // Cargar todas las citas
  const cargarCitas = async () => {
    setLoading(true);
    try {
      const response = await citaService.getAll();
      const todasLasCitas = response.data || [];
      setCitas(todasLasCitas);
      
      // Agrupar citas por fecha del mes actual
      const citasPorFecha = {};
      todasLasCitas.forEach(cita => {
        if (cita.fecha) {
          const fechaCita = new Date(cita.fecha);
          if (fechaCita.getFullYear() === fecha.getFullYear() && 
              fechaCita.getMonth() === fecha.getMonth()) {
            const dia = fechaCita.getDate();
            if (!citasPorFecha[dia]) {
              citasPorFecha[dia] = [];
            }
            citasPorFecha[dia].push(cita);
          }
        }
      });
      
      setCitasDelMes(citasPorFecha);
      setError(null);
    } catch (err) {
      setError('Error al cargar las citas: ' + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Obtener días del mes
  const getDiasDelMes = () => {
    const year = fecha.getFullYear();
    const month = fecha.getMonth();
    const primerDia = new Date(year, month, 1);
    const ultimoDia = new Date(year, month + 1, 0);
    const diasDelMes = [];
    
    // Días del mes anterior para llenar la primera semana
    const diasAnterior = primerDia.getDay();
    for (let i = diasAnterior; i > 0; i--) {
      const dia = new Date(year, month, -i + 1);
      diasDelMes.push({ fecha: dia, esDelMesActual: false });
    }
    
    // Días del mes actual
    for (let i = 1; i <= ultimoDia.getDate(); i++) {
      diasDelMes.push({ fecha: new Date(year, month, i), esDelMesActual: true });
    }
    
    // Días del mes siguiente para llenar la última semana
    const diasSiguiente = 42 - diasDelMes.length; // 6 semanas * 7 días = 42
    for (let i = 1; i <= diasSiguiente; i++) {
      const dia = new Date(year, month + 1, i);
      diasDelMes.push({ fecha: dia, esDelMesActual: false });
    }
    
    return diasDelMes;
  };

  // Cambiar mes anterior
  const mesAnterior = () => {
    setFecha(new Date(fecha.getFullYear(), fecha.getMonth() - 1, 1));
  };

  // Cambiar mes siguiente
  const mesSiguiente = () => {
    setFecha(new Date(fecha.getFullYear(), fecha.getMonth() + 1, 1));
  };

  // Seleccionar un día para ver sus citas
  const seleccionarDia = (dia) => {
    if (citasDelMes[dia]) {
      setCitasSeleccionadas(citasDelMes[dia]);
    } else {
      setCitasSeleccionadas([]);
    }
  };

  // Formatos
  const meses = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 
                 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
  const diasSemana = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
  const diasDelMes = getDiasDelMes();
  const semanas = [];
  
  for (let i = 0; i < diasDelMes.length; i += 7) {
    semanas.push(diasDelMes.slice(i, i + 7));
  }

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-2xl font-bold mb-6">Calendario de Citas</h2>

      {error && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      {loading && (
        <div className="text-center py-8">
          <p className="text-gray-500">Cargando citas...</p>
        </div>
      )}

      {!loading && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Calendario */}
          <div className="lg:col-span-2">
            <div className="bg-gray-50 rounded-lg p-4">
              {/* Encabezado con mes y año */}
              <div className="flex justify-between items-center mb-6">
                <button
                  onClick={mesAnterior}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded"
                >
                  ← Anterior
                </button>
                <h3 className="text-xl font-bold">
                  {meses[fecha.getMonth()]} {fecha.getFullYear()}
                </h3>
                <button
                  onClick={mesSiguiente}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded"
                >
                  Siguiente →
                </button>
              </div>

              {/* Días de la semana */}
              <div className="grid grid-cols-7 gap-1 mb-2">
                {diasSemana.map(dia => (
                  <div key={dia} className="text-center font-bold text-gray-700 py-2">
                    {dia}
                  </div>
                ))}
              </div>

              {/* Celdas del calendario */}
              <div className="grid grid-cols-7 gap-1">
                {semanas.map((semana, semanaIdx) => (
                  semana.map((dia, diaIdx) => {
                    const diaNum = dia.fecha.getDate();
                    const tieneCitas = citasDelMes[diaNum] && dia.esDelMesActual;
                    const cantidadCitas = tieneCitas ? citasDelMes[diaNum].length : 0;
                    const esHoy = 
                      new Date().getDate() === diaNum &&
                      new Date().getMonth() === fecha.getMonth() &&
                      new Date().getFullYear() === fecha.getFullYear();

                    return (
                      <button
                        key={`${semanaIdx}-${diaIdx}`}
                        onClick={() => dia.esDelMesActual && seleccionarDia(diaNum)}
                        disabled={!dia.esDelMesActual}
                        className={`p-2 rounded text-sm h-20 relative ${
                          !dia.esDelMesActual 
                            ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                            : esHoy
                            ? 'bg-blue-500 text-white font-bold'
                            : tieneCitas
                            ? 'bg-green-100 hover:bg-green-200 border-2 border-green-500'
                            : 'bg-white hover:bg-gray-100 border border-gray-300'
                        }`}
                      >
                        <div className="font-bold">{diaNum}</div>
                        {tieneCitas && (
                          <div className="text-xs mt-1 font-semibold text-green-700">
                            {cantidadCitas} {cantidadCitas === 1 ? 'cita' : 'citas'}
                          </div>
                        )}
                      </button>
                    );
                  })
                ))}
              </div>
            </div>
          </div>

          {/* Panel de detalles de citas */}
          <div className="lg:col-span-1">
            <div className="bg-gray-50 rounded-lg p-4">
              <h4 className="text-lg font-bold mb-4">Citas del día</h4>
              
              {citasSeleccionadas.length === 0 ? (
                <p className="text-gray-500 text-center py-8">
                  Selecciona un día para ver sus citas
                </p>
              ) : (
                <div className="space-y-3 max-h-96 overflow-y-auto">
                  {citasSeleccionadas.sort((a, b) => {
                    const horaA = a.hora ? a.hora.substring(0, 5) : '00:00';
                    const horaB = b.hora ? b.hora.substring(0, 5) : '00:00';
                    return horaA.localeCompare(horaB);
                  }).map((cita, idx) => (
                    <div
                      key={idx}
                      className="bg-white rounded p-3 border-l-4 border-blue-500 shadow-sm"
                    >
                      <div className="font-bold text-blue-600">
                        {cita.hora ? cita.hora.substring(0, 5) : '—'}
                      </div>
                      <div className="text-sm font-semibold">{cita.nombre}</div>
                      <div className="text-xs text-gray-600">{cita.modeloVehiculo}</div>
                      <div className="text-xs text-gray-500 mt-1">
                        {cita.tipoLavado}
                      </div>
                      <div className="text-xs mt-2">
                        <span className={`px-2 py-1 rounded font-semibold ${
                          cita.estado === 'CONFIRMADA' ? 'bg-green-100 text-green-800' :
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
