import { useState, useEffect } from 'react';
import citaService from '../../services/citaService';

const Citas = () => {
  const [citas, setCitas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingCita, setEditingCita] = useState(null);
  const [tiposLavado, setTiposLavado] = useState([]);

  // Estados para paginación
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  // Estados para validación de disponibilidad
  const [horariosDisponibles, setHorariosDisponibles] = useState([]);
  const [todosLosHorarios, setTodosLosHorarios] = useState([]);
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

  // Cargar configuración inicial
  useEffect(() => {
    cargarCitas();
    cargarTiposLavado();
    cargarHorariosConfigurados();
  }, [currentPage, pageSize]);

  useEffect(() => {
    if (formData.fecha) {
      cargarHorariosDisponibles(formData.fecha);
    } else {
      setHorariosDisponibles(todosLosHorarios);
    }
  }, [formData.fecha]);

  // Cargar horarios configurados del backend
  const cargarHorariosConfigurados = async () => {
    try {
      const response = await citaService.getHorariosConfigurados();
      if (response?.data && Array.isArray(response.data)) {
        setTodosLosHorarios(response.data);
        setHorariosDisponibles(response.data);
        console.log('Horarios configurados cargados:', response.data);
      }
    } catch (err) {
      console.error('Error cargando horarios configurados:', err);
      // Fallback a horarios por defecto si falla
      const horariosFallback = [
        '08:00', '09:00', '10:00', '11:00', '12:00', 
        '13:00', '14:00', '15:00', '16:00', '17:00', 
        '18:00', '19:00', '20:00'
      ];
      setTodosLosHorarios(horariosFallback);
      setHorariosDisponibles(horariosFallback);
    }
  };

  const cargarCitas = async () => {
    setLoading(true);
    try {
      // Orden descendente por defecto (fecha más reciente primero)
      const response = await citaService.getAllPaginado(currentPage, pageSize, 'fecha', 'desc');
      const data = response.data;

      if (data.content) {
        setCitas(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      } else {
        setCitas(data || []);
      }
      
      setError(null);
    } catch (err) {
      setError('Error al cargar las citas: ' + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

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

  const cargarHorariosDisponibles = async (fecha) => {
    if (!fecha) {
      setHorariosDisponibles(todosLosHorarios);
      return;
    }

    setLoadingHorarios(true);
    try {
      const response = await citaService.getHorariosDisponibles(fecha);
      let horarios = response.data || [];

      if (editingCita && editingCita.hora) {
        const horaActual = editingCita.hora.substring(0, 5);
        if (!horarios.includes(horaActual)) {
          horarios.push(horaActual);
          horarios.sort();
        }
      }

      if (horarios.length === 0) {
        setHorariosDisponibles(todosLosHorarios);
      } else {
        setHorariosDisponibles(horarios);
      }
    } catch (err) {
      console.error('Error cargando horarios disponibles:', err);
      setHorariosDisponibles(todosLosHorarios);
    } finally {
      setLoadingHorarios(false);
    }
  };

  const validarDisponibilidad = async (fecha, hora) => {
    try {
      const response = await citaService.checkDisponibilidad(fecha, hora);
      const estaOcupado = response.data;
      return !estaOcupado;
    } catch (err) {
      console.error('Error validando disponibilidad:', err);
      return true;
    }
  };

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

  const abrirModalEditar = (cita) => {
    setEditingCita(cita);

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
  };

  const cerrarModal = () => {
    setShowModal(false);
    setEditingCita(null);
    setError(null);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    
    if (name === 'fecha') {
      setFormData(prev => ({
        ...prev,
        fecha: value,
        hora: ''
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }));
    }
  };

  const guardarCita = async (e) => {
    e.preventDefault();
    
    try {
      if (!formData.fecha || !formData.hora) {
        setError('Por favor, selecciona fecha y hora');
        return;
      }

      setValidandoDisponibilidad(true);
      
      let debeValidarDisponibilidad = true;
      if (editingCita) {
        const fechaOriginal = editingCita.fecha.split('T')[0];
        const horaOriginal = editingCita.hora.substring(0, 5);
        const horaFormateada = formData.hora;
        
        if (fechaOriginal === formData.fecha && horaOriginal === horaFormateada) {
          debeValidarDisponibilidad = false;
        }
      }

      if (debeValidarDisponibilidad) {
        const disponible = await validarDisponibilidad(formData.fecha, formData.hora);
        
        if (!disponible) {
          setError(`El horario ${formData.hora} del día ${formatearFecha(formData.fecha)} ya está ocupado.`);
          setValidandoDisponibilidad(false);
          return;
        }
      }

      setValidandoDisponibilidad(false);

      const datosAEnviar = {
        ...formData,
        hora: formData.hora
      };

      if (editingCita) {
        await citaService.update(editingCita.id, datosAEnviar);
      } else {
        await citaService.create(datosAEnviar);
      }
      
      await cargarCitas();
      cerrarModal();
      setError(null);
    } catch (err) {
      setError('Error al guardar la cita: ' + err.message);
      console.error(err);
      setValidandoDisponibilidad(false);
    }
  };

  const formatearFecha = (fecha) => {
    if (!fecha) return '—';

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

  const cambiarEstado = async (id, nuevoEstado) => {
    try {
      await citaService.cambiarEstado(id, nuevoEstado);
      await cargarCitas();
      setError(null);
    } catch (err) {
      setError('Error al cambiar el estado: ' + err.message);
      console.error(err);
    }
  };

  const esCitaDeHoy = (fecha) => {
    const hoy = new Date();
    const hoyStr = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`;

    let fechaCita = fecha;

    if (fechaCita && fechaCita.includes('/')) {
      const [d, m, y] = fechaCita.split('/');
      fechaCita = `${y}-${m}-${d}`;
    }

    if (fechaCita) {
      fechaCita = fechaCita.split('T')[0];
    }

    return fechaCita === hoyStr;
  };

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

  const irAPagina = (pagina) => {
    if (pagina >= 0 && pagina < totalPages) {
      setCurrentPage(pagina);
    }
  };

  const cambiarTamanoPagina = (nuevoTamano) => {
    setPageSize(nuevoTamano);
    setCurrentPage(0);
  };

  const getPaginaInicio = () => currentPage * pageSize + 1;
  const getPaginaFin = () => Math.min((currentPage + 1) * pageSize, totalElements);

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
              <p className="text-base text-gray-600">Citas pendientes hoy: <span className="font-semibold text-blue-600">{citasHoy()}</span> | Total: {totalElements} citas</p>
            </div>
          </div>
          <button
            onClick={abrirModalNuevo}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-base font-medium"
          >
            + Nueva Cita
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center">
          <span className="text-red-800 text-base">{error}</span>
        </div>
      )}

      <div className="bg-white rounded-lg shadow overflow-hidden">
        {loading ? (
          <div className="text-center py-8">
            <div className="flex items-center justify-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <span className="ml-3 text-gray-500 text-base">Cargando citas...</span>
            </div>
          </div>
        ) : citas.length === 0 ? (
          <div className="text-center py-8 text-gray-500 text-base">
            No hay citas registradas
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-100">
                  <tr>
                    <th className="border border-gray-300 px-4 py-3 text-left text-sm font-semibold">Cliente</th>
                    <th className="border border-gray-300 px-4 py-3 text-left text-sm font-semibold">Teléfono</th>
                    <th className="border border-gray-300 px-4 py-3 text-left text-sm font-semibold">Fecha</th>
                    <th className="border border-gray-300 px-4 py-3 text-left text-sm font-semibold">Hora</th>
                    <th className="border border-gray-300 px-4 py-3 text-left text-sm font-semibold">Tipo Lavado</th>
                    <th className="border border-gray-300 px-4 py-3 text-left text-sm font-semibold">Modelo Vehículo</th>
                    <th className="border border-gray-300 px-4 py-3 text-left text-sm font-semibold">Estado</th>
                    <th className="border border-gray-300 px-4 py-3 text-center text-sm font-semibold">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {citas.map((cita) => (
                    <tr key={cita.id} className={`${esCitaDeHoy(cita.fecha) ? 'bg-blue-100 hover:bg-blue-200' : 'hover:bg-gray-50'}`}>
                      <td className="border border-gray-300 px-4 py-3 text-base">{cita.nombre}</td>
                      <td className="border border-gray-300 px-4 py-3 text-base">{cita.telefono}</td>
                      <td className="border border-gray-300 px-4 py-3 text-base">
                        {formatearFecha(cita.fecha)}
                      </td>
                      <td className="border border-gray-300 px-4 py-3 text-base">{cita.hora?.substring(0, 5)}</td>
                      <td className="border border-gray-300 px-4 py-3 text-base">{cita.tipoLavado}</td>
                      <td className="border border-gray-300 px-4 py-3 text-base">{cita.modeloVehiculo}</td>
                      <td className="border border-gray-300 px-4 py-3 text-base">
                        <span className={`px-3 py-1 rounded text-sm font-medium ${cita.estado === 'CONFIRMADA' ? 'bg-green-100 text-green-800' :
                            cita.estado === 'CANCELADA' ? 'bg-red-100 text-red-800' :
                              cita.estado === 'COMPLETADA' ? 'bg-blue-100 text-blue-800' :
                                'bg-yellow-100 text-yellow-800'
                          }`}>
                          {cita.estado}
                        </span>
                      </td>
                      <td className="border border-gray-300 px-4 py-3 text-center text-base">
                        <div className="flex items-center justify-center gap-2 flex-wrap">
                          <select
                            value={cita.estado}
                            onChange={(e) => cambiarEstado(cita.id, e.target.value)}
                            className={`px-3 py-2 rounded text-base font-medium border cursor-pointer ${cita.estado === 'CONFIRMADA' ? 'bg-green-100 text-green-800 border-green-300' :
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
                            className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded text-base font-medium"
                          >
                            Editar
                          </button>
                          <button
                            onClick={() => eliminarCita(cita.id)}
                            className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded text-base font-medium"
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

            <div className="bg-gray-50 px-4 py-3 border-t border-gray-200 sm:px-6">
              <div className="flex items-center justify-between flex-wrap gap-4">
                <div className="flex items-center gap-4">
                  <p className="text-base text-gray-700">
                    Mostrando <span className="font-medium">{getPaginaInicio()}</span> a <span className="font-medium">{getPaginaFin()}</span> de{' '}
                    <span className="font-medium">{totalElements}</span> resultados
                  </p>

                  <div className="flex items-center gap-2">
                    <label htmlFor="pageSize" className="text-base text-gray-700">
                      Por página:
                    </label>
                    <select
                      id="pageSize"
                      value={pageSize}
                      onChange={(e) => cambiarTamanoPagina(Number(e.target.value))}
                      className="border border-gray-300 rounded px-3 py-2 text-base"
                    >
                      <option value={5}>5</option>
                      <option value={10}>10</option>
                      <option value={20}>20</option>
                      <option value={50}>50</option>
                    </select>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => irAPagina(0)}
                    disabled={currentPage === 0}
                    className={`px-4 py-2 rounded text-base ${currentPage === 0
                        ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                        : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
                      }`}
                  >
                    Primera
                  </button>
                  <button
                    onClick={() => irAPagina(currentPage - 1)}
                    disabled={currentPage === 0}
                    className={`px-4 py-2 rounded text-base ${currentPage === 0
                        ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                        : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
                      }`}
                  >
                    Anterior
                  </button>

                  <div className="flex gap-1">
                    {[...Array(totalPages)].map((_, index) => {
                      if (
                        index === 0 ||
                        index === totalPages - 1 ||
                        (index >= currentPage - 2 && index <= currentPage + 2)
                      ) {
                        return (
                          <button
                            key={index}
                            onClick={() => irAPagina(index)}
                            className={`px-3 py-2 rounded text-base ${currentPage === index
                                ? 'bg-blue-600 text-white'
                                : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
                              }`}
                          >
                            {index + 1}
                          </button>
                        );
                      } else if (index === currentPage - 3 || index === currentPage + 3) {
                        return <span key={index} className="px-2">...</span>;
                      }
                      return null;
                    })}
                  </div>

                  <button
                    onClick={() => irAPagina(currentPage + 1)}
                    disabled={currentPage === totalPages - 1}
                    className={`px-4 py-2 rounded text-base ${currentPage === totalPages - 1
                        ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                        : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
                      }`}
                  >
                    Siguiente
                  </button>
                  <button
                    onClick={() => irAPagina(totalPages - 1)}
                    disabled={currentPage === totalPages - 1}
                    className={`px-4 py-2 rounded text-base ${currentPage === totalPages - 1
                        ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                        : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
                      }`}
                  >
                    Última
                  </button>
                </div>
              </div>
            </div>
          </>
        )}

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
                  placeholder="Email (opcional)"
                  value={formData.email}
                  onChange={handleInputChange}
                  className="w-full border border-gray-300 rounded px-3 py-2"
                />
                
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