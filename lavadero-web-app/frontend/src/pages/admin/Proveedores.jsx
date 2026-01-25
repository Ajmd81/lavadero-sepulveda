import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Search, Plus, Edit, Trash2, Eye } from 'lucide-react';
import proveedorService from '../../services/proveedorService';

const Proveedores = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedProveedor, setSelectedProveedor] = useState(null);
  const queryClient = useQueryClient();

  const { data: proveedores, isLoading } = useQuery({
    queryKey: ['proveedores'],
    queryFn: () => proveedorService.getAll(),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => proveedorService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['proveedores']);
      alert('Proveedor eliminado correctamente');
    },
  });

  const filteredProveedores = proveedores?.data?.filter(proveedor =>
    proveedor.nombre?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    proveedor.telefono?.includes(searchTerm) ||
    proveedor.email?.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div className="flex-1 max-w-md">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
            <input
              type="text"
              placeholder="Buscar proveedores..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10 pr-4 py-2 w-full border rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
        <button
          onClick={() => { setSelectedProveedor(null); setShowModal(true); }}
          className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
        >
          <Plus size={20} />
          Nuevo Proveedor
        </button>
      </div>

      {/* Tabla de Proveedores */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nombre</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">NIF/CIF</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Teléfono</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Categoría</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Estado</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Acciones</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {isLoading ? (
              <tr>
                <td colSpan="7" className="px-6 py-4 text-center text-gray-500">
                  Cargando proveedores...
                </td>
              </tr>
            ) : filteredProveedores.length === 0 ? (
              <tr>
                <td colSpan="7" className="px-6 py-4 text-center text-gray-500">
                  No se encontraron proveedores
                </td>
              </tr>
            ) : (
              filteredProveedores.map((proveedor) => (
                <tr key={proveedor.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="font-medium text-gray-900">{proveedor.nombre}</div>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-600">{proveedor.nif || '—'}</td>
                  <td className="px-6 py-4 text-sm text-gray-600">{proveedor.telefono}</td>
                  <td className="px-6 py-4 text-sm text-gray-600">{proveedor.email}</td>
                  <td className="px-6 py-4 text-sm text-gray-600">{proveedor.categoria || '—'}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 text-xs rounded ${
                      proveedor.activo
                        ? 'bg-green-100 text-green-800'
                        : 'bg-red-100 text-red-800'
                    }`}>
                      {proveedor.activo ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right text-sm font-medium space-x-2">
                    <button
                      onClick={() => { setSelectedProveedor(proveedor); setShowModal(true); }}
                      className="text-blue-600 hover:text-blue-800"
                    >
                      <Edit size={18} />
                    </button>
                    <button
                      onClick={() => {
                        if (window.confirm('¿Eliminar este proveedor?')) {
                          deleteMutation.mutate(proveedor.id);
                        }
                      }}
                      className="text-red-600 hover:text-red-800"
                    >
                      <Trash2 size={18} />
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Modal para crear/editar */}
      {showModal && (
        <ProveedorModal
          proveedor={selectedProveedor}
          onClose={() => setShowModal(false)}
          onSuccess={() => {
            queryClient.invalidateQueries(['proveedores']);
            setShowModal(false);
          }}
        />
      )}
    </div>
  );
};

const ProveedorModal = ({ proveedor, onClose, onSuccess }) => {
  const [formData, setFormData] = useState(proveedor || {
    nombre: '',
    nif: '',
    telefono: '',
    email: '',
    direccion: '',
    ciudad: '',
    codigoPostal: '',
    contacto: '',
    categoria: '',
    formaPago: 'TRANSFERENCIA',
    activo: true,
  });

  const createMutation = useMutation({
    mutationFn: (data) => proveedorService.create(data),
    onSuccess: () => {
      alert('Proveedor creado correctamente');
      onSuccess();
    },
    onError: (err) => alert(`Error: ${err.message}`),
  });

  const updateMutation = useMutation({
    mutationFn: (data) => proveedorService.update(proveedor.id, data),
    onSuccess: () => {
      alert('Proveedor actualizado correctamente');
      onSuccess();
    },
    onError: (err) => alert(`Error: ${err.message}`),
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!formData.nombre || !formData.email) {
      alert('Nombre y email son obligatorios');
      return;
    }
    if (proveedor) {
      updateMutation.mutate(formData);
    } else {
      createMutation.mutate(formData);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 max-w-2xl w-full max-h-96 overflow-y-auto">
        <h2 className="text-xl font-bold mb-4">
          {proveedor ? 'Editar Proveedor' : 'Nuevo Proveedor'}
        </h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold mb-1">Nombre*</label>
              <input
                type="text"
                value={formData.nombre}
                onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                className="w-full border rounded px-3 py-2 text-sm"
                placeholder="Nombre proveedor"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold mb-1">NIF/CIF</label>
              <input
                type="text"
                value={formData.nif}
                onChange={(e) => setFormData({ ...formData, nif: e.target.value })}
                className="w-full border rounded px-3 py-2 text-sm"
                placeholder="NIF/CIF"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold mb-1">Email*</label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="w-full border rounded px-3 py-2 text-sm"
                placeholder="email@ejemplo.com"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold mb-1">Teléfono</label>
              <input
                type="tel"
                value={formData.telefono}
                onChange={(e) => setFormData({ ...formData, telefono: e.target.value })}
                className="w-full border rounded px-3 py-2 text-sm"
                placeholder="600000000"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold mb-1">Contacto</label>
              <input
                type="text"
                value={formData.contacto}
                onChange={(e) => setFormData({ ...formData, contacto: e.target.value })}
                className="w-full border rounded px-3 py-2 text-sm"
                placeholder="Nombre contacto"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold mb-1">Categoría</label>
              <select
                value={formData.categoria}
                onChange={(e) => setFormData({ ...formData, categoria: e.target.value })}
                className="w-full border rounded px-3 py-2 text-sm"
              >
                <option value="">Seleccionar categoría</option>
                <option value="Suministros">Suministros</option>
                <option value="Servicios">Servicios</option>
                <option value="Mantenimiento">Mantenimiento</option>
                <option value="Equipamiento">Equipamiento</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-semibold mb-1">Dirección</label>
              <input
                type="text"
                value={formData.direccion}
                onChange={(e) => setFormData({ ...formData, direccion: e.target.value })}
                className="w-full border rounded px-3 py-2 text-sm"
                placeholder="Dirección"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold mb-1">Ciudad</label>
              <input
                type="text"
                value={formData.ciudad}
                onChange={(e) => setFormData({ ...formData, ciudad: e.target.value })}
                className="w-full border rounded px-3 py-2 text-sm"
                placeholder="Ciudad"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold mb-1">Código Postal</label>
              <input
                type="text"
                value={formData.codigoPostal}
                onChange={(e) => setFormData({ ...formData, codigoPostal: e.target.value })}
                className="w-full border rounded px-3 py-2 text-sm"
                placeholder="CP"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold mb-1">Forma de Pago</label>
              <select
                value={formData.formaPago}
                onChange={(e) => setFormData({ ...formData, formaPago: e.target.value })}
                className="w-full border rounded px-3 py-2 text-sm"
              >
                <option value="TRANSFERENCIA">Transferencia</option>
                <option value="CHEQUE">Cheque</option>
                <option value="EFECTIVO">Efectivo</option>
                <option value="TARJETA">Tarjeta</option>
              </select>
            </div>
            <div className="col-span-2">
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={formData.activo}
                  onChange={(e) => setFormData({ ...formData, activo: e.target.checked })}
                  className="rounded"
                />
                <span className="text-sm font-semibold">Activo</span>
              </label>
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 border rounded font-semibold hover:bg-gray-100"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-blue-600 text-white rounded font-semibold hover:bg-blue-700"
            >
              {proveedor ? 'Actualizar' : 'Crear'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Proveedores;
