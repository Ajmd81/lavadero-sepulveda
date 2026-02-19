import { useState, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Search, Plus, Edit, Trash2, ChevronLeft, ChevronRight } from 'lucide-react';
import proveedorService from '../../services/proveedorService';
import * as XLSX from 'xlsx';

const Proveedores = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedProveedor, setSelectedProveedor] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const queryClient = useQueryClient();

  const { data: proveedoresData, isLoading } = useQuery({
    queryKey: ['proveedores', currentPage, pageSize],
    queryFn: () => proveedorService.getAllPaginated(currentPage, pageSize),
  });

  // Extraer datos de la respuesta paginada
  const proveedores = proveedoresData?.data?.content || [];
  const totalPages = proveedoresData?.data?.totalPages || 0;
  const totalItems = proveedoresData?.data?.totalItems || 0;

  const deleteMutation = useMutation({
    mutationFn: (id) => proveedorService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['proveedores']);
      alert('Proveedor eliminado correctamente');
    },
  });

  const filteredProveedores = proveedores.filter(proveedor =>
    proveedor.nombre?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    proveedor.telefono?.includes(searchTerm) ||
    proveedor.email?.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  const handlePageSizeChange = (e) => {
    setPageSize(Number(e.target.value));
    setCurrentPage(0); // Reset a la primera página
  };

  // ── Importar desde Excel ──
  const fileInputRef = useRef(null);

  const handleImportExcel = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    e.target.value = '';

    try {
      const buffer = await file.arrayBuffer();
      const workbook = XLSX.read(buffer, { type: 'array' });
      const sheet = workbook.Sheets[workbook.SheetNames[0]];
      const rows = XLSX.utils.sheet_to_json(sheet);

      if (rows.length === 0) {
        alert('El archivo Excel está vacío');
        return;
      }

      let creados = 0;
      let errores = [];

      for (const [idx, row] of rows.entries()) {
        try {
          const datos = {
            nombre: row.nombre || '',
            nif: row.nif || '',
            direccion: row.direccion || '',
            telefono: row.telefono ? String(row.telefono) : '',
            email: row.email || '',
            contacto: row.contacto || '',
            web: row.web || '',
            notas: row.notas || '',
            activo: row.activo !== false && row.activo !== 'false' && row.activo !== 'no',
          };

          await proveedorService.create(datos);
          creados++;
        } catch (err) {
          errores.push(`Fila ${idx + 2}: ${err.response?.data?.message || err.message}`);
        }
      }

      queryClient.invalidateQueries(['proveedores']);
      alert(`✅ Importación completada:\n- ${creados} proveedores creados\n${errores.length > 0 ? `- ${errores.length} errores:\n${errores.join('\n')}` : '- Sin errores'}`);
    } catch (err) {
      console.error('Error importando Excel:', err);
      alert('Error al leer el archivo Excel');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between flex-wrap gap-4">
          <div className="flex items-center gap-3">
            <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <img src="/assets/icons/proveedor.png" alt="Proveedores" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Gestión de Proveedores</h1>
              <p className="text-gray-600">Total: {totalItems} proveedores</p>
            </div>
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => fileInputRef.current?.click()}
              className="flex items-center gap-2 bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors"
            >
              📥 Importar Excel
            </button>
            <input
              type="file"
              ref={fileInputRef}
              accept=".xlsx,.xls"
              onChange={handleImportExcel}
              className="hidden"
            />
            <button
              onClick={() => { setSelectedProveedor(null); setShowModal(true); }}
              className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Plus size={20} />
              Nuevo Proveedor
            </button>
          </div>
        </div>
      </div>

      {/* Barra de búsqueda */}
      <div className="bg-white rounded-lg shadow p-4">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Buscar proveedores por nombre, teléfono o email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>
      </div>

      {/* Tabla de Proveedores */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
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
                  <td colSpan="7" className="px-6 py-8 text-center">
                    <div className="flex items-center justify-center">
                      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                      <span className="ml-3 text-gray-500">Cargando proveedores...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredProveedores.length === 0 ? (
                <tr>
                  <td colSpan="7" className="px-6 py-8 text-center text-gray-500">
                    {searchTerm ? 'No se encontraron proveedores' : 'No hay proveedores registrados'}
                  </td>
                </tr>
              ) : (
                filteredProveedores.map((proveedor) => (
                  <tr key={proveedor.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="font-medium text-gray-900">{proveedor.nombre}</div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">{proveedor.nif || '—'}</td>
                    <td className="px-6 py-4 text-sm text-gray-600">{proveedor.telefono}</td>
                    <td className="px-6 py-4 text-sm text-gray-600">{proveedor.email}</td>
                    <td className="px-6 py-4 text-sm text-gray-600">{proveedor.categoria || '—'}</td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-1 text-xs rounded ${proveedor.activo
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

        {/* Paginación */}
        {totalPages > 0 && (
          <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
            <div className="flex-1 flex justify-between sm:hidden">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Anterior
              </button>
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage >= totalPages - 1}
                className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Siguiente
              </button>
            </div>
            <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
              <div className="flex items-center gap-4">
                <p className="text-sm text-gray-700">
                  Mostrando <span className="font-medium">{currentPage * pageSize + 1}</span> a{' '}
                  <span className="font-medium">{Math.min((currentPage + 1) * pageSize, totalItems)}</span> de{' '}
                  <span className="font-medium">{totalItems}</span> resultados
                </p>
                <div className="flex items-center gap-2">
                  <label className="text-sm text-gray-700">Por página:</label>
                  <select
                    value={pageSize}
                    onChange={handlePageSizeChange}
                    className="border border-gray-300 rounded-md text-sm px-2 py-1 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value={5}>5</option>
                    <option value={10}>10</option>
                    <option value={25}>25</option>
                    <option value={50}>50</option>
                  </select>
                </div>
              </div>
              <div>
                <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                  <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 0}
                    className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <ChevronLeft size={20} />
                  </button>
                  
                  {[...Array(totalPages)].map((_, index) => {
                    // Mostrar solo páginas relevantes
                    if (
                      index === 0 || // Primera página
                      index === totalPages - 1 || // Última página
                      (index >= currentPage - 1 && index <= currentPage + 1) // Páginas cercanas a la actual
                    ) {
                      return (
                        <button
                          key={index}
                          onClick={() => handlePageChange(index)}
                          className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                            currentPage === index
                              ? 'z-10 bg-blue-600 border-blue-600 text-white'
                              : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                          }`}
                        >
                          {index + 1}
                        </button>
                      );
                    } else if (
                      index === currentPage - 2 ||
                      index === currentPage + 2
                    ) {
                      return (
                        <span key={index} className="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700">
                          ...
                        </span>
                      );
                    }
                    return null;
                  })}
                  
                  <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage >= totalPages - 1}
                    className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <ChevronRight size={20} />
                  </button>
                </nav>
              </div>
            </div>
          </div>
        )}
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
      <div className="bg-white rounded-lg p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto">
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