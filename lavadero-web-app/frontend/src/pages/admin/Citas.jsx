import { useState, useEffect } from 'react';
import citaService from '../../services/citaService';

const Citas = () => {
  const [citas, setCitas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingCita, setEditingCita] = useState(null);
  const [tiposLavado, setTiposLavado] = useState([]);

  // ⭐ NUEVO: Estados para disponibilidad
  const [horariosDisponibles, setHorariosDisponibles] = useState([]);
  const [loadingHorarios, setLoadingHorarios] = useState(false);
  const [validandoDisponibilidad, setValidandoDisponibilidad] = useState(false);

  // Estados del formulario
  const [formData, setFormData] = useState({
    nombre: '',
    telefono: '',
    email: '',
    fecha: '',
    hora: '',
    tipoLavado: '',
    modeloVehiculo: '',
    observaciones: '',
  });

  // Horarios base (todos los posibles)
  const todosLosHorarios = [
    '08:00', '09:00', '10:00', '11:00', '12:00', 
    '13:00', '14:00', '15:00', '16:00', '17:00', 
    '18:00', '19:00', '20:00'
  ];

  // Cargar citas al montar componente
  useEffect(() => {
    cargarCitas();
    cargarTiposLavado();
  }, []);

  // ⭐ NUEVO: Cargar horarios disponibles cuando cambia la fecha
  useEffect(() => {
    if (formData.fecha) {
      cargarHorariosDisponibles(formData.fecha);
    } else {
      // Si no hay fecha, mostrar todos los horarios
      setHorariosDisponibles(todosLosHorarios);
    }
  }, [formData.fecha]);

  // Cargar todas las citas
  const cargarCitas = async () => {
    setLoading(true);
    try {
      const response = await citaService.getAll();
      let citasData = response.data || [];

      // Si es un objeto con content (paginado), extraer content
      if (citasData && citasData.content && Array.isArray(citasData.content)) {
        citasData = citasData.content;
      }

      // Validar que sea un array
      if (!Array.isArray(citasData)) {
        console.warn('⚠️ response.data no es un array:', typeof citasData);
        citasData = [];
      }

      // Ordenar citas por fecha y hora
      citasData = citasData.sort((a, b) => {
        let fechaA = a.fecha;
        let fechaB = b.fecha;

        if (fechaA && fechaA.includes('/')) {
          const [d, m, y] = fechaA.split('/');
          fechaA = `${y}-${m}-${d}`;
        }
        if (fechaB && fechaB.includes('/')) {
          const [d, m, y] = fechaB.split('/');
          fechaB = `${y}-${m}-${d}`;
        }

        fechaA = fechaA ? fechaA.split('T')[0] : '0000-00-00';
        fechaB = fechaB ? fechaB.split('T')[0] : '0000-00-00';

        const comparacionFecha = fechaA.localeCompare(fechaB);
        if (comparacionFecha !== 0) {
          return comparacionFecha;
        }

        const horaA = a.hora ? a.hora.substring(0, 5) : '00:00';
        const horaB = b.hora ? b.hora.substring(0, 5) : '00:00';

        return horaA.localeCompare(horaB);
      });

      setCitas(citasData);
      setError(null);
    } catch (err) {
      setError('Error al cargar las citas: ' + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Cargar tipos de lavado
  const cargarTiposLavado = async () => {
    try {
      const response = await citaService.getTiposLavado();
      if (response?.data && Array.isArray(response.data)) {
        setTiposLavado(response.data);
      }
    } catch (err) {
      console.error('Error cargando tipos de lavado:', err);
    }
  };

  // ⭐ NUEVO: Cargar horarios disponibles para una fecha
  const cargarHorariosDisponibles = async (fecha) => {
    if (!fecha) {
      setHorariosDisponibles(todosLosHorarios);
      return;
    }

    setLoadingHorarios(true);
    try {
      const response = await citaService.getHorariosDisponibles(fecha);
      let horarios = response.data || [];

      console.log('📅 Horarios disponibles para', fecha, ':', horarios);

      // Si estamos editando, añadir la hora actual de la cita aunque esté ocupada
      if (editingCita && editingCita.hora) {
        const horaActual = editingCita.hora.substring(0, 5);
        if (!horarios.includes(horaActual)) {
          horarios.push(horaActual);
          horarios.sort();
          console.log('✅ Hora actual de la cita añadida:', horaActual);
        }
      }

      // Si no hay horarios disponibles, mostrar todos pero con advertencia
      if (horarios.length === 0) {
        console.warn('⚠️ No hay horarios disponibles para esta fecha');
        setHorariosDisponibles(todosLosHorarios);
      } else {
        setHorariosDisponibles(horarios);
      }
    } catch (err) {
      console.error('Error cargando horarios disponibles:', err);
      // En caso de error, mostrar todos los horarios
      setHorariosDisponibles(todosLosHorarios);
    } finally {
      setLoadingHorarios(false);
    }
  };

  // ⭐ NUEVO: Validar disponibilidad de un horario específico
  const validarDisponibilidad = async (fecha, hora) => {
    try {
      const response = await citaService.checkDisponibilidad(fecha, hora);
      // IMPORTANTE: El backend devuelve true si está OCUPADO, false si está DISPONIBLE
      const estaOcupado = response.data;
      
      console.log(`🔍 Validando ${fecha} ${hora}: ${estaOcupado ? 'OCUPADO' : 'DISPONIBLE'}`);
      
      return !estaOcupado; // Invertimos para que devuelva true si está disponible
    } catch (err) {
      console.error('Error validando disponibilidad:', err);
      // En caso de error, asumimos que está disponible para no bloquear
      return true;
    }
  };

  // Abrir modal para crear nueva cita
  const abrirModalNuevo = () => {
    setEditingCita(null);
    setFormData({
      nombre: '',
      telefono: '',
      email: '',
      fecha: '',
      hora: '',
      tipoLavado: '',
      modeloVehiculo: '',
      observaciones: '',
    });
    setHorariosDisponibles(todosLosHorarios);
    setShowModal(true);
  };

  // Abrir modal para editar cita
  const abrirModalEditar = (cita) => {
    setEditingCita(cita);

    console.log('📝 Editando cita:', cita);

    // Convertir fecha a formato YYYY-MM-DD
    let fechaFormato = '';
    if (cita.fecha) {
      if (typeof cita.fecha === 'string') {
        fechaFormato = cita.fecha.split('T')[0];
      } else if (cita.fecha instanceof Date) {
        const year = cita.fecha.getFullYear();
        const month = String(cita.fecha.getMonth() + 1).padStart(2, '0');
        const day = String(cita.fecha.getDate()).padStart(2, '0');
        fechaFormato = `${year}-${month}-${day}`;
      }
    }

    // Convertir hora a formato HH:00
    let horaFormato = '';
    if (cita.hora) {
      if (typeof cita.hora === 'string') {
        const horaParts = cita.hora.substring(0, 5).split(':');
        horaFormato = `${horaParts[0]}:00`;
      } else if (cita.hora instanceof Date) {
        const hours = String(cita.hora.getHours()).padStart(2, '0');
        horaFormato = `${hours}:00`;
      }
    }

    setFormData({
      nombre: cita.nombre || '',
      telefono: cita.telefono || '',
      email: cita.email || '',
      fecha: fechaFormato,
      hora: horaFormato,
      tipoLavado: cita.tipoLavado || '',
      modeloVehiculo: cita.modeloVehiculo || '',
      observaciones: cita.observaciones || '',
    });
    
    setShowModal(true);
    // Los horarios se cargarán automáticamente por el useEffect
  };

  // Cerrar modal
  const cerrarModal = () => {
    setShowModal(false);
    setEditingCita(null);
    setError(null);
  };

  // Manejar cambios en el formulario
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    
    // Si cambia la fecha, resetear la hora
    if (name === 'fecha') {
      setFormData(prev => ({
        ...prev,
        fecha: value,
        hora: '' // Resetear hora cuando cambia la fecha
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }));
    }
  };

  // ⭐ MEJORADO: Guardar cita con validación de disponibilidad
  const guardarCita = async (e) => {
    e.preventDefault();
    
    try {
      // Validar campos requeridos
      if (!formData.fecha || !formData.hora) {
        setError('Por favor, selecciona fecha y hora');
        return;
      }

      // ⭐ VALIDAR DISPONIBILIDAD antes de guardar
      setValidandoDisponibilidad(true);
      
      // Si estamos editando, solo validamos si cambió la fecha u hora
      let debeValidarDisponibilidad = true;
      if (editingCita) {
        const fechaOriginal = editingCita.fecha.split('T')[0];
        const horaOriginal = editingCita.hora.substring(0, 5);
        const horaFormateada = formData.hora;
        
        // Si no cambió fecha ni hora, no validar
        if (fechaOriginal === formData.fecha && horaOriginal === horaFormateada) {
          debeValidarDisponibilidad = false;
          console.log('✅ Fecha y hora no cambiaron, saltando validación');
        }
      }

      if (debeValidarDisponibilidad) {
        const disponible = await validarDisponibilidad(formData.fecha, formData.hora);
        
        if (!disponible) {
          setError(`❌ El horario ${formData.hora} del día ${formatearFecha(formData.fecha)} ya está ocupado. Por favor, selecciona otro horario.`);
          setValidandoDisponibilidad(false);
          return;
        }
        
        console.log('✅ Horario disponible, procediendo a guardar');
      }

      setValidandoDisponibilidad(false);

      // Preparar datos para enviar
      const datosAEnviar = {
        ...formData,
        hora: formData.hora
      };

      console.log('💾 Guardando cita:', datosAEnviar);

      if (editingCita) {
        await citaService.update(editingCita.id, datosAEnviar);
        console.log('✅ Cita actualizada');
      } else {
        await citaService.create(datosAEnviar);
        console.log('✅ Cita creada');
      }
      
      await cargarCitas();
      cerrarModal();
      setError(null);
    } catch (err) {
      setError('Error al guardar la cita: ' + err.message);
      console.error('❌ Error completo:', err);
      setValidandoDisponibilidad(false);
    }
  };

  // Función auxiliar para formatear fecha
  const formatearFecha = (fecha) => {
    if (!fecha) {
      return '—';
    }

    try {
      let day, month, year;

      if (typeof fecha === 'string') {
        if (fecha.includes('/')) {
          const partes = fecha.split('/');
          day = partes[0];
          month = partes[1];
          year = partes[2];
        } else if (fecha.includes('-')) {
          const fechaSolo = fecha.split('T')[0];
          const partes = fechaSolo.split('-');
          year = partes[0];
          month = partes[1];
          day = partes[2];
        } else {
          return fecha;
        }
      } else if (fecha instanceof Date) {
        day = String(fecha.getDate()).padStart(2, '0');
        month = String(fecha.getMonth() + 1).padStart(2, '0');
        year = fecha.getFullYear();
      } else {
        return '—';
      }

      return `${day}/${month}/${year}`;
    } catch (error) {
      console.error('Error formateando fecha:', error, fecha);
      return '—';
    }
  };

  // Eliminar cita
  const eliminarCita = async (id) => {
    if (window.confirm('¿Está seguro de que desea eliminar esta cita?')) {
      try {
        await citaService.delete(id);
        await cargarCitas();
        setError(null);
      } catch (err) {
        setError('Error al eliminar la cita: ' + err.message);
        console.error(err);
      }
    }
  };

  // Cambiar estado de cita
  const cambiarEstado = async (id, nuevoEstado) => {
    try {
      console.log(`Cambiando estado de cita ${id} a ${nuevoEstado}`);
      await citaService.cambiarEstado(id, nuevoEstado);
      await cargarCitas();
      setError(null);
    } catch (err) {
      setError('Error al cambiar el estado: ' + err.message);
      console.error(err);
    }
  };

  // Calcular citas pendientes de hoy
  const citasHoy = () => {
    const hoy = new Date();
    const hoyStr = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`;

    return citas.filter(cita => {
      let fechaCita = cita.fecha;

      if (fechaCita && fechaCita.includes('/')) {
        const [d, m, y] = fechaCita.split('/');
        fechaCita = `${y}-${m}-${d}`;
      }

      if (fechaCita) {
        fechaCita = fechaCita.split('T')[0];
      }

      return fechaCita === hoyStr && cita.estado !== 'CANCELADA';
    }).length;
  };

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between flex-wrap gap-4">
          <div className="flex items-center gap-3">
            <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <img src="/assets/icons/citas.png" alt="Citas" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Gestión de Citas</h1>
              <p className="text-gray-600">Citas pendientes hoy: <span className="font-semibold text-blue-600">{citasHoy()}</span> | Total: {citas.length} citas</p>
            </div>
          </div>
          <button
            onClick={abrirModalNuevo}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            + Nueva Cita
          </button>
        </div>
      </div>

      {/* Mensaje de error */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center">
          <span className="text-red-800">{error}</span>
        </div>
      )}

      {/* Tabla de citas */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        {loading ? (
          <div className="text-center py-8">
            <div className="flex items-center justify-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <span className="ml-3 text-gray-500">Cargando citas...</span>
            </div>
          </div>
        ) : citas.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            No hay citas registradas
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-100">
                <tr>
                  <th className="border border-gray-300 px-4 py-2 text-left">Cliente</th>
                  <th className="border border-gray-300 px-4 py-2 text-left">Teléfono</th>
                  <th className="border border-gray-300 px-4 py-2 text-left">Fecha</th>
                  <th className="border border-gray-300 px-4 py-2 text-left">Hora</th>
                  <th className="border border-gray-300 px-4 py-2 text-left">Tipo Lavado</th>
                  <th className="border border-gray-300 px-4 py-2 text-left">Modelo Vehículo</th>
                  <th className="border border-gray-300 px-4 py-2 text-left">Estado</th>
                  <th className="border border-gray-300 px-4 py-2 text-center">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {citas.map((cita) => (
                  <tr key={cita.id} className="hover:bg-gray-50">
                    <td className="border border-gray-300 px-4 py-2">{cita.nombre}</td>
                    <td className="border border-gray-300 px-4 py-2">{cita.telefono}</td>
                    <td className="border border-gray-300 px-4 py-2">
                      {formatearFecha(cita.fecha)}
                    </td>
                    <td className="border border-gray-300 px-4 py-2">{cita.hora?.substring(0, 5)}</td>
                    <td className="border border-gray-300 px-4 py-2">{cita.tipoLavado}</td>
                    <td className="border border-gray-300 px-4 py-2">{cita.modeloVehiculo}</td>
                    <td className="border border-gray-300 px-4 py-2">
                      <span className={`px-3 py-1 rounded text-sm font-medium ${cita.estado === 'CONFIRMADA' ? 'bg-green-100 text-green-800' :
                          cita.estado === 'CANCELADA' ? 'bg-red-100 text-red-800' :
                            cita.estado === 'COMPLETADA' ? 'bg-blue-100 text-blue-800' :
                              'bg-yellow-100 text-yellow-800'
                        }`}>
                        {cita.estado}
                      </span>
                    </td>
                    <td className="border border-gray-300 px-4 py-2 text-center">
                      <div className="flex items-center justify-center gap-2 flex-wrap">
                        <select
                          value={cita.estado}
                          onChange={(e) => cambiarEstado(cita.id, e.target.value)}
                          className={`px-2 py-1 rounded text-sm font-medium border cursor-pointer ${cita.estado === 'CONFIRMADA' ? 'bg-green-100 text-green-800 border-green-300' :
                              cita.estado === 'CANCELADA' ? 'bg-red-100 text-red-800 border-red-300' :
                                cita.estado === 'COMPLETADA' ? 'bg-blue-100 text-blue-800 border-blue-300' :
                                  'bg-yellow-100 text-yellow-800 border-yellow-300'
                            }`}
                        >
                          <option value="PENDIENTE">PENDIENTE</option>
                          <option value="CONFIRMADA">CONFIRMADA</option>
                          <option value="COMPLETADA">COMPLETADA</option>
                          <option value="CANCELADA">CANCELADA</option>
                        </select>
                        <button
                          onClick={() => abrirModalEditar(cita)}
                          className="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded"
                        >
                          Editar
                        </button>
                        <button
                          onClick={() => eliminarCita(cita.id)}
                          className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded"
                        >
                          Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Modal para crear/editar cita */}
        {showModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-8 max-w-md w-full max-h-[90vh] overflow-y-auto">
              <h3 className="text-xl font-bold mb-4">
                {editingCita ? 'Editar Cita' : 'Nueva Cita'}
              </h3>
              <form onSubmit={guardarCita} className="space-y-4">
                <input
                  type="text"
                  name="nombre"
                  placeholder="Nombre del cliente"
                  value={formData.nombre}
                  onChange={handleInputChange}
                  className="w-full border border-gray-300 rounded px-3 py-2"
                  required
                />
                <input
                  type="tel"
                  name="telefono"
                  placeholder="Teléfono"
                  value={formData.telefono}
                  onChange={handleInputChange}
                  className="w-full border border-gray-300 rounded px-3 py-2"
                  required
                />
                <input
                  type="email"
                  name="email"
                  placeholder="Email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className="w-full border border-gray-300 rounded px-3 py-2"
                  required
                />
                
                {/* ⭐ Input de fecha */}
                <div>
                  <input
                    type="date"
                    name="fecha"
                    value={formData.fecha}
                    onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded px-3 py-2"
                    required
                  />
                  <p className="text-xs text-gray-500 mt-1">
                    Selecciona primero la fecha para ver horarios disponibles
                  </p>
                </div>

                {/* ⭐ Select de hora con disponibilidad dinámica */}
                <div>
                  <select
                    name="hora"
                    value={formData.hora}
                    onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded px-3 py-2"
                    required
                    disabled={!formData.fecha || loadingHorarios}
                  >
                    <option value="">
                      {!formData.fecha 
                        ? 'Primero selecciona una fecha' 
                        : loadingHorarios 
                        ? 'Cargando horarios...' 
                        : 'Seleccionar hora'}
                    </option>
                    {horariosDisponibles.map(hora => (
                      <option key={hora} value={hora}>
                        {hora}
                      </option>
                    ))}
                  </select>
                  {formData.fecha && horariosDisponibles.length === 0 && !loadingHorarios && (
                    <p className="text-xs text-red-600 mt-1">
                      ⚠️ No hay horarios disponibles para esta fecha
                    </p>
                  )}
                  {formData.fecha && horariosDisponibles.length > 0 && (
                    <p className="text-xs text-green-600 mt-1">
                      ✅ {horariosDisponibles.length} horario(s) disponible(s)
                    </p>
                  )}
                </div>

                <select
                  name="tipoLavado"
                  value={formData.tipoLavado}
                  onChange={handleInputChange}
                  className="w-full border border-gray-300 rounded px-3 py-2"
                  required
                >
                  <option value="">Seleccionar tipo de lavado</option>
                  {tiposLavado.map(tipo => (
                    <option key={tipo.id} value={tipo.id}>
                      {tipo.descripcion} - €{tipo.precio.toFixed(2)}
                    </option>
                  ))}
                </select>
                <input
                  type="text"
                  name="modeloVehiculo"
                  placeholder="Modelo del vehículo"
                  value={formData.modeloVehiculo}
                  onChange={handleInputChange}
                  className="w-full border border-gray-300 rounded px-3 py-2"
                  required
                />
                <textarea
                  name="observaciones"
                  placeholder="Observaciones"
                  value={formData.observaciones}
                  onChange={handleInputChange}
                  className="w-full border border-gray-300 rounded px-3 py-2"
                  rows="3"
                />
                
                {/* ⭐ Botones con estado de validación */}
                <div className="flex gap-4">
                  <button
                    type="submit"
                    disabled={validandoDisponibilidad || loadingHorarios}
                    className={`flex-1 px-4 py-2 rounded text-white font-medium
                      ${validandoDisponibilidad || loadingHorarios
                        ? 'bg-gray-400 cursor-not-allowed' 
                        : 'bg-green-600 hover:bg-green-700'
                      }`}
                  >
                    {validandoDisponibilidad 
                      ? '🔍 Validando...' 
                      : editingCita 
                      ? 'Actualizar' 
                      : 'Crear'}
                  </button>
                  <button
                    type="button"
                    onClick={cerrarModal}
                    disabled={validandoDisponibilidad}
                    className="flex-1 bg-gray-400 hover:bg-gray-500 text-white px-4 py-2 rounded"
                  >
                    Cancelar
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Citas;