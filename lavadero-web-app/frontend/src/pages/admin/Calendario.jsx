import { useState, useEffect, useCallback } from 'react';
import citaService from '../../services/citaService';
import api from '../../services/api';

// ── Helpers ──────────────────────────────────────────────────────────────────

const MESES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
];
const DIAS_SEMANA = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];

const TIPOS_CIERRE = [
  { value: 'FESTIVO',       label: '🎉 Festivo' },
  { value: 'VACACIONES',    label: '🏖️ Vacaciones' },
  { value: 'MANTENIMIENTO', label: '🔧 Mantenimiento' },
  { value: 'OTRO',          label: '📌 Otro' },
];

function parseFecha(fechaStr) {
  if (!fechaStr) return null;
  const s = fechaStr.includes('T') ? fechaStr.split('T')[0] : fechaStr;
  const sep = s.includes('/') ? '/' : '-';
  const p = s.split(sep);
  if (sep === '/') return new Date(+p[2], +p[1] - 1, +p[0]);
  return new Date(+p[0], +p[1] - 1, +p[2]);
}

function toISODate(d) {
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mm}-${dd}`;
}

function getDiasDelMes(fecha) {
  const year = fecha.getFullYear(), month = fecha.getMonth();
  const primerDia = new Date(year, month, 1);
  const ultimoDia = new Date(year, month + 1, 0);
  const dias = [];
  for (let i = primerDia.getDay(); i > 0; i--)
    dias.push({ fecha: new Date(year, month, -i + 1), esDelMesActual: false });
  for (let i = 1; i <= ultimoDia.getDate(); i++)
    dias.push({ fecha: new Date(year, month, i), esDelMesActual: true });
  const restantes = 42 - dias.length;
  for (let i = 1; i <= restantes; i++)
    dias.push({ fecha: new Date(year, month + 1, i), esDelMesActual: false });
  return dias;
}

// ── Componente ────────────────────────────────────────────────────────────────

const Calendario = () => {
  const [fecha, setFecha] = useState(new Date());
  const [citasDelMes, setCitasDelMes] = useState({});
  const [diasCerrados, setDiasCerrados] = useState({});
  const [disponibilidadMes, setDisponibilidadMes] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [citasSeleccionadas, setCitasSeleccionadas] = useState([]);
  const [diaSeleccionado, setDiaSeleccionado] = useState(null);
  const [todasLasCitas, setTodasLasCitas] = useState([]);

  const [mostrarFormCierre, setMostrarFormCierre] = useState(false);
  const [guardandoCierre, setGuardandoCierre] = useState(false);
  const [formCierre, setFormCierre] = useState({ tipo: 'FESTIVO', motivo: '' });

  useEffect(() => { 
    cargarCitas(); 
  }, []);

  useEffect(() => {
    agruparCitasPorMes();
    cargarDiasCerradosDelMes();
    cargarDisponibilidadDelMes();
  }, [fecha, todasLasCitas]);

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

  const cargarDiasCerradosDelMes = async () => {
    try {
      const year = fecha.getFullYear();
      const month = fecha.getMonth();
      const inicio = toISODate(new Date(year, month, 1));
      const fin    = toISODate(new Date(year, month + 1, 0));
      const response = await citaService.getDiasCerradosPorRango(inicio, fin);
      const mapa = {};
      (response.data || []).forEach(d => { mapa[d.fecha] = d; });
      setDiasCerrados(mapa);
    } catch {
      // endpoint aún no desplegado — ignorar silenciosamente
    }
  };

  // ✅ NUEVO: Cargar disponibilidad del mes
  const cargarDisponibilidadDelMes = async () => {
    try {
      const year = fecha.getFullYear();
      const month = fecha.getMonth() + 1;
      const response = await api.get('/citas/disponibilidad-mes', {
        params: { anio: year, mes: month }
      });
      setDisponibilidadMes(response.data || {});
    } catch (err) {
      console.warn('No se pudo cargar disponibilidad:', err.message);
      // No es crítico si falla este endpoint
    }
  };

  const agruparCitasPorMes = () => {
    const citasPorFecha = {};
    todasLasCitas.forEach(cita => {
      const fechaCita = parseFecha(cita.fecha);
      if (!fechaCita) return;
      if (
        fechaCita.getFullYear() === fecha.getFullYear() &&
        fechaCita.getMonth()    === fecha.getMonth()
      ) {
        const dia = fechaCita.getDate();
        if (!citasPorFecha[dia]) citasPorFecha[dia] = [];
        citasPorFecha[dia].push(cita);
      }
    });
    setCitasDelMes(citasPorFecha);
  };

  const mesAnterior  = () => setFecha(new Date(fecha.getFullYear(), fecha.getMonth() - 1, 1));
  const mesSiguiente = () => setFecha(new Date(fecha.getFullYear(), fecha.getMonth() + 1, 1));

  const seleccionarDia = (diaNum) => {
    setDiaSeleccionado(diaNum);
    setCitasSeleccionadas(citasDelMes[diaNum] || []);
    setMostrarFormCierre(false);
    setFormCierre({ tipo: 'FESTIVO', motivo: '' });
  };

  const fechaSeleccionadaISO = diaSeleccionado
    ? toISODate(new Date(fecha.getFullYear(), fecha.getMonth(), diaSeleccionado))
    : null;

  const infoCierre = fechaSeleccionadaISO ? diasCerrados[fechaSeleccionadaISO] : null;

  const handleMarcarCerrado = async () => {
    if (!fechaSeleccionadaISO) return;
    setGuardandoCierre(true);
    try {
      await citaService.marcarDiaCerrado({
        fecha:  fechaSeleccionadaISO,
        tipo:   formCierre.tipo,
        motivo: formCierre.motivo || null,
      });
      await cargarDiasCerradosDelMes();
      setMostrarFormCierre(false);
      setFormCierre({ tipo: 'FESTIVO', motivo: '' });
    } catch (err) {
      const msg = err.response?.data?.error || err.message;
      setError('No se pudo marcar el día: ' + msg);
    } finally {
      setGuardandoCierre(false);
    }
  };

  const handleReabrirDia = async () => {
    if (!fechaSeleccionadaISO) return;
    setGuardandoCierre(true);
    try {
      await citaService.eliminarDiaCerradoPorFecha(fechaSeleccionadaISO);
      await cargarDiasCerradosDelMes();
    } catch (err) {
      setError('No se pudo reabrir el día: ' + err.message);
    } finally {
      setGuardandoCierre(false);
    }
  };

  // ✅ NUEVO: Obtener datos de disponibilidad para un día
  const obtenerDisponibilidadDia = (diaNum) => {
    const isoFecha = toISODate(new Date(fecha.getFullYear(), fecha.getMonth(), diaNum));
    return disponibilidadMes[isoFecha];
  };

  // ✅ NUEVO: Determinar color según disponibilidad
  const obtenerColorPorDisponibilidad = (diaNum) => {
    const disponibilidad = obtenerDisponibilidadDia(diaNum);
    if (!disponibilidad) return null;

    const ocupacion = disponibilidad.porcentajeOcupacion || 0;

    if (disponibilidad.esDiaCerrado) return 'text-red-400';
    if (ocupacion === 0) return 'text-green-400';      // Verde: sin ocupación
    if (ocupacion <= 25) return 'text-emerald-400';    // Esmeralda: 0-25%
    if (ocupacion <= 50) return 'text-yellow-400';     // Amarillo: 25-50%
    if (ocupacion <= 75) return 'text-orange-400';     // Naranja: 50-75%
    return 'text-red-400';                             // Rojo: 75-100%
  };

  const diasDelMes = getDiasDelMes(fecha);
  const semanas = [];
  for (let i = 0; i < diasDelMes.length; i += 7) semanas.push(diasDelMes.slice(i, i + 7));

  const hoy = new Date();

  return (
    <div className="bg-sepulveda-graphite rounded-lg shadow-lg p-6">

      {/* ── Cabecera ── */}
      <div className="flex items-center gap-3 mb-6">
        <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <img src="/assets/icons/calendario.png" alt="Calendario" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
        </div>
        <div>
          <h1 className="text-3xl font-bold text-sepulveda-silver-light">Calendario de Citas</h1>
          <p className="text-sm text-sepulveda-silver/70">Disponibilidad y gestión de días cerrados</p>
        </div>
      </div>

      {/* ── Error ── */}
      {error && (
        <div className="mb-4 p-4 bg-red-900/40 border border-red-500 text-red-300 rounded-lg flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError(null)} className="font-bold ml-4 text-red-300 hover:text-white">✕</button>
        </div>
      )}

      {loading && (
        <div className="text-center py-8">
          <p className="text-sepulveda-silver">Cargando citas y disponibilidad...</p>
        </div>
      )}

      {!loading && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

          {/* ── Calendario ── */}
          <div className="lg:col-span-2">
            <div className="bg-sepulveda-carbon rounded-lg p-4 border border-sepulveda-silver/20">

              {/* Navegación mes */}
              <div className="flex justify-between items-center mb-6">
                <button
                  onClick={mesAnterior}
                  className="bg-sepulveda-blue hover:bg-sepulveda-blue-dark text-white px-3 py-1 rounded font-semibold transition-colors"
                >
                  ← Anterior
                </button>
                <h3 className="text-xl font-bold text-sepulveda-silver-light">
                  {MESES[fecha.getMonth()]} {fecha.getFullYear()}
                </h3>
                <button
                  onClick={mesSiguiente}
                  className="bg-sepulveda-blue hover:bg-sepulveda-blue-dark text-white px-3 py-1 rounded font-semibold transition-colors"
                >
                  Siguiente →
                </button>
              </div>

              {/* ✅ NUEVO: Leyenda de disponibilidad */}
              <div className="mb-4 p-3 bg-sepulveda-graphite/50 rounded-lg border border-sepulveda-silver/20">
                <p className="text-xs font-semibold text-sepulveda-silver mb-2">Ocupación por color:</p>
                <div className="grid grid-cols-2 md:grid-cols-5 gap-2 text-xs">
                  <div className="flex items-center gap-1">
                    <span className="w-2 h-2 rounded-full bg-green-400" />
                    <span className="text-sepulveda-silver">0% (Libre)</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <span className="w-2 h-2 rounded-full bg-emerald-400" />
                    <span className="text-sepulveda-silver">0-25%</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <span className="w-2 h-2 rounded-full bg-yellow-400" />
                    <span className="text-sepulveda-silver">25-50%</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <span className="w-2 h-2 rounded-full bg-orange-400" />
                    <span className="text-sepulveda-silver">50-75%</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <span className="w-2 h-2 rounded-full bg-red-400" />
                    <span className="text-sepulveda-silver">75%+ (Lleno)</span>
                  </div>
                </div>
              </div>

              {/* Cabecera días semana */}
              <div className="grid grid-cols-7 gap-1 mb-2">
                {DIAS_SEMANA.map(d => (
                  <div key={d} className="text-center font-bold text-sepulveda-silver py-2 text-sm">
                    {d}
                  </div>
                ))}
              </div>

              {/* Celdas */}
              <div className="grid grid-cols-7 gap-1">
                {semanas.map((semana, si) =>
                  semana.map((dia, di) => {
                    const diaNum   = dia.fecha.getDate();
                    const isoFecha = toISODate(dia.fecha);
                    const tieneCitas = dia.esDelMesActual && !!citasDelMes[diaNum];
                    const cantCitas  = tieneCitas ? citasDelMes[diaNum].length : 0;
                    const esCerrado  = dia.esDelMesActual && !!diasCerrados[isoFecha];
                    const esHoy =
                      hoy.getDate()        === diaNum &&
                      hoy.getMonth()       === fecha.getMonth() &&
                      hoy.getFullYear()    === fecha.getFullYear();
                    const esSelec = diaSeleccionado === diaNum && dia.esDelMesActual;

                    // ✅ NUEVO: Datos de disponibilidad
                    const disponibilidad = obtenerDisponibilidadDia(diaNum);
                    const colorDisponibilidad = obtenerColorPorDisponibilidad(diaNum);

                    let clases = 'p-2 rounded text-sm h-24 relative transition-all ';
                    if (!dia.esDelMesActual) {
                      clases += 'bg-sepulveda-carbon/50 text-sepulveda-silver/30 cursor-not-allowed border border-sepulveda-silver/10';
                    } else if (esSelec) {
                      clases += 'bg-sepulveda-blue text-white font-bold border-2 border-sepulveda-blue-light';
                    } else if (esCerrado) {
                      clases += 'bg-sepulveda-graphite hover:bg-sepulveda-graphite/80 border-2 border-red-500 text-sepulveda-silver';
                    } else if (esHoy) {
                      clases += 'bg-sepulveda-blue-light text-white font-bold border-2 border-sepulveda-blue';
                    } else if (tieneCitas || disponibilidad) {
                      clases += 'bg-sepulveda-blue/20 hover:bg-sepulveda-blue/30 border-2 border-sepulveda-blue text-sepulveda-silver-light';
                    } else {
                      clases += 'bg-sepulveda-graphite/60 hover:bg-sepulveda-graphite border border-sepulveda-silver/20 text-sepulveda-silver-light';
                    }

                    return (
                      <button
                        key={`${si}-${di}`}
                        onClick={() => dia.esDelMesActual && seleccionarDia(diaNum)}
                        disabled={!dia.esDelMesActual}
                        className={clases}
                        title={disponibilidad ? `Disponibles: ${disponibilidad.disponibles}/${disponibilidad.total}` : ''}
                      >
                        <div className="font-bold flex items-center justify-between mb-1">
                          <span>{diaNum}</span>
                          {esCerrado && <span title="Día cerrado">🔒</span>}
                        </div>

                        {esCerrado && (
                          <div className="text-xs mt-1 font-semibold text-red-400 truncate">
                            {diasCerrados[isoFecha]?.tipo || 'Cerrado'}
                          </div>
                        )}

                        {/* ✅ NUEVO: Mostrar disponibilidad */}
                        {!esCerrado && disponibilidad && (
                          <div className="text-xs space-y-0.5">
                            <div className={`font-bold ${colorDisponibilidad}`}>
                              {disponibilidad.disponibles}/{disponibilidad.total}
                            </div>
                            <div className="w-full h-1 bg-sepulveda-graphite rounded overflow-hidden">
                              <div
                                className={`h-full transition-all ${
                                  disponibilidad.porcentajeOcupacion === 0 ? 'bg-green-500' :
                                  disponibilidad.porcentajeOcupacion <= 25 ? 'bg-emerald-500' :
                                  disponibilidad.porcentajeOcupacion <= 50 ? 'bg-yellow-500' :
                                  disponibilidad.porcentajeOcupacion <= 75 ? 'bg-orange-500' :
                                  'bg-red-500'
                                }`}
                                style={{ width: `${disponibilidad.porcentajeOcupacion}%` }}
                              />
                            </div>
                            <div className="text-xs text-sepulveda-silver/70">
                              {disponibilidad.porcentajeOcupacion}% lleno
                            </div>
                          </div>
                        )}

                        {/* Mostrar cantidad de citas si no hay disponibilidad cargada */}
                        {!esCerrado && !disponibilidad && tieneCitas && (
                          <div className="text-xs mt-1 font-semibold text-sepulveda-blue-light">
                            {cantCitas} {cantCitas === 1 ? 'cita' : 'citas'}
                          </div>
                        )}
                      </button>
                    );
                  })
                )}
              </div>

              {/* Leyenda */}
              <div className="flex flex-wrap gap-4 mt-4 text-xs text-sepulveda-silver">
                <span className="flex items-center gap-1">
                  <span className="w-3 h-3 rounded bg-sepulveda-blue-light inline-block border border-sepulveda-blue" />
                  <span>Hoy</span>
                </span>
                <span className="flex items-center gap-1">
                  <span className="w-3 h-3 rounded bg-sepulveda-blue/20 border border-sepulveda-blue inline-block" />
                  <span>Con citas</span>
                </span>
                <span className="flex items-center gap-1">
                  <span className="w-3 h-3 rounded bg-sepulveda-graphite border border-red-500 inline-block" />
                  <span>🔒 Cerrado</span>
                </span>
                <span className="flex items-center gap-1">
                  <span className="w-3 h-3 rounded bg-sepulveda-blue inline-block" />
                  <span>Seleccionado</span>
                </span>
              </div>
            </div>
          </div>

          {/* ── Panel lateral ── */}
          <div className="lg:col-span-1 space-y-4">
            <div className="bg-sepulveda-carbon rounded-lg p-4 border border-sepulveda-silver/20">
              <h4 className="text-lg font-bold mb-4 text-sepulveda-silver-light">
                {diaSeleccionado
                  ? `${diaSeleccionado}/${fecha.getMonth() + 1}/${fecha.getFullYear()}`
                  : 'Citas del día'}
              </h4>

              {/* ✅ NUEVO: Panel de disponibilidad */}
              {diaSeleccionado && obtenerDisponibilidadDia(diaSeleccionado) && (
                <div className="mb-4 p-3 bg-sepulveda-graphite rounded-lg border border-sepulveda-silver/30">
                  <div className="font-bold text-sepulveda-silver-light mb-2">Disponibilidad</div>
                  <div className="space-y-2 text-sm text-sepulveda-silver">
                    <div className="flex justify-between">
                      <span>Disponibles:</span>
                      <span className="font-bold text-green-400">
                        {obtenerDisponibilidadDia(diaSeleccionado)?.disponibles || 0}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span>Agendadas:</span>
                      <span className="font-bold text-orange-400">
                        {obtenerDisponibilidadDia(diaSeleccionado)?.ocupadas || 0}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span>Total:</span>
                      <span className="font-bold text-sepulveda-silver-light">
                        {obtenerDisponibilidadDia(diaSeleccionado)?.total || 0}
                      </span>
                    </div>
                    <div className="w-full h-2 bg-sepulveda-carbon rounded overflow-hidden mt-2">
                      <div
                        className={`h-full ${
                          (obtenerDisponibilidadDia(diaSeleccionado)?.porcentajeOcupacion || 0) === 0 ? 'bg-green-500' :
                          (obtenerDisponibilidadDia(diaSeleccionado)?.porcentajeOcupacion || 0) <= 25 ? 'bg-emerald-500' :
                          (obtenerDisponibilidadDia(diaSeleccionado)?.porcentajeOcupacion || 0) <= 50 ? 'bg-yellow-500' :
                          (obtenerDisponibilidadDia(diaSeleccionado)?.porcentajeOcupacion || 0) <= 75 ? 'bg-orange-500' :
                          'bg-red-500'
                        }`}
                        style={{ width: `${obtenerDisponibilidadDia(diaSeleccionado)?.porcentajeOcupacion || 0}%` }}
                      />
                    </div>
                  </div>
                </div>
              )}

              {/* ── Bloque días cerrados ── */}
              {diaSeleccionado && (
                <div className="mb-4">
                  {infoCierre ? (
                    /* Día CERRADO */
                    <div className="bg-sepulveda-graphite border border-sepulveda-silver rounded-lg p-3">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="text-lg">🔒</span>
                        <span className="font-bold text-sepulveda-silver-light">Día cerrado</span>
                      </div>
                      <div className="text-sm text-sepulveda-silver">
                        <span className="font-semibold">Tipo: </span>{infoCierre.tipo}
                      </div>
                      {infoCierre.motivo && (
                        <div className="text-sm text-sepulveda-silver mt-1">
                          <span className="font-semibold">Motivo: </span>{infoCierre.motivo}
                        </div>
                      )}
                      <button
                        onClick={handleReabrirDia}
                        disabled={guardandoCierre}
                        className="mt-3 w-full bg-sepulveda-blue hover:bg-sepulveda-blue-dark disabled:opacity-50 text-white text-sm py-1.5 rounded font-semibold transition-colors"
                      >
                        {guardandoCierre ? 'Procesando...' : '✅ Reabrir día'}
                      </button>
                    </div>
                  ) : (
                    !mostrarFormCierre ? (
                      <button
                        onClick={() => setMostrarFormCierre(true)}
                        className="w-full bg-sepulveda-graphite hover:bg-sepulveda-graphite/80 border border-sepulveda-silver text-sepulveda-silver text-sm py-2 rounded-lg font-semibold flex items-center justify-center gap-2 transition-colors"
                      >
                        🔒 Marcar como día cerrado
                      </button>
                    ) : (
                      /* Formulario de cierre */
                      <div className="bg-sepulveda-graphite border border-sepulveda-silver/50 rounded-lg p-3 space-y-3">
                        <p className="text-sm font-bold text-sepulveda-silver-light">Marcar como cerrado</p>

                        <div>
                          <label className="text-xs font-semibold text-sepulveda-silver block mb-1">Tipo</label>
                          <select
                            value={formCierre.tipo}
                            onChange={e => setFormCierre(f => ({ ...f, tipo: e.target.value }))}
                            className="w-full bg-sepulveda-carbon border border-sepulveda-silver/40 text-sepulveda-silver-light rounded px-2 py-1.5 text-sm focus:outline-none focus:border-sepulveda-blue"
                          >
                            {TIPOS_CIERRE.map(t => (
                              <option key={t.value} value={t.value}>{t.label}</option>
                            ))}
                          </select>
                        </div>

                        <div>
                          <label className="text-xs font-semibold text-sepulveda-silver block mb-1">Motivo (opcional)</label>
                          <input
                            type="text"
                            placeholder="Ej: Día de la Constitución"
                            value={formCierre.motivo}
                            onChange={e => setFormCierre(f => ({ ...f, motivo: e.target.value }))}
                            className="w-full bg-sepulveda-carbon border border-sepulveda-silver/40 text-sepulveda-silver-light placeholder-sepulveda-silver/40 rounded px-2 py-1.5 text-sm focus:outline-none focus:border-sepulveda-blue"
                          />
                        </div>

                        <div className="flex gap-2">
                          <button
                            onClick={handleMarcarCerrado}
                            disabled={guardandoCierre}
                            className="flex-1 bg-sepulveda-blue hover:bg-sepulveda-blue-dark disabled:opacity-50 text-white text-sm py-1.5 rounded font-semibold transition-colors"
                          >
                            {guardandoCierre ? 'Guardando...' : '🔒 Confirmar cierre'}
                          </button>
                          <button
                            onClick={() => { setMostrarFormCierre(false); setFormCierre({ tipo: 'FESTIVO', motivo: '' }); }}
                            className="flex-1 bg-sepulveda-graphite/60 hover:bg-sepulveda-graphite border border-sepulveda-silver/30 text-sepulveda-silver text-sm py-1.5 rounded transition-colors"
                          >
                            Cancelar
                          </button>
                        </div>
                      </div>
                    )
                  )}
                </div>
              )}

              {/* ── Lista de citas ── */}
              {!diaSeleccionado ? (
                <p className="text-sepulveda-silver/60 text-center py-8">Selecciona un día para ver sus citas</p>
              ) : citasSeleccionadas.length === 0 ? (
                <p className="text-sepulveda-silver/60 text-center py-4">No hay citas para este día</p>
              ) : (
                <div className="space-y-3 max-h-96 overflow-y-auto">
                  {[...citasSeleccionadas]
                    .sort((a, b) => {
                      const ha = a.hora ? a.hora.substring(0, 5) : '00:00';
                      const hb = b.hora ? b.hora.substring(0, 5) : '00:00';
                      return ha.localeCompare(hb);
                    })
                    .map((cita, idx) => (
                      <div key={idx} className="bg-sepulveda-graphite rounded p-3 border-l-4 border-sepulveda-blue shadow-sm">
                        <div className="flex items-center justify-between mb-1">
                          <div className="font-bold text-sepulveda-blue-light">
                            {cita.hora ? cita.hora.substring(0, 5) : '—'}
                          </div>
                          <span className="text-xs font-mono text-sepulveda-silver/60 bg-sepulveda-carbon px-1.5 py-0.5 rounded">
                            #{cita.id}
                          </span>
                        </div>
                        <div className="text-sm font-semibold text-sepulveda-silver-light">{cita.nombre}</div>
                        <div className="text-xs text-sepulveda-silver">{cita.modeloVehiculo}</div>
                        <div className="text-xs text-sepulveda-silver/70 mt-1">{cita.tipoLavado}</div>
                        <div className="text-xs mt-2">
                          <span className={`px-2 py-1 rounded font-semibold ${
                            cita.estado === 'CONFIRMADA'  ? 'bg-green-900/60 text-green-300' :
                            cita.estado === 'CANCELADA'   ? 'bg-red-900/60 text-red-300' :
                            cita.estado === 'COMPLETADA'  ? 'bg-sepulveda-blue/30 text-sepulveda-blue-light' :
                            cita.estado === 'EN_PROCESO'  ? 'bg-orange-900/60 text-orange-300' :
                                                            'bg-yellow-900/60 text-yellow-300'
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