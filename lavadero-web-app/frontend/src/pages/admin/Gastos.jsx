import { useState, useEffect } from 'react';
import gastoService from '../../services/gastoService';

const Gastos = () => {
  const [gastos, setGastos] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [editandoGasto, setEditandoGasto] = useState(null);
  const [formData, setFormData] = useState({
    concepto: '',
    fecha: '',
    categoria: 'MANTENIMIENTO',
    importe: '',
    ivaIncluido: false,
    baseImponible: '',
    cuotaIva: '',
    metodoPago: 'EFECTIVO',
    recurrente: false,
    diaRecurrencia: '',
    notas: '',
    pagado: false,
  });

  useEffect(() => {
    cargarGastos();
  }, []);

  const cargarGastos = async () => {
    setLoading(true);
    try {
      const response = await gastoService.getAll();
      let gastosData = response.data || [];

      gastosData = gastosData.sort((a, b) => {
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

        return fechaB.localeCompare(fechaA);
      });

      setGastos(gastosData);
      setError(null);
    } catch (err) {
      setError('Error al cargar los gastos: ' + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const abrirModalNuevo = () => {
    setFormData({
      concepto: '',
      fecha: new Date().toISOString().split('T')[0],
      categoria: 'MANTENIMIENTO',
      importe: '',
      ivaIncluido: false,
      baseImponible: '',
      cuotaIva: '',
      metodoPago: 'EFECTIVO',
      recurrente: false,
      diaRecurrencia: '',
      notas: '',
      pagado: false,
    });
    setEditandoGasto(null);
    setModalAbierto(true);
  };

  const abrirModalEditar = (gasto) => {
    setFormData(gasto);
    setEditandoGasto(gasto.id);
    setModalAbierto(true);
  };

  const cerrarModal = () => {
    setModalAbierto(false);
    setEditandoGasto(null);
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const guardarGasto = async (e) => {
    e.preventDefault();

    if (!formData.concepto || !formData.importe) {
      alert('Por favor completa los campos obligatorios');
      return;
    }

    try {
      if (editandoGasto) {
        await gastoService.update(editandoGasto, formData);
        alert('Gasto actualizado correctamente');
      } else {
        await gastoService.create(formData);
        alert('Gasto creado correctamente');
      }
      cerrarModal();
      cargarGastos();
    } catch (err) {
      alert('Error al guardar gasto: ' + err.message);
      console.error(err);
    }
  };

  const eliminarGasto = async (id) => {
    if (!window.confirm('¿Estás seguro de que deseas eliminar este gasto?')) {
      return;
    }

    try {
      await gastoService.delete(id);
      alert('Gasto eliminado correctamente');
      cargarGastos();
    } catch (err) {
      alert('Error al eliminar gasto: ' + err.message);
      console.error(err);
    }
  };

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

  const formatearMoneda = (cantidad) => {
    if (!cantidad) return '€0,00';
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
    }).format(parseFloat(cantidad));
  };

  const totalGastos = gastos.reduce((sum, gasto) => sum + (parseFloat(gasto.importe) || 0), 0);

  const getColorCategoria = (categoria) => {
    const colores = {
      'MANTENIMIENTO': 'bg-blue-100 text-blue-800',
      'SUMINISTROS': 'bg-green-100 text-green-800',
      'PERSONAL': 'bg-purple-100 text-purple-800',
      'SERVICIOS': 'bg-yellow-100 text-yellow-800',
      'TRANSPORTE': 'bg-orange-100 text-orange-800',
      'OTROS': 'bg-gray-100 text-gray-800',
    };
    return colores[categoria] || 'bg-gray-100 text-gray-800';
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-6">
        <div className="flex items-center gap-3">
          <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <img src="/assets/icons/invoice.png" alt="Gastos" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
          </div>
          <div>
            <h2 className="text-2xl font-bold">Gastos</h2>
            <p className="text-gray-500 text-sm mt-1">
              Total: {formatearMoneda(totalGastos)}
            </p>
          </div>
        </div>
        <button
          onClick={abrirModalNuevo}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded font-semibold flex items-center gap-2"
        >
          <span>+</span> Nuevo Gasto
        </button>
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-8">
          <p className="text-gray-500">Cargando gastos...</p>
        </div>
      ) : gastos.length === 0 ? (
        <div className="text-center py-8">
          <p className="text-gray-500">No hay gastos registrados</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-gray-100 border-b">
                <th className="px-4 py-2 text-left font-semibold">Concepto</th>
                <th className="px-4 py-2 text-left font-semibold">Fecha</th>
                <th className="px-4 py-2 text-left font-semibold">Categoría</th>
                <th className="px-4 py-2 text-left font-semibold">Método Pago</th>
                <th className="px-4 py-2 text-right font-semibold">Importe</th>
                <th className="px-4 py-2 text-center font-semibold">Pagado</th>
                <th className="px-4 py-2 text-center font-semibold">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {gastos.map(gasto => (
                <tr key={gasto.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-3 font-semibold">{gasto.concepto}</td>
                  <td className="px-4 py-3">{formatearFecha(gasto.fecha)}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 rounded text-xs font-semibold ${getColorCategoria(gasto.categoria)}`}>
                      {gasto.categoria}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm">{gasto.metodoPago}</td>
                  <td className="px-4 py-3 text-right font-semibold text-red-600">
                    {formatearMoneda(gasto.importe)}
                  </td>
                  <td className="px-4 py-3 text-center">
                    <span className={`px-2 py-1 rounded text-xs font-semibold ${gasto.pagado
                      ? 'bg-green-100 text-green-800'
                      : 'bg-yellow-100 text-yellow-800'
                      }`}>
                      {gasto.pagado ? 'Pagado' : 'Pendiente'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-center space-x-2">
                    <button
                      onClick={() => abrirModalEditar(gasto)}
                      className="text-blue-600 hover:text-blue-800 font-semibold text-sm"
                    >
                      Editar
                    </button>
                    <button
                      onClick={() => eliminarGasto(gasto.id)}
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

      {/* Modal */}
      {modalAbierto && (
        <div className="fixed inset-0 bg-blue-900 bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-4xl w-full max-h-[92vh] flex flex-col">
            <div className="px-8 py-5 border-b border-gray-200 bg-gradient-to-r from-purple-50 to-purple-100">
              <h3 className="text-2xl font-bold text-gray-900">
                {editandoGasto ? 'Editar Gasto' : 'Nuevo Gasto'}
              </h3>
            </div>

            <div className="px-8 py-6 overflow-y-auto flex-1">
              <form onSubmit={guardarGasto} className="space-y-6">
                <div>
                  <h4 className="text-lg font-semibold text-gray-800 mb-3 pb-2 border-b border-gray-200">
                    Información del Gasto
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="md:col-span-2">
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Concepto*
                      </label>
                      <input
                        type="text"
                        name="concepto"
                        value={formData.concepto}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                        placeholder="Descripción del gasto"
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
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Categoría
                      </label>
                      <select
                        name="categoria"
                        value={formData.categoria}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                      >
                        <option value="MANTENIMIENTO">Mantenimiento</option>
                        <option value="SUMINISTROS">Suministros</option>
                        <option value="PERSONAL">Personal</option>
                        <option value="SERVICIOS">Servicios</option>
                        <option value="TRANSPORTE">Transporte</option>
                        <option value="OTROS">Otros</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Importe*
                      </label>
                      <input
                        type="number"
                        name="importe"
                        value={formData.importe}
                        onChange={handleInputChange}
                        step="0.01"
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                        placeholder="0,00"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Método Pago
                      </label>
                      <select
                        name="metodoPago"
                        value={formData.metodoPago}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                      >
                        <option value="EFECTIVO">Efectivo</option>
                        <option value="TARJETA">Tarjeta</option>
                        <option value="TRANSFERENCIA">Transferencia</option>
                        <option value="CHEQUE">Cheque</option>
                      </select>
                    </div>
                    <div className="flex items-center gap-6 md:col-span-2">
                      <label className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="checkbox"
                          name="pagado"
                          checked={formData.pagado}
                          onChange={handleInputChange}
                          className="w-5 h-5 rounded"
                        />
                        <span className="text-sm font-semibold text-gray-700">Pagado</span>
                      </label>
                      <label className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="checkbox"
                          name="recurrente"
                          checked={formData.recurrente}
                          onChange={handleInputChange}
                          className="w-5 h-5 rounded"
                        />
                        <span className="text-sm font-semibold text-gray-700">Recurrente</span>
                      </label>
                    </div>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">
                    Notas
                  </label>
                  <textarea
                    name="notas"
                    value={formData.notas}
                    onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                    rows="3"
                    placeholder="Notas adicionales"
                  />
                </div>
              </form>
            </div>

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
                onClick={guardarGasto}
                className="px-6 py-2.5 bg-purple-600 hover:bg-purple-700 text-white rounded-lg font-semibold transition-colors shadow-md hover:shadow-lg"
              >
                {editandoGasto ? 'Actualizar Gasto' : 'Crear Gasto'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Gastos;