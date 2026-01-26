import { useState, useEffect } from 'react';
import citaService from '../../services/citaService';

const Citas = () => {
  const [citas, setCitas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingSubmit, setLoadingSubmit] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingCita, setEditingCita] = useState(null);
  const [tiposLavado, setTiposLavado] = useState([]);
  
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

  // Validar email
  const validarEmail = (email) => {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
  };

  // Validar teléfono (al menos 9 dígitos)
  const validarTelefono = (telefono) => {
    const soloNumeros = telefono.replace(/\D/g, '');
    return soloNumeros.length >= 9;
  };

  // Convertir fecha a formato YYYY-MM-DD
  const convertirFechaAISO = (fecha) => {
    if (!fecha) return '';
    if (typeof fecha === 'string') {
      return fecha.split('T')[0];
    }
    if (fecha instanceof Date && !isNaN(fecha.getTime())) {
      const year = fecha.getFullYear();
      const month = String(fecha.getMonth() + 1).padStart(2, '0');
      const day = String(fecha.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    }
    return '';
  };

  // Convertir hora a formato HH:mm
  const convertirHora = (hora) => {
    if (!hora) return '';
    if (typeof hora === 'string') {
      return hora.substring(0, 5);
    }
    if (hora instanceof Date && !isNaN(hora.getTime())) {
      const hours = String(hora.getHours()).padStart(2, '0');
      const minutes = String(hora.getMinutes()).padStart(2, '0');
      return `${hours}:${minutes}`;
    }
    return '';
  };

  // Cargar citas al montar componente
  useEffect(() => {
    cargarCitas();
    cargarTiposLavado();
  }, []);

  // Cargar todas las citas
  const cargarCitas = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await citaService.getAll();
      let citasData = response.data || [];
      
      // Ordenar citas por fecha y hora
      citasData = citasData.sort((a, b) => {
        const fechaA = convertirFechaAISO(a.fecha) || '0000-00-00';
        const fechaB = convertirFechaAISO(b.fecha) || '0000-00-00';
        
        const comparacionFecha = fechaA.localeCompare(fechaB);
        if (comparacionFecha !== 0) {
          return comparacionFecha;
        }
        
        const horaA = convertirHora(a.hora) || '00:00';
        const horaB = convertirHora(b.hora) || '00:00';
        
        return horaA.localeCompare(horaB);
      });
      
      setCitas(citasData);
    } catch (err) {
      console.error('Error cargando citas:', err);
      setError('Error al cargar las citas: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Cargar tipos de lavado
  const cargarTiposLavado = async () => {
    try {
      const response = await citaService.getTiposLavado?.();
      if (response?.data) {
        setTiposLavado(response.data);
      }
    } catch (err) {
      console.error('Error cargando tipos de lavado:', err);
    }
  };

  // Mostrar mensaje de éxito temporal
  const mostrarExito = (mensaje) => {
    setSuccess(mensaje);
    setTimeout(() => setSuccess(null), 3000);
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
    setError(null);
    setShowModal(true);
  };

  // Abrir modal para editar cita
  const abrirModalEditar = (cita) => {
    setEditingCita(cita);
    setFormData({
      nombre: cita.nombre || '',
      telefono: cita.telefono || '',
      email: cita.email || '',
      fecha: convertirFechaAISO(cita.fecha),
      hora: convertirHora(cita.hora),
      tipoLavado: cita.tipoLavado || '',
      modeloVehiculo: cita.modeloVehiculo || '',
      observaciones: cita.observaciones || '',
    });
    setError(null);
    setShowModal(true);
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
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Validar formulario
  const validarFormulario = () => {
    if (!formData.nombre.trim()) {
      setError('El nombre es requerido');
      return false;
    }
    if (!validarTelefono(formData.telefono)) {
      setError('Teléfono inválido (mínimo 9 dígitos)');
      return false;
    }
    if (!validarEmail(formData.email)) {
      setError('Email inválido');
      return false;
    }
    if (!formData.fecha) {
      setError('La fecha es requerida');
      return false;
    }
    if (!formData.hora) {
      setError('La hora es requerida');
      return false;
    }
    if (!formData.tipoLavado) {
      setError('Debe seleccionar un tipo de lavado');
      return false;
    }
    if (!formData.modeloVehiculo.trim()) {
      setError('El modelo del vehículo es requerido');
      return false;
    }
    return true;
  };

  // Guardar cita (crear o actualizar)
  const guardarCita = async (e) => {
    e.preventDefault();
    
    if (!validarFormulario()) {
      return;
    }

    setLoadingSubmit(true);
    try {
      if (editingCita) {
        await citaService.update(editingCita.id, formData);
        mostrarExito('Cita actualizada correctamente');
      } else {
        await citaService.create(formData);
        mostrarExito('Cita creada correctamente');
      }
      await cargarCitas();
      cerrarModal();
    } catch (err) {
      console.error('Error guardando cita:', err);
      setError('Error al guardar la cita: ' + err.message);
    } finally {
      setLoadingSubmit(false);
    }
  };

  // Función auxiliar para formatear fecha para mostrar
  const formatearFecha = (fecha) => {
    if (!fecha) {
      return '—';
    }
    
    try {
      const fechaISO = convertirFechaAISO(fecha);
      if (!fechaISO) {
        return '—';
      }
      
      const [year, month, day] = fechaISO.split('-');
      const y = parseInt(year);
      const m = parseInt(month);
      const d = parseInt(day);
      
      if (m < 1 || m > 12 || d < 1 || d > 31) {
        console.warn('Fecha fuera de rango:', fecha);
        return '—';
      }
      
      return `${String(d).padStart(2, '0')}/${String(m).padStart(2, '0')}/${y}`;
    } catch (err) {
      console.error('Error formateando fecha:', fecha, err);
      return '—';
    }
  };

  // Eliminar cita
  const eliminarCita = async (id) => {
    if (window.confirm('¿Está seguro de que desea eliminar esta cita?')) {
      try {
        await citaService.delete(id);
        await cargarCitas();
        mostrarExito('Cita eliminada correctamente');
        setError(null);
      } catch (err) {
        console.error('Error eliminando cita:', err);
        setError('Error al eliminar la cita: ' + err.message);
      }
    }
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold">Gestión de Citas</h2>
        <button
          onClick={abrirModalNuevo}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded disabled:opacity-50"
          disabled={loading}
        >
          + Nueva Cita
        </button>
      </div>

      {/* Mensaje de éxito */}
      {success && (
        <div className="mb-4 p-4 bg-green-100 border border-green-400 text-green-700 rounded">
          {success}
        </div>
      )}

      {/* Mensaje de error */}
      {error && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      {/* Tabla de citas */}
      {loading ? (
        <div className="text-center py-8">
          <p className="text-gray-500">Cargando citas...</p>
        </div>
      ) : citas.length === 0 ? (
        <div className="text-center py-8">
          <p className="text-gray-500">No hay citas registradas</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full border-collapse border border-gray-300">
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
                  <td className="border border-gray-300 px-4 py-2">{convertirHora(cita.hora) || '—'}</td>
                  <td className="border border-gray-300 px-4 py-2">{cita.tipoLavado}</td>
                  <td className="border border-gray-300 px-4 py-2">{cita.modeloVehiculo}</td>
                  <td className="border border-gray-300 px-4 py-2">
                    <span className={`px-3 py-1 rounded text-sm font-medium ${
                      cita.estado === 'CONFIRMADA' ? 'bg-green-100 text-green-800' :
                      cita.estado === 'CANCELADA' ? 'bg-red-100 text-red-800' :
                      'bg-yellow-100 text-yellow-800'
                    }`}>
                      {cita.estado}
                    </span>
                  </td>
                  <td className="border border-gray-300 px-4 py-2 text-center">
                    <button
                      onClick={() => abrirModalEditar(cita)}
                      className="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded mr-2"
                    >
                      Editar
                    </button>
                    <button
                      onClick={() => eliminarCita(cita.id)}
                      className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded"
                    >
                      Eliminar
                    </button>
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
                placeholder="Teléfono (9+ dígitos)"
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
              <input
                type="date"
                name="fecha"
                value={formData.fecha}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required
              />
              <input
                type="time"
                name="hora"
                value={formData.hora}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required
              />
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
                    {tipo.nombre} - ${tipo.precio}
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
                placeholder="Observaciones (opcional)"
                value={formData.observaciones}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded px-3 py-2"
                rows="3"
              />
              <div className="flex gap-4">
                <button
                  type="submit"
                  className="flex-1 bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded disabled:opacity-50"
                  disabled={loadingSubmit}
                >
                  {loadingSubmit ? 'Guardando...' : (editingCita ? 'Actualizar' : 'Crear')}
                </button>
                <button
                  type="button"
                  onClick={cerrarModal}
                  className="flex-1 bg-gray-400 hover:bg-gray-500 text-white px-4 py-2 rounded disabled:opacity-50"
                  disabled={loadingSubmit}
                >
                  Cancelar
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
