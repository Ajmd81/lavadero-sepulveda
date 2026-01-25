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
    nombreCliente: '',
    telefonoCliente: '',
    emailCliente: '',
    fechaCita: '',
    horaCita: '',
    tipoLavado: '',
    placa: '',
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
      setCitas(response.data || []);
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
      nombreCliente: '',
      telefonoCliente: '',
      emailCliente: '',
      fechaCita: '',
      horaCita: '',
      tipoLavado: '',
      placa: '',
      observaciones: '',
    });
    setShowModal(true);
  };

  // Abrir modal para editar cita
  const abrirModalEditar = (cita) => {
    setEditingCita(cita);
    setFormData({
      nombreCliente: cita.nombreCliente || '',
      telefonoCliente: cita.telefonoCliente || '',
      emailCliente: cita.emailCliente || '',
      fechaCita: cita.fechaCita || '',
      horaCita: cita.horaCita || '',
      tipoLavado: cita.tipoLavado || '',
      placa: cita.placa || '',
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
                <th className="border border-gray-300 px-4 py-2 text-left">Placa</th>
                <th className="border border-gray-300 px-4 py-2 text-left">Estado</th>
                <th className="border border-gray-300 px-4 py-2 text-center">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {citas.map((cita) => (
                <tr key={cita.id} className="hover:bg-gray-50">
                  <td className="border border-gray-300 px-4 py-2">{cita.nombreCliente}</td>
                  <td className="border border-gray-300 px-4 py-2">{cita.telefonoCliente}</td>
                  <td className="border border-gray-300 px-4 py-2">{cita.fechaCita}</td>
                  <td className="border border-gray-300 px-4 py-2">{cita.horaCita}</td>
                  <td className="border border-gray-300 px-4 py-2">{cita.tipoLavado}</td>
                  <td className="border border-gray-300 px-4 py-2">{cita.placa}</td>
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
          <div className="bg-white rounded-lg p-8 max-w-md w-full">
            <h3 className="text-xl font-bold mb-4">
              {editingCita ? 'Editar Cita' : 'Nueva Cita'}
            </h3>
            <form onSubmit={guardarCita} className="space-y-4">
              <input
                type="text"
                name="nombreCliente"
                placeholder="Nombre del cliente"
                value={formData.nombreCliente}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required
              />
              <input
                type="tel"
                name="telefonoCliente"
                placeholder="Teléfono"
                value={formData.telefonoCliente}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required
              />
              <input
                type="email"
                name="emailCliente"
                placeholder="Email"
                value={formData.emailCliente}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded px-3 py-2"
              />
              <input
                type="date"
                name="fechaCita"
                value={formData.fechaCita}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required
              />
              <input
                type="time"
                name="horaCita"
                value={formData.horaCita}
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
                name="placa"
                placeholder="Placa del vehículo"
                value={formData.placa}
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
