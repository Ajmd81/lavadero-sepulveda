// src/pages/admin/AlbaranesEmitidos.jsx
import React, { useState, useEffect } from 'react';
import { albaranService } from '../../services/albaranService';
import clienteService from '../../services/clienteService';
import { 
  FiPlus, FiEdit2, FiTrash2, FiCheck, FiClock, 
  FiFileText, FiSearch, FiFilter, FiX 
} from 'react-icons/fi';

const AlbaranesEmitidos = () => {
  
  const [albaranes, setAlbaranes] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [filteredAlbaranes, setFilteredAlbaranes] = useState([]);
  const [loading, setLoading] = useState(true);
  
  const [searchTerm, setSearchTerm] = useState('');
  const [estadoFilter, setEstadoFilter] = useState('TODOS');
  const [clienteFilter, setClienteFilter] = useState('');
  
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState('create'); // 'create' | 'edit'
  const [currentAlbaran, setCurrentAlbaran] = useState(null);
  
  const [showFacturarModal, setShowFacturarModal] = useState(false);
  const [selectedAlbaranes, setSelectedAlbaranes] = useState([]);
  const [clienteFacturar, setClienteFacturar] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    filterAlbaranes();
  }, [searchTerm, estadoFilter, clienteFilter, albaranes]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [albaranesData, clientesData] = await Promise.all([
        albaranService.getAll(),
        clienteService.getAll()
      ]);
      
      const albaranesArray = Array.isArray(albaranesData) ? albaranesData : [];
      
      let clientesArray = [];
      if (Array.isArray(clientesData)) {
        clientesArray = clientesData;
      } else if (clientesData?.data && Array.isArray(clientesData.data)) {
        clientesArray = clientesData.data;
      } else if (clientesData?.data?.content && Array.isArray(clientesData.data.content)) {
        clientesArray = clientesData.data.content;
      }
      
      setAlbaranes(albaranesArray);
      setClientes(clientesArray);
    } catch (error) {
      console.error('Error cargando datos:', error);
      setAlbaranes([]);
      setClientes([]);
    } finally {
      setLoading(false);
    }
  };

  const filterAlbaranes = () => {
    let filtered = [...albaranes];

    if (searchTerm) {
      filtered = filtered.filter(alb =>
        alb.numero?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        alb.clienteNombre?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (estadoFilter !== 'TODOS') {
      filtered = filtered.filter(alb => alb.estado === estadoFilter);
    }

    if (clienteFilter) {
      filtered = filtered.filter(alb => alb.clienteId === parseInt(clienteFilter));
    }

    setFilteredAlbaranes(filtered);
  };

  const handleOpenModal = (mode, albaran = null) => {
    setModalMode(mode);
    setCurrentAlbaran(albaran);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setCurrentAlbaran(null);
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Estás seguro de eliminar este albarán?')) {
      try {
        await albaranService.delete(id);
        loadData();
      } catch (error) {
        alert('Error al eliminar: ' + error.message);
      }
    }
  };

  const handleCambiarEstado = async (id, nuevoEstado) => {
    try {
      await albaranService.cambiarEstado(id, nuevoEstado);
      loadData();
    } catch (error) {
      alert('Error al cambiar estado: ' + error.message);
    }
  };

  const handleOpenFacturarModal = (clienteId) => {
    setClienteFacturar(clienteId);
    setSelectedAlbaranes([]);
    setShowFacturarModal(true);
  };

  const toggleAlbaranSelection = (albaranId) => {
    if (selectedAlbaranes.includes(albaranId)) {
      setSelectedAlbaranes(selectedAlbaranes.filter(id => id !== albaranId));
    } else {
      setSelectedAlbaranes([...selectedAlbaranes, albaranId]);
    }
  };

  const handleFacturar = () => {
    if (selectedAlbaranes.length === 0) {
      alert('Selecciona al menos un albarán');
      return;
    }
    
    // Aquí integrarías con el módulo de facturas
    alert(`Se van a facturar ${selectedAlbaranes.length} albaranes. (Funcionalidad pendiente de integrar con módulo de facturas)`);
    setShowFacturarModal(false);
  };

  const getEstadoBadge = (estado) => {
    const badges = {
      PENDIENTE: { color: 'bg-yellow-100 text-yellow-800', icon: FiClock, text: 'Pendiente' },
      ENTREGADO: { color: 'bg-green-100 text-green-800', icon: FiCheck, text: 'Entregado' },
      FACTURADO: { color: 'bg-blue-100 text-blue-800', icon: FiFileText, text: 'Facturado' }
    };
    
    const badge = badges[estado] || badges.PENDIENTE;
    const Icon = badge.icon;
    
    return (
      <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${badge.color}`}>
        <Icon size={12} /> {badge.text}
      </span>
    );
  };

  const albaranesPendientesFacturar = albaranes.filter(a => a.estado === 'ENTREGADO');
  const clientesConAlbaranes = [...new Set(albaranesPendientesFacturar.map(a => a.clienteId))];

  if (loading) {
    return <div className="loading">Cargando albaranes...</div>;
  }

  return (
    <div className="bg-white rounded-lg shadow">
      {/* Header */}
      <div className="p-6 border-b border-gray-200">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold text-gray-900">Albaranes Emitidos</h2>
          <button
            onClick={() => handleOpenModal('create')}
            className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <FiPlus size={20} />
            Nuevo Albarán
          </button>
        </div>

        {/* Filtros */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="relative">
            <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Buscar por número o cliente..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div className="relative">
            <FiFilter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <select
              value={estadoFilter}
              onChange={(e) => setEstadoFilter(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent appearance-none"
            >
              <option value="TODOS">Todos los estados</option>
              <option value="PENDIENTE">Pendiente</option>
              <option value="ENTREGADO">Entregado</option>
              <option value="FACTURADO">Facturado</option>
            </select>
          </div>

          <select
            value={clienteFilter}
            onChange={(e) => setClienteFilter(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">Todos los clientes</option>
            {clientes.map(cliente => (
              <option key={cliente.id} value={cliente.id}>{cliente.nombre}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Botón Facturar Albaranes */}
      {clientesConAlbaranes.length > 0 && (
        <div className="p-4 bg-green-50 border-b border-green-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-green-900">
                Hay {albaranesPendientesFacturar.length} albaranes entregados pendientes de facturar
              </p>
              <p className="text-xs text-green-700">
                De {clientesConAlbaranes.length} cliente(s)
              </p>
            </div>
            <select
              onChange={(e) => e.target.value && handleOpenFacturarModal(e.target.value)}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 cursor-pointer"
              value=""
            >
              <option value="">Seleccionar cliente para facturar</option>
              {clientesConAlbaranes.map(clienteId => {
                const cliente = clientes.find(c => c.id === clienteId);
                const cantidad = albaranesPendientesFacturar.filter(a => a.clienteId === clienteId).length;
                return (
                  <option key={clienteId} value={clienteId}>
                    {cliente?.nombre} ({cantidad} albaranes)
                  </option>
                );
              })}
            </select>
          </div>
        </div>
      )}

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4 p-6 bg-gray-50">
        <div className="text-center">
          <div className="text-2xl font-bold text-yellow-600">
            {albaranes.filter(a => a.estado === 'PENDIENTE').length}
          </div>
          <div className="text-sm text-gray-600">Pendientes</div>
        </div>
        <div className="text-center">
          <div className="text-2xl font-bold text-green-600">
            {albaranes.filter(a => a.estado === 'ENTREGADO').length}
          </div>
          <div className="text-sm text-gray-600">Entregados</div>
        </div>
        <div className="text-center">
          <div className="text-2xl font-bold text-blue-600">
            {albaranes.filter(a => a.estado === 'FACTURADO').length}
          </div>
          <div className="text-sm text-gray-600">Facturados</div>
        </div>
      </div>

      {/* Tabla */}
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Número
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Cliente
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Fecha
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Estado
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Total
              </th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filteredAlbaranes.length === 0 ? (
              <tr>
                <td colSpan="6" className="px-6 py-8 text-center text-gray-500">
                  No se encontraron albaranes
                </td>
              </tr>
            ) : (
              filteredAlbaranes.map(albaran => (
                <tr key={albaran.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="text-sm font-medium text-gray-900">{albaran.numero}</span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="text-sm text-gray-900">{albaran.clienteNombre}</span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="text-sm text-gray-500">
                      {new Date(albaran.fecha).toLocaleDateString('es-ES')}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {getEstadoBadge(albaran.estado)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right">
                    <span className="text-sm font-medium text-gray-900">
                      {albaran.total?.toFixed(2)} €
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-center">
                    <div className="flex items-center justify-center gap-2">
                      {albaran.estado !== 'FACTURADO' && (
                        <>
                          <button
                            onClick={() => handleOpenModal('edit', albaran)}
                            className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                            title="Editar"
                          >
                            <FiEdit2 size={18} />
                          </button>
                          <button
                            onClick={() => handleDelete(albaran.id)}
                            className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                            title="Eliminar"
                          >
                            <FiTrash2 size={18} />
                          </button>
                        </>
                      )}
                      {albaran.estado === 'PENDIENTE' && (
                        <button
                          onClick={() => handleCambiarEstado(albaran.id, 'ENTREGADO')}
                          className="px-3 py-1 text-xs bg-green-600 text-white rounded hover:bg-green-700 transition-colors"
                        >
                          Marcar entregado
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Modal Crear/Editar */}
      {showModal && (
        <AlbaranModal
          mode={modalMode}
          albaran={currentAlbaran}
          clientes={clientes}
          onClose={handleCloseModal}
          onSave={() => {
            loadData();
            handleCloseModal();
          }}
        />
      )}

      {/* Modal Facturar */}
      {showFacturarModal && (
        <FacturarAlbaranesModal
          clienteId={clienteFacturar}
          albaranes={albaranes.filter(a => a.clienteId === parseInt(clienteFacturar) && a.estado === 'ENTREGADO')}
          selectedAlbaranes={selectedAlbaranes}
          onToggle={toggleAlbaranSelection}
          onClose={() => setShowFacturarModal(false)}
          onFacturar={handleFacturar}
        />
      )}
    </div>
  );
};

// Modal Crear/Editar Albarán
const AlbaranModal = ({ mode, albaran, clientes, onClose, onSave }) => {
  const [formData, setFormData] = useState({
    clienteId: albaran?.clienteId || '',
    fecha: albaran?.fecha || new Date().toISOString().split('T')[0],
    lineas: albaran?.lineas || [
      { concepto: '', cantidad: 1, precioUnitario: 0, tipoIva: 21 }
    ]
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.clienteId) {
      alert('Selecciona un cliente');
      return;
    }

    try {
      setLoading(true);
      
      const dataToSend = {
        clienteId: parseInt(formData.clienteId),
        fecha: formData.fecha,
        lineas: formData.lineas.map(linea => ({
          concepto: linea.concepto,
          cantidad: parseInt(linea.cantidad),
          precioUnitario: parseFloat(linea.precioUnitario),
          tipoIva: parseFloat(linea.tipoIva)
        }))
      };

      if (mode === 'edit') {
        await albaranService.update(albaran.id, dataToSend);
      } else {
        await albaranService.create(dataToSend);
      }
      
      onSave();
    } catch (error) {
      console.error('Error:', error);
      alert('Error al guardar: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const addLinea = () => {
    setFormData({
      ...formData,
      lineas: [...formData.lineas, { concepto: '', cantidad: 1, precioUnitario: 0, tipoIva: 21 }]
    });
  };

  const removeLinea = (index) => {
    if (formData.lineas.length === 1) return;
    setFormData({
      ...formData,
      lineas: formData.lineas.filter((_, i) => i !== index)
    });
  };

  const updateLinea = (index, field, value) => {
    const newLineas = [...formData.lineas];
    newLineas[index][field] = value;
    setFormData({ ...formData, lineas: newLineas });
  };

  const calcularLinea = (linea) => {
    const subtotal = linea.cantidad * linea.precioUnitario;
    const iva = subtotal * (linea.tipoIva / 100);
    return { subtotal, iva, total: subtotal + iva };
  };

  const calcularTotales = () => {
    return formData.lineas.reduce(
      (acc, linea) => {
        const { subtotal, iva } = calcularLinea(linea);
        return {
          base: acc.base + subtotal,
          iva: acc.iva + iva,
          total: acc.total + subtotal + iva
        };
      },
      { base: 0, iva: 0, total: 0 }
    );
  };

  const totales = calcularTotales();

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
          <h3 className="text-xl font-bold text-gray-900">
            {mode === 'edit' ? 'Editar Albarán' : 'Nuevo Albarán'}
          </h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <FiX size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Cliente *
              </label>
              <select
                value={formData.clienteId}
                onChange={(e) => setFormData({ ...formData, clienteId: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                required
              >
                <option value="">Seleccionar...</option>
                {clientes.map(c => (
                  <option key={c.id} value={c.id}>{c.nombre}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Fecha *
              </label>
              <input
                type="date"
                value={formData.fecha}
                onChange={(e) => setFormData({ ...formData, fecha: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
          </div>

          <div>
            <div className="flex justify-between items-center mb-4">
              <h4 className="text-lg font-medium text-gray-900">Líneas del Albarán</h4>
              <button
                type="button"
                onClick={addLinea}
                className="inline-flex items-center gap-2 px-3 py-2 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200"
              >
                <FiPlus size={16} /> Añadir línea
              </button>
            </div>

            <div className="space-y-3">
              {formData.lineas.map((linea, index) => {
                const { total } = calcularLinea(linea);
                return (
                  <div key={index} className="grid grid-cols-12 gap-2 items-center p-3 bg-gray-50 rounded-lg">
                    <div className="col-span-4">
                      <input
                        type="text"
                        placeholder="Concepto"
                        value={linea.concepto}
                        onChange={(e) => updateLinea(index, 'concepto', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                        required
                      />
                    </div>
                    <div className="col-span-2">
                      <input
                        type="number"
                        placeholder="Cant."
                        value={linea.cantidad}
                        onChange={(e) => updateLinea(index, 'cantidad', parseFloat(e.target.value) || 1)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                        min="1"
                        required
                      />
                    </div>
                    <div className="col-span-2">
                      <input
                        type="number"
                        placeholder="Precio"
                        value={linea.precioUnitario}
                        onChange={(e) => updateLinea(index, 'precioUnitario', parseFloat(e.target.value) || 0)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                        step="0.01"
                        min="0"
                        required
                      />
                    </div>
                    <div className="col-span-2">
                      <select
                        value={linea.tipoIva}
                        onChange={(e) => updateLinea(index, 'tipoIva', parseFloat(e.target.value))}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                      >
                        <option value="0">0%</option>
                        <option value="4">4%</option>
                        <option value="10">10%</option>
                        <option value="21">21%</option>
                      </select>
                    </div>
                    <div className="col-span-1 text-right text-sm font-medium">
                      {total.toFixed(2)}€
                    </div>
                    <div className="col-span-1 text-center">
                      <button
                        type="button"
                        onClick={() => removeLinea(index)}
                        disabled={formData.lineas.length === 1}
                        className="p-2 text-red-600 hover:bg-red-50 rounded-lg disabled:opacity-30"
                      >
                        <FiTrash2 size={16} />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          <div className="border-t border-gray-200 pt-4">
            <div className="flex justify-end space-y-2">
              <div className="text-right space-y-1">
                <div className="text-sm text-gray-600">
                  Base: <span className="font-medium">{totales.base.toFixed(2)}€</span>
                </div>
                <div className="text-sm text-gray-600">
                  IVA: <span className="font-medium">{totales.iva.toFixed(2)}€</span>
                </div>
                <div className="text-lg font-bold text-gray-900">
                  Total: {totales.total.toFixed(2)}€
                </div>
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
              {loading ? 'Guardando...' : 'Guardar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Modal Facturar Albaranes
const FacturarAlbaranesModal = ({ albaranes, selectedAlbaranes, onToggle, onClose, onFacturar }) => {
  const totalFacturar = albaranes
    .filter(a => selectedAlbaranes.includes(a.id))
    .reduce((sum, a) => sum + (a.total || 0), 0);

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[80vh] overflow-hidden flex flex-col">
        <div className="bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
          <h3 className="text-xl font-bold text-gray-900">Seleccionar Albaranes para Facturar</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <FiX size={24} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-6">
          <div className="space-y-3">
            {albaranes.map(albaran => (
              <div
                key={albaran.id}
                onClick={() => onToggle(albaran.id)}
                className={`p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                  selectedAlbaranes.includes(albaran.id)
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <input
                      type="checkbox"
                      checked={selectedAlbaranes.includes(albaran.id)}
                      onChange={() => {}}
                      className="h-5 w-5 text-blue-600 rounded"
                    />
                    <div>
                      <div className="font-medium text-gray-900">{albaran.numero}</div>
                      <div className="text-sm text-gray-500">
                        {new Date(albaran.fecha).toLocaleDateString('es-ES')}
                      </div>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold text-gray-900">{albaran.total?.toFixed(2)}€</div>
                    <div className="text-xs text-gray-500">{albaran.lineas?.length || 0} líneas</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="border-t border-gray-200 px-6 py-4 bg-gray-50">
          <div className="flex justify-between items-center mb-4">
            <div className="text-sm text-gray-600">
              {selectedAlbaranes.length} albarán(es) seleccionado(s)
            </div>
            <div className="text-xl font-bold text-gray-900">
              Total: {totalFacturar.toFixed(2)}€
            </div>
          </div>
          <div className="flex justify-end gap-3">
            <button
              onClick={onClose}
              className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              onClick={onFacturar}
              disabled={selectedAlbaranes.length === 0}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Crear Factura
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AlbaranesEmitidos;