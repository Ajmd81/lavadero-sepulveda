import { useState, useEffect } from 'react';
import facturaRecibidaService from '../../services/facturaRecibidaService';

const FacturasRecibidas = () => {
  const [facturas, setFacturas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [editandoFactura, setEditandoFactura] = useState(null);
  const [formData, setFormData] = useState({
    numeroFactura: '',
    proveedorNombre: '',
    proveedorNif: '',
    fechaFactura: '',
    fechaVencimiento: '',
    fechaPago: '',
    categoria: 'SUMINISTROS',
    concepto: '',
    baseImponible: '',
    tipoIva: '21',
    cuotaIva: '',
    tipoIrpf: '0',
    cuotaIrpf: '',
    total: '',
    estado: 'PENDIENTE',
    metodoPago: 'TRANSFERENCIA',
    notas: '',
  });

  useEffect(() => {
    cargarFacturas();
  }, []);

  // Cargar todas las facturas recibidas
  const cargarFacturas = async () => {
    setLoading(true);
    try {
      const response = await facturaRecibidaService.getAll();
      let facturasData = response.data || [];
      
      // Ordenar por fecha (más reciente primero)
      facturasData = facturasData.sort((a, b) => {
        let fechaA = a.fechaFactura;
        let fechaB = b.fechaFactura;
        
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
      setError('Error al cargar las facturas recibidas: ' + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Abrir modal para crear nueva factura
  const abrirModalNuevo = () => {
    setFormData({
      numeroFactura: '',
      proveedorNombre: '',
      proveedorNif: '',
      fechaFactura: new Date().toISOString().split('T')[0],
      fechaVencimiento: '',
      fechaPago: '',
      categoria: 'SUMINISTROS',
      concepto: '',
      baseImponible: '',
      tipoIva: '21',
      cuotaIva: '',
      tipoIrpf: '0',
      cuotaIrpf: '',
      total: '',
      estado: 'PENDIENTE',
      metodoPago: 'TRANSFERENCIA',
      notas: '',
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

  // Guardar factura
  const guardarFactura = async (e) => {
    e.preventDefault();
    
    if (!formData.numeroFactura || !formData.proveedorNombre || !formData.total) {
      alert('Por favor completa los campos obligatorios');
      return;
    }

    try {
      if (editandoFactura) {
        await facturaRecibidaService.update(editandoFactura, formData);
        alert('Factura actualizada correctamente');
      } else {
        await facturaRecibidaService.create(formData);
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
      await facturaRecibidaService.delete(id);
      alert('Factura eliminada correctamente');
      cargarFacturas();
    } catch (err) {
      alert('Error al eliminar factura: ' + err.message);
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
      case 'RECHAZADA':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  // Calcular total de facturas
  const totalFacturas = facturas.reduce((sum, factura) => sum + (parseFloat(factura.total) || 0), 0);

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold">Facturas Recibidas</h2>
          <p className="text-gray-500 text-sm mt-1">
            Total: {formatearMoneda(totalFacturas)}
          </p>
        </div>
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
          <p className="text-gray-500">Cargando facturas recibidas...</p>
        </div>
      ) : facturas.length === 0 ? (
        <div className="text-center py-8">
          <p className="text-gray-500">No hay facturas recibidas registradas</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-gray-100 border-b">
                <th className="px-4 py-2 text-left font-semibold">Nº Factura</th>
                <th className="px-4 py-2 text-left font-semibold">Proveedor</th>
                <th className="px-4 py-2 text-left font-semibold">Fecha</th>
                <th className="px-4 py-2 text-left font-semibold">Vencimiento</th>
                <th className="px-4 py-2 text-left font-semibold">Categoría</th>
                <th className="px-4 py-2 text-right font-semibold">Total</th>
                <th className="px-4 py-2 text-center font-semibold">Estado</th>
                <th className="px-4 py-2 text-center font-semibold">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {facturas.map(factura => (
                <tr key={factura.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-3 font-semibold text-blue-600">{factura.numeroFactura}</td>
                  <td className="px-4 py-3">{factura.proveedorNombre}</td>
                  <td className="px-4 py-3">{formatearFecha(factura.fechaFactura)}</td>
                  <td className="px-4 py-3">{formatearFecha(factura.fechaVencimiento)}</td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-1 bg-gray-200 rounded text-xs font-semibold">
                      {factura.categoria}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right font-semibold text-orange-600">
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

      {/* Modal para crear/editar factura */}
      {modalAbierto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-2xl w-full max-h-96 overflow-y-auto">
            <h3 className="text-xl font-bold mb-4">
              {editandoFactura ? 'Editar Factura Recibida' : 'Nueva Factura Recibida'}
            </h3>
            
            <form onSubmit={guardarFactura} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-semibold mb-1">Nº Factura*</label>
                  <input
                    type="text"
                    name="numeroFactura"
                    value={formData.numeroFactura}
                    onChange={handleInputChange}
                    className="w-full border rounded px-3 py-2"
                    placeholder="Ej: FAC-2026-001"
                  />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Fecha*</label>
                  <input
                    type="date"
                    name="fechaFactura"
                    value={formData.fechaFactura}
                    onChange={handleInputChange}
                    className="w-full border rounded px-3 py-2"
                  />
                </div>
                <div className="col-span-2">
                  <label className="block text-sm font-semibold mb-1">Proveedor*</label>
                  <input
                    type="text"
                    name="proveedorNombre"
                    value={formData.proveedorNombre}
                    onChange={handleInputChange}
                    className="w-full border rounded px-3 py-2"
                    placeholder="Nombre del proveedor"
                  />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">NIF/CIF</label>
                  <input
                    type="text"
                    name="proveedorNif"
                    value={formData.proveedorNif}
                    onChange={handleInputChange}
                    className="w-full border rounded px-3 py-2"
                    placeholder="NIF/CIF"
                  />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Categoría</label>
                  <select
                    name="categoria"
                    value={formData.categoria}
                    onChange={handleInputChange}
                    className="w-full border rounded px-3 py-2"
                  >
                    <option value="SUMINISTROS">Suministros</option>
                    <option value="SERVICIOS">Servicios</option>
                    <option value="MANTENIMIENTO">Mantenimiento</option>
                    <option value="OTROS">Otros</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Base Imponible*</label>
                  <input
                    type="number"
                    name="baseImponible"
                    value={formData.baseImponible}
                    onChange={handleInputChange}
                    step="0.01"
                    className="w-full border rounded px-3 py-2"
                    placeholder="0,00"
                  />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Total*</label>
                  <input
                    type="number"
                    name="total"
                    value={formData.total}
                    onChange={handleInputChange}
                    step="0.01"
                    className="w-full border rounded px-3 py-2"
                    placeholder="0,00"
                  />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Estado</label>
                  <select
                    name="estado"
                    value={formData.estado}
                    onChange={handleInputChange}
                    className="w-full border rounded px-3 py-2"
                  >
                    <option value="PENDIENTE">Pendiente</option>
                    <option value="PAGADA">Pagada</option>
                    <option value="RECHAZADA">Rechazada</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-1">Vencimiento</label>
                  <input
                    type="date"
                    name="fechaVencimiento"
                    value={formData.fechaVencimiento}
                    onChange={handleInputChange}
                    className="w-full border rounded px-3 py-2"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-semibold mb-1">Notas</label>
                <textarea
                  name="notas"
                  value={formData.notas}
                  onChange={handleInputChange}
                  className="w-full border rounded px-3 py-2"
                  rows="2"
                  placeholder="Notas adicionales"
                />
              </div>

              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={cerrarModal}
                  className="px-4 py-2 border rounded font-semibold hover:bg-gray-100"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded font-semibold"
                >
                  {editandoFactura ? 'Actualizar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default FacturasRecibidas;
