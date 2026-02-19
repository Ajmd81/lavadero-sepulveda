import { useState, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Search, Plus, Edit, Trash2, X, Phone, Mail, AlertCircle, CheckCircle } from 'lucide-react';
import clienteService from '../../services/clienteService';
import * as XLSX from 'xlsx';

const Clientes = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedCliente, setSelectedCliente] = useState(null);
  const [mensaje, setMensaje] = useState(null);
  
  // Estados para paginación
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [pageSize, setPageSize] = useState(50);
  
  const queryClient = useQueryClient();

  const [formulario, setFormulario] = useState({
    nombre: '',
    apellidos: '',
    nif: '',
    telefono: '',
    email: '',
    direccion: '',
    ciudad: '',
    codigoPostal: '',
    vehiculoHabitual: '',
    notas: '',
    activo: true
  });

  const { data: clientesData, isLoading } = useQuery({
    queryKey: ['clientes', currentPage, pageSize],
    queryFn: async () => {
      const response = await clienteService.getAllPaginated(currentPage, pageSize);
      const data = response.data;
      
      // Asegurarse de que los valores siempre tienen un default
      const totalItems = data?.totalItems || 0;
      const totalPages = data?.totalPages || 0;
      
      setTotalPages(totalPages);
      setTotalItems(totalItems);
      
      if (data?.content && Array.isArray(data.content)) {
        return data.content;
      }
      return [];
    },
  });

  const clientes = clientesData || [];

  const guardarMutation = useMutation({
    mutationFn: async (cliente) => {
      if (selectedCliente) {
        return await clienteService.update(selectedCliente.id, cliente);
      } else {
        return await clienteService.create(cliente);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['clientes']);
      cerrarModal();
      setMensaje({ tipo: 'exito', texto: selectedCliente ? 'Cliente actualizado correctamente' : 'Cliente creado correctamente' });
      setTimeout(() => setMensaje(null), 3000);
    },
    onError: (error) => {
      setMensaje({ tipo: 'error', texto: error.response?.data?.mensaje || 'Error al guardar el cliente' });
      setTimeout(() => setMensaje(null), 5000);
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => clienteService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['clientes']);
      setMensaje({ tipo: 'exito', texto: 'Cliente eliminado correctamente' });
      setTimeout(() => setMensaje(null), 3000);
    },
    onError: (error) => {
      setMensaje({ tipo: 'error', texto: error.response?.data?.mensaje || 'Error al eliminar el cliente' });
      setTimeout(() => setMensaje(null), 5000);
    }
  });

  const filteredClientes = clientes.filter(cliente =>
    cliente.nombre?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    cliente.apellidos?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    cliente.telefono?.includes(searchTerm) ||
    cliente.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    cliente.nif?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const abrirModalNuevo = () => {
    setSelectedCliente(null);
    setFormulario({
      nombre: '',
      apellidos: '',
      nif: '',
      telefono: '',
      email: '',
      direccion: '',
      ciudad: '',
      codigoPostal: '',
      vehiculoHabitual: '',
      notas: '',
      activo: true
    });
    setShowModal(true);
  };

  const abrirModalEditar = (cliente) => {
    setSelectedCliente(cliente);
    setFormulario({
      nombre: cliente.nombre || '',
      apellidos: cliente.apellidos || '',
      nif: cliente.nif || '',
      telefono: cliente.telefono || '',
      email: cliente.email || '',
      direccion: cliente.direccion || '',
      ciudad: cliente.ciudad || '',
      codigoPostal: cliente.codigoPostal || '',
      vehiculoHabitual: cliente.vehiculoHabitual || '',
      notas: cliente.notas || '',
      activo: cliente.activo !== undefined ? cliente.activo : true
    });
    setShowModal(true);
  };

  const cerrarModal = () => {
    setShowModal(false);
    setSelectedCliente(null);
    setFormulario({
      nombre: '',
      apellidos: '',
      nif: '',
      telefono: '',
      email: '',
      direccion: '',
      ciudad: '',
      codigoPostal: '',
      vehiculoHabitual: '',
      notas: '',
      activo: true
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    guardarMutation.mutate(formulario);
  };

  const handleEliminar = (id) => {
    if (window.confirm('¿Está seguro de que desea eliminar este cliente?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormulario(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  // Funciones de paginación estilo Citas
  const irAPagina = (pagina) => {
    if (pagina >= 0 && pagina < totalPages) {
      setCurrentPage(pagina);
    }
  };

  const cambiarTamanoPagina = (nuevoTamano) => {
    setPageSize(Number(nuevoTamano));
    setCurrentPage(0);
  };

  const getPaginaInicio = () => {
    const start = currentPage * Number(pageSize) + 1;
    return isNaN(start) ? 0 : start;
  };
  const getPaginaFin = () => {
    const end = Math.min((currentPage + 1) * Number(pageSize), Number(totalItems) || 0);
    return isNaN(end) ? 0 : end;
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
            apellidos: row.apellidos || '',
            nif: row.nif || '',
            telefono: row.telefono ? String(row.telefono) : '',
            email: row.email || '',
            direccion: row.direccion || '',
            ciudad: row.ciudad || '',
            codigoPostal: row.codigoPostal ? String(row.codigoPostal) : '',
            vehiculoHabitual: row.vehiculoHabitual || '',
            notas: row.notas || '',
            activo: row.activo !== false && row.activo !== 'false' && row.activo !== 'no',
          };

          await clienteService.create(datos);
          creados++;
        } catch (err) {
          errores.push(`Fila ${idx + 2}: ${err.response?.data?.message || err.message}`);
        }
      }

      queryClient.invalidateQueries(['clientes']);
      alert(`✅ Importación completada:\n- ${creados} clientes creados\n${errores.length > 0 ? `- ${errores.length} errores:\n${errores.join('\n')}` : '- Sin errores'}`);
    } catch (err) {
      console.error('Error importando Excel:', err);
      alert('Error al leer el archivo Excel');
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between flex-wrap gap-4">
          <div className="flex items-center gap-3">
            <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <img src="/assets/icons/cliente.png" alt="Clientes" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Gestión de Clientes</h1>
              <p className="text-base text-gray-600">Total: <span className="font-semibold text-blue-600">{totalItems}</span> clientes</p>
            </div>
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => fileInputRef.current?.click()}
              className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-base font-medium"
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
            <button onClick={abrirModalNuevo} className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-base font-medium">
              + Nuevo Cliente
            </button>
          </div>
        </div>
      </div>

      {mensaje && (
        <div className={`rounded-lg p-4 flex items-center justify-between ${mensaje.tipo === 'exito' ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'}`}>
          <div className="flex items-center">
            {mensaje.tipo === 'exito' ? <CheckCircle className="text-green-600 mr-2" size={20} /> : <AlertCircle className="text-red-600 mr-2" size={20} />}
            <span className={mensaje.tipo === 'exito' ? 'text-green-800' : 'text-red-800'}>{mensaje.texto}</span>
          </div>
          <button onClick={() => setMensaje(null)}>
            <X size={20} className="text-gray-500" />
          </button>
        </div>
      )}

      <div className="bg-white rounded-lg shadow p-4">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
          <input type="text" placeholder="Buscar por nombre, DNI, teléfono o email..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
        </div>
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        {isLoading ? (
          <div className="text-center py-8">
            <div className="flex items-center justify-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <span className="ml-3 text-gray-500 text-base">Cargando clientes...</span>
            </div>
          </div>
        ) : filteredClientes.length === 0 ? (
          <div className="text-center py-8 text-gray-500 text-base">
            {searchTerm ? 'No se encontraron clientes' : 'No hay clientes registrados'}
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-100">
                  <tr>
                    <th className="border border-gray-300 px-4 py-3 text-left text-sm font-semibold">Cliente</th>
                    <th className="border border-gray-300 px-4 py-3 text-left text-sm font-semibold">DNI</th>
                    <th className="border border-gray-300 px-4 py-3 text-left text-sm font-semibold">Contacto</th>
                    <th className="border border-gray-300 px-4 py-3 text-left text-sm font-semibold">Vehículo</th>
                    <th className="border border-gray-300 px-4 py-3 text-left text-sm font-semibold">Estado</th>
                    <th className="border border-gray-300 px-4 py-3 text-center text-sm font-semibold">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredClientes.map((cliente) => (
                    <tr key={cliente.id} className="hover:bg-gray-50">
                      <td className="border border-gray-300 px-4 py-3 text-base">
                        <div className="font-medium text-gray-900">{cliente.nombre} {cliente.apellidos}</div>
                        {cliente.ciudad && <div className="text-sm text-gray-500">{cliente.ciudad}</div>}
                      </td>
                      <td className="border border-gray-300 px-4 py-3 text-base">{cliente.nif || '-'}</td>
                      <td className="border border-gray-300 px-4 py-3 text-base">
                        <div className="flex flex-col gap-1">
                          {cliente.telefono && (
                            <div className="flex items-center text-sm text-gray-600">
                              <Phone size={14} className="mr-1 flex-shrink-0" />
                              <span>{cliente.telefono}</span>
                            </div>
                          )}
                          {cliente.email && (
                            <div className="flex items-center text-sm text-gray-600">
                              <Mail size={14} className="mr-1 flex-shrink-0" />
                              <span className="truncate max-w-xs">{cliente.email}</span>
                            </div>
                          )}
                        </div>
                      </td>
                      <td className="border border-gray-300 px-4 py-3 text-base">{cliente.vehiculoHabitual || '-'}</td>
                      <td className="border border-gray-300 px-4 py-3 text-base">
                        <span className={`px-3 py-1 rounded text-sm font-medium ${cliente.activo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                          {cliente.activo ? 'Activo' : 'Inactivo'}
                        </span>
                      </td>
                      <td className="border border-gray-300 px-4 py-3 text-center text-base">
                        <div className="flex items-center justify-center gap-2">
                          <button onClick={() => abrirModalEditar(cliente)} className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded text-base font-medium">
                            Editar
                          </button>
                          <button onClick={() => handleEliminar(cliente.id)} className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded text-base font-medium">
                            Eliminar
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Paginación estilo Citas */}
            <div className="bg-gray-50 px-4 py-3 border-t border-gray-200 sm:px-6">
              <div className="flex items-center justify-between flex-wrap gap-4">
                <div className="flex items-center gap-4">
                  <p className="text-base text-gray-700">
                    Mostrando <span className="font-medium">{getPaginaInicio()}</span> a <span className="font-medium">{getPaginaFin()}</span> de{' '}
                    <span className="font-medium">{totalItems}</span> resultados
                  </p>

                  <div className="flex items-center gap-2">
                    <label htmlFor="pageSize" className="text-base text-gray-700">
                      Por página:
                    </label>
                    <select
                      id="pageSize"
                      value={pageSize}
                      onChange={(e) => cambiarTamanoPagina(Number(e.target.value))}
                      className="border border-gray-300 rounded px-3 py-2 text-base"
                    >
                      <option value={5}>5</option>
                      <option value={10}>10</option>
                      <option value={20}>20</option>
                      <option value={50}>50</option>
                    </select>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => irAPagina(0)}
                    disabled={currentPage === 0}
                    className={`px-4 py-2 rounded text-base ${currentPage === 0
                        ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                        : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
                      }`}
                  >
                    Primera
                  </button>
                  <button
                    onClick={() => irAPagina(currentPage - 1)}
                    disabled={currentPage === 0}
                    className={`px-4 py-2 rounded text-base ${currentPage === 0
                        ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                        : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
                      }`}
                  >
                    Anterior
                  </button>

                  <div className="flex gap-1">
                    {[...Array(totalPages)].map((_, index) => {
                      if (
                        index === 0 ||
                        index === totalPages - 1 ||
                        (index >= currentPage - 2 && index <= currentPage + 2)
                      ) {
                        return (
                          <button
                            key={index}
                            onClick={() => irAPagina(index)}
                            className={`px-3 py-2 rounded text-base ${currentPage === index
                                ? 'bg-blue-600 text-white'
                                : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
                              }`}
                          >
                            {index + 1}
                          </button>
                        );
                      } else if (index === currentPage - 3 || index === currentPage + 3) {
                        return <span key={index} className="px-2">...</span>;
                      }
                      return null;
                    })}
                  </div>

                  <button
                    onClick={() => irAPagina(currentPage + 1)}
                    disabled={currentPage === totalPages - 1}
                    className={`px-4 py-2 rounded text-base ${currentPage === totalPages - 1
                        ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                        : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
                      }`}
                  >
                    Siguiente
                  </button>
                  <button
                    onClick={() => irAPagina(totalPages - 1)}
                    disabled={currentPage === totalPages - 1}
                    className={`px-4 py-2 rounded text-base ${currentPage === totalPages - 1
                        ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                        : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
                      }`}
                  >
                    Última
                  </button>
                </div>
              </div>
            </div>
          </>
        )}
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between p-6 border-b border-gray-200 sticky top-0 bg-white z-10">
              <div className="flex items-center gap-3">
                <div style={{ width: 32, height: 32, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <img src="/assets/icons/cliente.png" alt="Cliente" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                </div>
                <h2 className="text-2xl font-bold text-gray-900">{selectedCliente ? 'Editar Cliente' : 'Nuevo Cliente'}</h2>
              </div>
              <button onClick={cerrarModal} className="text-gray-400 hover:text-gray-600 transition-colors">
                <X size={24} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-6">
              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Información Personal</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Nombre *</label>
                    <input type="text" name="nombre" value={formulario.nombre} onChange={handleChange} required className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Apellidos *</label>
                    <input type="text" name="apellidos" value={formulario.apellidos} onChange={handleChange} required className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">DNI/NIE</label>
                    <input type="text" name="nif" value={formulario.nif} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono *</label>
                    <input type="tel" name="telefono" value={formulario.telefono} onChange={handleChange} required className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                    <input type="email" name="email" value={formulario.email} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                  </div>
                </div>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Dirección</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">Dirección</label>
                    <input type="text" name="direccion" value={formulario.direccion} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Ciudad</label>
                    <input type="text" name="ciudad" value={formulario.ciudad} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Código Postal</label>
                    <input type="text" name="codigoPostal" value={formulario.codigoPostal} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                  </div>
                </div>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Información del Vehículo</h3>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Vehículo Habitual</label>
                  <input type="text" name="vehiculoHabitual" value={formulario.vehiculoHabitual} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                </div>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Notas</h3>
                <textarea name="notas" value={formulario.notas} onChange={handleChange} rows="4" className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>

              <div>
                <label className="flex items-center gap-2">
                  <input type="checkbox" name="activo" checked={formulario.activo} onChange={handleChange} className="w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500" />
                  <span className="text-sm font-medium text-gray-700">Cliente activo</span>
                </label>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
                <button type="button" onClick={cerrarModal} className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors">
                  Cancelar
                </button>
                <button type="submit" disabled={guardarMutation.isPending} className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2">
                  {guardarMutation.isPending ? (
                    <>
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                      <span>Guardando...</span>
                    </>
                  ) : (
                    <span>{selectedCliente ? 'Actualizar' : 'Crear'}</span>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Clientes;