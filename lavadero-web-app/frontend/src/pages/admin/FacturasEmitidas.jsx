import { useState, useEffect } from 'react';
import facturaService from '../../services/facturaService';
import clienteService from '../../services/clienteService';

const FacturasEmitidas = () => {
  const [facturas, setFacturas] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [editandoFactura, setEditandoFactura] = useState(null);
  const [formData, setFormData] = useState({
    numero: '',
    fecha: '',
    tipo: 'SIMPLIFICADA',
    estado: 'PENDIENTE',
    metodoPago: 'EFECTIVO',
    clienteId: '', // NUEVO
    clienteNombre: '',
    clienteNif: '',
    clienteDireccion: '',
    clienteEmail: '',
    clienteTelefono: '',
    baseImponible: '',
    tipoIva: '21',
    importeIva: '',
    total: '',
    observaciones: '',
  });

  useEffect(() => {
    cargarFacturas();
    cargarClientes();
  }, []);

  const cargarClientes = async () => {
    setLoading(true);
    try {
      const response = await clienteService.getAll();
      setClientes(response.data || []);
      setError(null);
    } catch (err) {
      setError('Error al cargar los clientes: ' + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };
  // Cargar todas las facturas
  const cargarFacturas = async () => {
    setLoading(true);
    try {
      const response = await facturaService.getAll();
      let facturasData = response.data || [];

      // Ordenar por fecha (más reciente primero)
      facturasData = facturasData.sort((a, b) => {
        let fechaA = a.fecha;
        let fechaB = b.fecha;

        // Si es DD/MM/YYYY, convertir a YYYY-MM-DD
        if (fechaA && fechaA.includes('/')) {
          const [d, m, y] = fechaA.split('/');
          fechaA = `${y}-${m}-${d}`;
        }
        if (fechaB && fechaB.includes('/')) {
          const [d, m, y] = fechaB.split('/');
          fechaB = `${y}-${m}-${d}`;
        }

        return fechaB.localeCompare(fechaA);
      });

      setFacturas(facturasData);
      setError(null);
    } catch (err) {
      setError('Error al cargar las facturas: ' + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Abrir modal para crear nueva factura
  const abrirModalNuevo = () => {
    setFormData({
      numero: '',
      fecha: new Date().toISOString().split('T')[0],
      tipo: 'FACTURA',
      estado: 'PENDIENTE',
      metodoPago: 'EFECTIVO',
      clienteId: '',
      clienteNombre: '',
      clienteNif: '',
      clienteDireccion: '',
      clienteEmail: '',
      clienteTelefono: '',
      baseImponible: '',
      tipoIva: '21',
      importeIva: '',
      total: '',
      observaciones: '',
    });
    setEditandoFactura(null);
    setModalAbierto(true);
  };

  // Abrir modal para editar factura
  const abrirModalEditar = (factura) => {
    setFormData(factura);
    setEditandoFactura(factura.id);
    setModalAbierto(true);
  };

  // Cerrar modal
  const cerrarModal = () => {
    setModalAbierto(false);
    setEditandoFactura(null);
  };

  // Manejar cambios en los inputs
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };
  const handleClienteChange = (e) => {
    const clienteId = e.target.value;

    if (!clienteId) {
      setFormData(prev => ({
        ...prev,
        clienteId: '',
        clienteNombre: '',
        clienteNif: '',
        clienteDireccion: '',
        clienteEmail: '',
        clienteTelefono: '',
      }));
      return;
    }
    const cliente = clientes.find(cliente => cliente.id === e.target.value);
    setFormData(prev => ({
      ...prev,
      clienteId: e.target.value,
      clienteNombre: cliente ? cliente.nombre : '',
      clienteNif: cliente ? cliente.nif : '',
      clienteDireccion: cliente ? cliente.direccion : '',
      clienteEmail: cliente ? cliente.email : '',
      clienteTelefono: cliente ? cliente.telefono : '',
    }));
  };

  // Guardar factura
  const guardarFactura = async (e) => {
    e.preventDefault();

    if (!formData.numero || !formData.clienteNombre || !formData.total) {
      alert('Por favor completa los campos obligatorios');
      return;
    }

    try {
      if (editandoFactura) {
        await facturaService.update(editandoFactura, formData);
        alert('Factura actualizada correctamente');
      } else {
        await facturaService.create(formData);
        alert('Factura creada correctamente');
      }
      cerrarModal();
      cargarFacturas();
    } catch (err) {
      alert('Error al guardar factura: ' + err.message);
      console.error(err);
    }
  };

  // Eliminar factura
  const eliminarFactura = async (id) => {
    if (!window.confirm('¿Estás seguro de que deseas eliminar esta factura?')) {
      return;
    }

    try {
      await facturaService.delete(id);
      alert('Factura eliminada correctamente');
      cargarFacturas();
    } catch (err) {
      alert('Error al eliminar factura: ' + err.message);
      console.error(err);
    }
  };

  // Descargar PDF
  const descargarPdf = async (id, numero) => {
    try {
      const response = await facturaService.generarPdf(id);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `factura_${numero}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentElement.removeChild(link);
    } catch (err) {
      alert('Error al descargar PDF: ' + err.message);
      console.error(err);
    }
  };

  // Formatear fecha
  const formatearFecha = (fecha) => {
    if (!fecha) return '—';

    try {
      let day, month, year;

      if (typeof fecha === 'string') {
        if (fecha.includes('/')) {
          const partes = fecha.split('/');
          day = parseInt(partes[0]);
          month = parseInt(partes[1]);
          year = parseInt(partes[2]);
        } else if (fecha.includes('-')) {
          const partes = fecha.split('-');
          year = parseInt(partes[0]);
          month = parseInt(partes[1]);
          day = parseInt(partes[2]);
        }
      }

      if (day && month && year) {
        return `${String(day).padStart(2, '0')}/${String(month).padStart(2, '0')}/${year}`;
      }
      return fecha;
    } catch (err) {
      console.error('Error formateando fecha:', err);
      return fecha;
    }
  };

  // Formatear moneda
  const formatearMoneda = (cantidad) => {
    if (!cantidad) return '€0,00';
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
    }).format(parseFloat(cantidad));
  };

  // Obtener color de estado
  const getColorEstado = (estado) => {
    switch (estado) {
      case 'PAGADA':
        return 'bg-green-100 text-green-800';
      case 'PENDIENTE':
        return 'bg-yellow-100 text-yellow-800';
      case 'CANCELADA':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold">Facturas Emitidas</h2>
        <button
          onClick={abrirModalNuevo}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded font-semibold"
        >
          + Nueva Factura
        </button>
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-8">
          <p className="text-gray-500">Cargando facturas...</p>
        </div>
      ) : facturas.length === 0 ? (
        <div className="text-center py-8">
          <p className="text-gray-500">No hay facturas registradas</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-gray-100 border-b">
                <th className="px-4 py-2 text-left font-semibold">Nº Factura</th>
                <th className="px-4 py-2 text-left font-semibold">Fecha</th>
                <th className="px-4 py-2 text-left font-semibold">Cliente</th>
                <th className="px-4 py-2 text-left font-semibold">Tipo</th>
                <th className="px-4 py-2 text-right font-semibold">Total</th>
                <th className="px-4 py-2 text-center font-semibold">Estado</th>
                <th className="px-4 py-2 text-center font-semibold">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {facturas.map(factura => (
                <tr key={factura.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-3 font-semibold text-blue-600">{factura.numero}</td>
                  <td className="px-4 py-3">{formatearFecha(factura.fecha)}</td>
                  <td className="px-4 py-3">{factura.clienteNombre}</td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-1 bg-gray-200 rounded text-xs font-semibold">
                      {factura.tipo}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right font-semibold text-green-600">
                    {formatearMoneda(factura.total)}
                  </td>
                  <td className="px-4 py-3 text-center">
                    <span className={`px-2 py-1 rounded text-xs font-semibold ${getColorEstado(factura.estado)}`}>
                      {factura.estado}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-center space-x-2">
                    <button
                      onClick={() => abrirModalEditar(factura)}
                      className="text-blue-600 hover:text-blue-800 font-semibold text-sm"
                    >
                      Editar
                    </button>
                    <button
                      onClick={() => descargarPdf(factura.id, factura.numero)}
                      className="text-green-600 hover:text-green-800 font-semibold text-sm"
                    >
                      PDF
                    </button>
                    <button
                      onClick={() => eliminarFactura(factura.id)}
                      className="text-red-600 hover:text-red-800 font-semibold text-sm"
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

      {/* Modal para crear/editar factura - MEJORADO */}
      {modalAbierto && (
        <div className="fixed inset-0 bg-blue-900 bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-5xl w-full max-h-[92vh] flex flex-col">
            {/* Header del modal */}
            <div className="px-8 py-5 border-b border-gray-200 bg-gradient-to-r from-blue-50 to-blue-100">
              <h3 className="text-2xl font-bold text-gray-900">
                {editandoFactura ? 'Editar Factura Emitida' : 'Nueva Factura Emitida'}
              </h3>
            </div>

            {/* Contenido con scroll */}
            <div className="px-8 py-6 overflow-y-auto flex-1">
              <form onSubmit={guardarFactura} className="space-y-6">
                {/* Sección: Datos de la Factura */}
                <div>
                  <h4 className="text-lg font-semibold text-gray-800 mb-3 pb-2 border-b border-gray-200">
                    Datos de la Factura
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Nº Factura*
                      </label>
                      <input
                        type="text"
                        name="numero"
                        value={formData.numero}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="Ej: 2026/001"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Fecha*
                      </label>
                      <input
                        type="date"
                        name="fecha"
                        value={formData.fecha}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Tipo de Factura*
                      </label>
                      <select
                        name="tipo"
                        value={formData.tipo}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      >
                        <option value="SIMPLIFICADA">Simplificada</option>
                        <option value="COMPLETA">Completa</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Estado
                      </label>
                      <select
                        name="estado"
                        value={formData.estado}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      >
                        <option value="PENDIENTE">Pendiente</option>
                        <option value="PAGADA">Pagada</option>
                        <option value="CANCELADA">Cancelada</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Método de Pago
                      </label>
                      <select
                        name="metodoPago"
                        value={formData.metodoPago}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      >
                        <option value="EFECTIVO">Efectivo</option>
                        <option value="TARJETA">Tarjeta</option>
                        <option value="TRANSFERENCIA">Transferencia</option>
                        <option value="BIZUM">Bizum</option>
                      </select>
                    </div>
                  </div>
                </div>

                {/* Sección: Datos del Cliente */}
                <div>
                  <h4 className="text-lg font-semibold text-gray-800 mb-3 pb-2 border-b border-gray-200">
                    Datos del Cliente
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {/* DESPLEGABLE DE CLIENTES - REEMPLAZA EL INPUT DE NOMBRE */}
                    <div className="md:col-span-2">
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Seleccionar Cliente*
                      </label>
                      <select
                        name="clienteId"
                        value={formData.clienteId}
                        onChange={handleClienteChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      >
                        <option value="">-- Selecciona un cliente --</option>
                        {clientes.map(cliente => (
                          <option key={cliente.id} value={cliente.id}>
                            {cliente.nombre} {cliente.apellidos} {cliente.nif && `(${cliente.nif})`}
                          </option>
                        ))}
                      </select>
                    </div>

                    {/* Campos autorellenados (solo lectura) */}
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Nombre (autocompletado)
                      </label>
                      <input
                        type="text"
                        name="clienteNombre"
                        value={formData.clienteNombre}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-gray-50"
                        placeholder="Se rellenará automáticamente"
                        readOnly
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        NIF/CIF (autocompletado)
                      </label>
                      <input
                        type="text"
                        name="clienteNif"
                        value={formData.clienteNif}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-gray-50"
                        placeholder="Se rellenará automáticamente"
                        readOnly
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Teléfono (autocompletado)
                      </label>
                      <input
                        type="tel"
                        name="clienteTelefono"
                        value={formData.clienteTelefono}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-gray-50"
                        placeholder="Se rellenará automáticamente"
                        readOnly
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Email (autocompletado)
                      </label>
                      <input
                        type="email"
                        name="clienteEmail"
                        value={formData.clienteEmail}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-gray-50"
                        placeholder="Se rellenará automáticamente"
                        readOnly
                      />
                    </div>
                    <div className="md:col-span-2">
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Dirección (autocompletada)
                      </label>
                      <input
                        type="text"
                        name="clienteDireccion"
                        value={formData.clienteDireccion}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-gray-50"
                        placeholder="Se rellenará automáticamente"
                        readOnly
                      />
                    </div>
                  </div>
                </div>

                {/* Sección: Importes */}
                <div>
                  <h4 className="text-lg font-semibold text-gray-800 mb-3 pb-2 border-b border-gray-200">
                    Importes
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Base Imponible*
                      </label>
                      <input
                        type="number"
                        name="baseImponible"
                        value={formData.baseImponible}
                        onChange={handleInputChange}
                        step="0.01"
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="0,00"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        IVA (%)
                      </label>
                      <input
                        type="number"
                        name="tipoIva"
                        value={formData.tipoIva}
                        onChange={handleInputChange}
                        step="0.01"
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="21"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Total*
                      </label>
                      <input
                        type="number"
                        name="total"
                        value={formData.total}
                        onChange={handleInputChange}
                        step="0.01"
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent text-lg font-semibold"
                        placeholder="0,00"
                        required
                      />
                    </div>
                  </div>
                </div>

                {/* Sección: Observaciones */}
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">
                    Observaciones
                  </label>
                  <textarea
                    name="observaciones"
                    value={formData.observaciones}
                    onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    rows="3"
                    placeholder="Notas adicionales sobre la factura"
                  />
                </div>
              </form>
            </div>

            {/* Footer con botones */}
            <div className="px-8 py-5 border-t border-gray-200 bg-gray-50 flex justify-end gap-3 rounded-b-xl">
              <button
                type="button"
                onClick={cerrarModal}
                className="px-6 py-2.5 border-2 border-gray-300 rounded-lg font-semibold text-gray-700 hover:bg-gray-100 transition-colors"
              >
                Cancelar
              </button>
              <button
                type="submit"
                onClick={guardarFactura}
                className="px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-semibold transition-colors shadow-md hover:shadow-lg"
              >
                {editandoFactura ? 'Actualizar Factura' : 'Crear Factura'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default FacturasEmitidas;
