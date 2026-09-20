import { useState, useEffect } from 'react';
import citaService from '../../services/citaService';

const Citas = () => {
  const [citas, setCitas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingCita, setEditingCita] = useState(null);
  const [tiposLavado, setTiposLavado] = useState([]);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  const [horariosDisponibles, setHorariosDisponibles] = useState([]);
  const [horariosPortDia, setHorariosPortDia] = useState({});
  const [loadingHorarios, setLoadingHorarios] = useState(false);
  const [validandoDisponibilidad, setValidandoDisponibilidad] = useState(false);

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

  useEffect(() => {
    cargarCitas();
    cargarTiposLavado();
    cargarHorariosConfiguracion();
  }, [currentPage, pageSize]);

  // Cuando cambia la fecha, recalcular horarios disponibles
  useEffect(() => {
    if (formData.fecha) {
      cargarHorariosDisponibles(formData.fecha);
    } else {
      setHorariosDisponibles([]);
    }
  }, [formData.fecha, horariosPortDia]);

  /**
   * Carga la configuración de horarios de la BD (todos los días)
   */
  const cargarHorariosConfiguracion = async () => {
    try {
      const response = await citaService.getHorariosDiaSemana();
      if (response?.data && Array.isArray(response.data)) {
        // Convertir lista de HorarioDiaSemana a un mapa por día
        const mapa = {};
        response.data.forEach(horario => {
          mapa[horario.diaSemana] = horario;
        });
        setHorariosPortDia(mapa);
      }
    } catch (err) {
      console.error('Error cargando horarios por día:', err);
      setHorariosPortDia({});
    }
  };

  const cargarCitas = async () => {
    setLoading(true);
    try {
      const response = await citaService.getAllPaginado(currentPage, pageSize, 'fecha', 'desc');
      const data = response.data;
      if (data.content) {
        const citasOrdenadas = data.content.sort((a, b) => {
          const fechaA = new Date(a.fecha);
          const fechaB = new Date(b.fecha);
          if (fechaA.getTime() !== fechaB.getTime()) return fechaB.getTime() - fechaA.getTime();
          const horaA = a.hora ? a.hora.split(':') : ['00', '00'];
          const horaB = b.hora ? b.hora.split(':') : ['00', '00'];
          return (parseInt(horaA[0]) * 60 + parseInt(horaA[1])) - (parseInt(horaB[0]) * 60 + parseInt(horaB[1]));
        });
        setCitas(citasOrdenadas);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      } else {
        setCitas(data || []);
      }
      setError(null);
    } catch (err) {
      setError('Error al cargar las citas: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const cargarTiposLavado = async () => {
    try {
      const response = await citaService.getTiposLavado();
      if (response?.data && Array.isArray(response.data)) setTiposLavado(response.data);
    } catch (err) {
      console.error('Error cargando tipos de lavado:', err);
    }
  };

  /**
   * Calcula los horarios disponibles para una fecha específica.
   * Usa los horarios de HorarioDiaSemana de la BD.
   */
  const cargarHorariosDisponibles = async (fecha) => {
    if (!fecha) {
      setHorariosDisponibles([]);
      return;
    }

    setLoadingHorarios(true);
    try {
      // Convertir fecha de YYYY-MM-DD a DD/MM/YYYY
      const [year, month, day] = fecha.split('-');
      const fechaFormato = `${day}/${month}/${year}`;

      console.log("📋 Pidiendo horarios para:", fechaFormato);

      // Llamar directamente al backend
      const response = await citaService.getHorariosDisponibles(fechaFormato);

      console.log("📋 Response del backend:", response);

      // Axios devuelve directamente el array o en response.data
      let horariosFinales = Array.isArray(response) ? response : (response?.data || []);

      console.log("✅ Horarios finales:", horariosFinales);

      // Asegurar formato correcto (HH:mm) con cero inicial
      if (Array.isArray(horariosFinales)) {
        horariosFinales = horariosFinales.map(h => {
          const str = String(h).substring(0, 5);
          if (str.length === 4 && str[0] !== '0') {
            return '0' + str;
          }
          return str;
        });
      }

      // Si estamos editando, agregar la hora actual aunque no esté disponible
      if (editingCita?.hora) {
        const horaActual = editingCita.hora.substring(0, 5);
        if (!horariosFinales.includes(horaActual)) {
          horariosFinales.push(horaActual);
          horariosFinales.sort();
        }
      }

      setHorariosDisponibles(horariosFinales);
    } catch (err) {
      console.error('❌ Error cargando horarios disponibles:', err);
      setHorariosDisponibles([]);
    } finally {
      setLoadingHorarios(false);
    }
  };

  const validarDisponibilidad = async (fecha, hora) => {
    setValidandoDisponibilidad(true);
    try {
      const response = await citaService.checkDisponibilidad(fecha, hora);
      return !response?.data;  // ← Cambio aquí
    } catch (err) {
      console.error('Error validando disponibilidad:', err);
      return false;
    } finally {
      setValidandoDisponibilidad(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const guardarCita = async (e) => {
    e.preventDefault();
    const disponible = await validarDisponibilidad(formData.fecha, formData.hora);
    if (!disponible) {
      setError('El horario no está disponible. Por favor, selecciona otro.');
      return;
    }

    try {
      if (editingCita) {
        await citaService.update(editingCita.id, formData);
      } else {
        await citaService.create(formData);
      }
      cargarCitas();
      cerrarModal();
      setFormData({ nombre: '', telefono: '', email: '', fecha: '', hora: '', tipoLavado: '', modeloVehiculo: '', observaciones: '' });
    } catch (err) {
      setError('Error al guardar la cita: ' + err.message);
    }
  };

  const abrirModalEditar = (cita) => {
    setEditingCita(cita);
    setFormData(cita);
    setShowModal(true);
  };

  const cerrarModal = () => {
    setShowModal(false);
    setEditingCita(null);
    setFormData({ nombre: '', telefono: '', email: '', fecha: '', hora: '', tipoLavado: '', modeloVehiculo: '', observaciones: '' });
  };

  const eliminarCita = async (id) => {
    if (!window.confirm('¿Estás seguro de que deseas eliminar esta cita?')) return;
    try {
      await citaService.delete(id);
      cargarCitas();
    } catch (err) {
      setError('Error al eliminar la cita: ' + err.message);
    }
  };

  const cambiarEstado = async (id, nuevoEstado) => {
    try {
      await citaService.cambiarEstado(id, nuevoEstado);
      cargarCitas();
    } catch (err) {
      setError('Error al cambiar el estado: ' + err.message);
    }
  };

  const getPaginaInicio = () => currentPage * pageSize + 1;
  const getPaginaFin = () => Math.min((currentPage + 1) * pageSize, totalElements);
  const irAPagina = (pagina) => setCurrentPage(pagina);
  const cambiarTamanoPagina = (size) => {
    setPageSize(size);
    setCurrentPage(0);
  };

  return (
    <div className="p-6 bg-white rounded-lg shadow-lg">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Gestión de Citas</h1>
        <button onClick={() => { setShowModal(true); setEditingCita(null); }} className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg font-medium">
          + Nueva Cita
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-4 text-red-800">
          {error}
          <button onClick={() => setError(null)} className="float-right text-red-600 hover:text-red-900">✕</button>
        </div>
      )}

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
        </div>
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className="w-full border-collapse border border-gray-300">
              <thead className="bg-gray-100">
                <tr>
                  <th className="border border-gray-300 px-4 py-2 text-left">Fecha</th>
                  <th className="border border-gray-300 px-4 py-2 text-left">Hora</th>
                  <th className="border border-gray-300 px-4 py-2 text-left">Cliente</th>
                  <th className="border border-gray-300 px-4 py-2 text-left">Teléfono</th>
                  <th className="border border-gray-300 px-4 py-2 text-left">Vehículo</th>
                  <th className="border border-gray-300 px-4 py-2 text-left">Estado</th>
                  <th className="border border-gray-300 px-4 py-2 text-center">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {citas.map((cita, idx) => (
                  <tr key={cita.id} className={idx % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                    <td className="border border-gray-300 px-4 py-2">{cita.fecha}</td>
                    <td className="border border-gray-300 px-4 py-2">{cita.hora}</td>
                    <td className="border border-gray-300 px-4 py-2">{cita.nombre}</td>
                    <td className="border border-gray-300 px-4 py-2">{cita.telefono}</td>
                    <td className="border border-gray-300 px-4 py-2">{cita.modeloVehiculo}</td>
                    <td className="border border-gray-300 px-4 py-2">
                      <select
                        value={cita.estado}
                        onChange={(e) => cambiarEstado(cita.id, e.target.value)}
                        className="border border-gray-300 rounded px-2 py-1"
                      >
                        <option value="PENDIENTE">PENDIENTE</option>
                        <option value="CONFIRMADA">CONFIRMADA</option>
                        <option value="COMPLETADA">COMPLETADA</option>
                        <option value="CANCELADA">CANCELADA</option>
                      </select>
                    </td>
                    <td className="border border-gray-300 px-4 py-2 text-center">
                      <button onClick={() => abrirModalEditar(cita)} className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded text-sm mr-2">Editar</button>
                      <button onClick={() => eliminarCita(cita.id)} className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded text-sm">Eliminar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="mt-4 flex justify-between items-center">
            <p className="text-gray-700">Mostrando {getPaginaInicio()} a {getPaginaFin()} de {totalElements}</p>
            <div className="flex gap-2">
              <button onClick={() => irAPagina(0)} disabled={currentPage === 0} className="px-4 py-2 border rounded disabled:opacity-50">Primera</button>
              <button onClick={() => irAPagina(currentPage - 1)} disabled={currentPage === 0} className="px-4 py-2 border rounded disabled:opacity-50">Anterior</button>
              <button onClick={() => irAPagina(currentPage + 1)} disabled={currentPage === totalPages - 1} className="px-4 py-2 border rounded disabled:opacity-50">Siguiente</button>
              <button onClick={() => irAPagina(totalPages - 1)} disabled={currentPage === totalPages - 1} className="px-4 py-2 border rounded disabled:opacity-50">Última</button>
            </div>
          </div>
        </>
      )}

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-lg w-full max-w-2xl p-6">
            <h2 className="text-2xl font-bold mb-4">{editingCita ? 'Editar Cita' : 'Nueva Cita'}</h2>
            <form onSubmit={guardarCita} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-semibold mb-1">Nombre *</label>
                  <input type="text" name="nombre" value={formData.nombre} onChange={handleInputChange} className="w-full border rounded px-3 py-2" required />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Teléfono *</label>
                  <input type="tel" name="telefono" value={formData.telefono} onChange={handleInputChange} className="w-full border rounded px-3 py-2" required />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Email</label>
                  <input type="email" name="email" value={formData.email} onChange={handleInputChange} className="w-full border rounded px-3 py-2" />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Modelo Vehículo *</label>
                  <input type="text" name="modeloVehiculo" value={formData.modeloVehiculo} onChange={handleInputChange} className="w-full border rounded px-3 py-2" required />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Fecha *</label>
                  <input type="date" name="fecha" value={formData.fecha} onChange={handleInputChange} className="w-full border rounded px-3 py-2" required />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Hora *</label>
                  <select name="hora" value={formData.hora} onChange={handleInputChange} className="w-full border rounded px-3 py-2" required disabled={!formData.fecha || loadingHorarios}>
                    <option value="">{loadingHorarios ? 'Cargando...' : 'Seleccionar hora'}</option>
                    {horariosDisponibles.map(hora => <option key={hora} value={hora}>{hora}</option>)}
                  </select>
                </div>
                <div className="col-span-2">
                  <label className="block text-sm font-semibold mb-1">Tipo Lavado *</label>
                  <select name="tipoLavado" value={formData.tipoLavado} onChange={handleInputChange} className="w-full border rounded px-3 py-2" required>
                    <option value="">Seleccionar tipo</option>
                    {tiposLavado.map(tipo => <option key={tipo.id} value={tipo.id}>{tipo.descripcion}</option>)}
                  </select>
                </div>
                <div className="col-span-2">
                  <label className="block text-sm font-semibold mb-1">Observaciones</label>
                  <textarea name="observaciones" value={formData.observaciones} onChange={handleInputChange} className="w-full border rounded px-3 py-2" rows="3" />
                </div>
              </div>
              <div className="flex justify-end gap-3">
                <button type="button" onClick={cerrarModal} className="px-6 py-2 border rounded hover:bg-gray-100">Cancelar</button>
                <button type="submit" disabled={validandoDisponibilidad} className="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50">
                  {validandoDisponibilidad ? 'Validando...' : editingCita ? 'Actualizar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Citas;