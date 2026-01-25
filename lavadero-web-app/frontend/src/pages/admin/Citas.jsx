import { useState, useEffect } from 'react';
import citaService from '../../services/citaService';

const Citas = () => {
  const [citas, setCitas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
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

  // Cargar citas al montar componente
  useEffect(() => {
    cargarCitas();
    cargarTiposLavado();
  }, []);

  // Cargar todas las citas
  const cargarCitas = async () => {
    setLoading(true);
    try {
      const response = await citaService.getAll();
      let citasData = response.data || [];
      
      // Ordenar citas por fecha y hora
      citasData = citasData.sort((a, b) => {
        // Convertir fechas a formato comparable YYYY-MM-DD
        const fechaA = a.fecha ? a.fecha.split('T')[0] : '0000-00-00';
        const fechaB = b.fecha ? b.fecha.split('T')[0] : '0000-00-00';
        
        // Comparar fechas primero
        const comparacionFecha = fechaA.localeCompare(fechaB);
        if (comparacionFecha !== 0) {
          return comparacionFecha; // Ordenar por fecha
        }
        
        // Si fechas son iguales, comparar por hora
        const horaA = a.hora ? a.hora.substring(0, 5) : '00:00';
        const horaB = b.hora ? b.hora.substring(0, 5) : '00:00';
        
        return horaA.localeCompare(horaB); // Ordenar por hora (menor a mayor)
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
      const response = await citaService.getTiposLavado?.();
      if (response?.data) {
        setTiposLavado(response.data);
      }
    } catch (err) {
      console.error('Error cargando tipos de lavado:', err);
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
    setShowModal(true);
  };

  // Abrir modal para editar cita
  const abrirModalEditar = (cita) => {
    setEditingCita(cita);
    
    // Debug: Ver qué viene en la cita
    console.log('Cita completa:', cita);
    console.log('Fecha recibida:', cita.fecha, 'Tipo:', typeof cita.fecha);
    console.log('Hora recibida:', cita.hora, 'Tipo:', typeof cita.hora);
    
    // Convertir fecha a formato YYYY-MM-DD
    let fechaFormato = '';
    if (cita.fecha) {
      if (typeof cita.fecha === 'string') {
        // Si es string, tomar solo la parte de fecha (antes de T)
        fechaFormato = cita.fecha.split('T')[0];
      } else if (cita.fecha instanceof Date) {
        // Si es un objeto Date
        const year = cita.fecha.getFullYear();
        const month = String(cita.fecha.getMonth() + 1).padStart(2, '0');
        const day = String(cita.fecha.getDate()).padStart(2, '0');
        fechaFormato = `${year}-${month}-${day}`;
      }
    }
    
    // Convertir hora a formato HH:mm
    let horaFormato = '';
    if (cita.hora) {
      if (typeof cita.hora === 'string') {
        // Si es string, tomar solo HH:mm
        horaFormato = cita.hora.substring(0, 5);
      } else if (cita.hora instanceof Date) {
        // Si es un objeto Date
        const hours = String(cita.hora.getHours()).padStart(2, '0');
        const minutes = String(cita.hora.getMinutes()).padStart(2, '0');
        horaFormato = `${hours}:${minutes}`;
      }
    }
    
    console.log('Fecha convertida:', fechaFormato);
    console.log('Hora convertida:', horaFormato);
    
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

  // Cerrar modal
  const cerrarModal = () => {
    setShowModal(false);
    setEditingCita(null);
  };

  // Manejar cambios en el formulario
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Guardar cita (crear o actualizar)
  const guardarCita = async (e) => {
    e.preventDefault();
    try {
      if (editingCita) {
        // Actualizar cita existente
        await citaService.update(editingCita.id, formData);
      } else {
        // Crear nueva cita
        await citaService.create(formData);
      }
      await cargarCitas();
      cerrarModal();
      setError(null);
    } catch (err) {
      setError('Error al guardar la cita: ' + err.message);
      console.error(err);
    }
  };

  // Función auxiliar para formatear fecha
  const formatearFecha = (fecha) => {
    if (!fecha) return '';
    try {
      let fechaObj;
      
      // Si es string en formato ISO (YYYY-MM-DD)
      if (typeof fecha === 'string') {
        // Crear fecha sin considerar timezone
        const partes = fecha.split('T')[0].split('-');
        fechaObj = new Date(parseInt(partes[0]), parseInt(partes[1]) - 1, parseInt(partes[2]));
      } else if (fecha instanceof Date) {
        fechaObj = fecha;
      } else {
        return fecha.toString();
      }
      
      // Validar que la fecha sea válida
      if (isNaN(fechaObj.getTime())) {
        console.warn('Fecha inválida:', fecha);
        return fecha.toString();
      }
      
      return fechaObj.toLocaleDateString('es-ES', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    } catch (err) {
      console.error('Error formateando fecha:', err);
      return fecha?.toString() || '';
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

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold">Gestión de Citas</h2>
        <button
          onClick={abrirModalNuevo}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded"
        >
          + Nueva Cita
        </button>
      </div>

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
                  <td className="border border-gray-300 px-4 py-2">{cita.hora?.substring(0, 5)}</td>
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
                placeholder="Observaciones"
                value={formData.observaciones}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded px-3 py-2"
                rows="3"
              />
              <div className="flex gap-4">
                <button
                  type="submit"
                  className="flex-1 bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded"
                >
                  {editingCita ? 'Actualizar' : 'Crear'}
                </button>
                <button
                  type="button"
                  onClick={cerrarModal}
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
  );
};

export default Citas;
