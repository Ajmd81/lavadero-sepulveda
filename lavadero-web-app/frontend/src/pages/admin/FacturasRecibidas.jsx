import { useState, useEffect } from 'react';
import facturaRecibidaService from '../../services/facturaRecibidaService';
import proveedorService from '../../services/proveedorService';
import enumsService from '../../services/enumsService';

const FacturasRecibidas = () => {
  const [facturas, setFacturas] = useState([]);
  const [proveedores, setProveedores] = useState([]);
  const [metodosPago, setMetodosPago] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [editandoFactura, setEditandoFactura] = useState(null);
  const [formData, setFormData] = useState({
    numeroFactura: '',
    proveedorId: '',
    proveedorNombre: '',
    proveedorNif: '',
    fechaFactura: '',
    fechaVencimiento: '',
    fechaPago: '',
    categoria: 'SUMINISTROS',
    concepto: '',
    lineas: [],
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

  const [nuevaLinea, setNuevaLinea] = useState({
    concepto: '',
    cantidad: 1,
    precioUnitario: '',
    tipoIva: '21',
  });

  useEffect(() => {
    cargarMetodosPago();
    cargarCategorias();
    cargarFacturas();
    cargarProveedores();
  }, []);

  const cargarFacturas = async () => {
    setLoading(true);
    try {
      const response = await facturaRecibidaService.getAll();
      let facturasData = response.data || [];

      // Si es un objeto con content (paginado), extraer content
      if (facturasData && facturasData.content && Array.isArray(facturasData.content)) {
        facturasData = facturasData.content;
      }

      // Validar que sea un array
      if (!Array.isArray(facturasData)) {
        console.warn('⚠️ response.data no es un array:', typeof facturasData);
        facturasData = [];
      }

      facturasData = facturasData.sort((a, b) => {
        let fechaA = a.fechaFactura;
        let fechaB = b.fechaFactura;

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

  const cargarMetodosPago = async () => {
    try {
      const response = await enumsService.obtenerMetodosPago();
      if (response.data && Array.isArray(response.data)) {
        setMetodosPago(response.data);
      }
    } catch (err) {
      console.error('Error al cargar métodos de pago:', err);
      // Fallback
      setMetodosPago([
        { valor: 'TRANSFERENCIA', descripcion: 'Transferencia' },
        { valor: 'EFECTIVO', descripcion: 'Efectivo' },
        { valor: 'TARJETA', descripcion: 'Tarjeta' },
        { valor: 'BIZUM', descripcion: 'Bizum' },
        { valor: 'CHEQUE', descripcion: 'Cheque' },
        { valor: 'DOMICILIACION', descripcion: 'Domiciliación' },
      ]);
    }
  };

  const cargarCategorias = async () => {
    try {
      const response = await enumsService.obtenerCategoriasGasto();
      if (response.data && Array.isArray(response.data)) {
        setCategorias(response.data);
      }
    } catch (err) {
      console.error('Error al cargar categorías:', err);
      // Fallback a categorías hardcodeadas
      setCategorias([
        { valor: 'AGUA', descripcion: 'Agua' },
        { valor: 'ALQUILER', descripcion: 'Alquiler' },
        { valor: 'ASOCIACIONES', descripcion: 'Asociaciones' },
        { valor: 'BANCARIOS', descripcion: 'Gastos bancarios' },
        { valor: 'COMBUSTIBLE', descripcion: 'Combustible' },
        { valor: 'GESTORÍA', descripcion: 'Gestoría/Asesoría' },
        { valor: 'IMPUESTOS', descripcion: 'Impuestos/Tasas' },
        { valor: 'LUZ', descripcion: 'Electricidad' },
        { valor: 'MANTENIMIENTO', descripcion: 'Mantenimiento' },
        { valor: 'MAQUINARIA', descripcion: 'Maquinaria/Equipos' },
        { valor: 'MATERIAL_OFICINA', descripcion: 'Material oficina' },
        { valor: 'OTROS', descripcion: 'Otros' },
        { valor: 'PERSONAL', descripcion: 'Personal/Nóminas' },
        { valor: 'PRODUCTOS', descripcion: 'Productos' },
        { valor: 'PUBLICIDAD', descripcion: 'Publicidad/Marketing' },
        { valor: 'REPARACIONES', descripcion: 'Reparaciones' },
        { valor: 'SEGURIDAD_SOCIAL', descripcion: 'Seguridad Social' },
        { valor: 'SEGURIDAD_SOCIAL_A_CARGO_EMPRESA', descripcion: 'Autónomos' },
        { valor: 'SEGUROS', descripcion: 'Seguros' },
        { valor: 'SUMINISTROS', descripcion: 'Productos de limpieza' },
        { valor: 'TELEFONIA', descripcion: 'Telefonía/Internet' },
        { valor: 'VEHICULOS', descripcion: 'Vehículos' },
      ]);
    }
  };

  const cargarProveedores = async () => {
    try {
      const response = await proveedorService.getActivos();
      setProveedores(response.data || []);
    } catch (err) {
      console.error('Error al cargar proveedores:', err);
    }
  };


  const abrirModalNuevo = () => {
    setFormData({
      numeroFactura: '',
      proveedorId: '',
      proveedorNombre: '',
      proveedorNif: '',
      fechaFactura: new Date().toISOString().split('T')[0],
      fechaVencimiento: '',
      fechaPago: '',
      categoria: 'SUMINISTROS',
      concepto: '',
      lineas: [],
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
    setNuevaLinea({
      concepto: '',
      cantidad: 1,
      precioUnitario: '',
      tipoIva: '21',
    });
    setEditandoFactura(null);
    setModalAbierto(true);
  };

  const abrirModalEditar = (factura) => {
    setFormData(factura);
    setNuevaLinea({
      concepto: '',
      cantidad: 1,
      precioUnitario: '',
      tipoIva: '21',
    });
    setEditandoFactura(factura.id);
    setModalAbierto(true);
  };

  const cerrarModal = () => {
    setModalAbierto(false);
    setEditandoFactura(null);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleNuevaLineaChange = (e) => {
    const { name, value } = e.target;
    setNuevaLinea(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const agregarLinea = (e) => {
    e.preventDefault();
    if (!nuevaLinea.concepto || !nuevaLinea.cantidad || !nuevaLinea.precioUnitario) {
      alert('Por favor completa todos los campos de la línea');
      return;
    }
    
    const cantidad = parseFloat(nuevaLinea.cantidad);
    const precioUnitario = parseFloat(nuevaLinea.precioUnitario);
    const subtotal = cantidad * precioUnitario;
    const tipoIvaLinea = parseFloat(nuevaLinea.tipoIva) ?? 21;

    const linea = {
      id: Date.now(),
      concepto: nuevaLinea.concepto,
      cantidad: cantidad,
      precioUnitario: precioUnitario,
      subtotal: subtotal,
      tipoIva: tipoIvaLinea,
    };
    
    const nuevasLineas = [...formData.lineas, linea];
    setFormData(prev => ({
      ...prev,
      lineas: nuevasLineas,
    }));
    recalcularTotales(nuevasLineas);
    
    setNuevaLinea({
      concepto: '',
      cantidad: 1,
      precioUnitario: '',
      tipoIva: '21',
    });
  };

  // Recalcular totales - ahora cada línea tiene su propio IVA
  const recalcularTotales = (lineas) => {
    const baseImponible = lineas.reduce((sum, linea) => sum + linea.subtotal, 0);
    
    // Calcular IVA por línea según su tipoIva
    const cuotaIvaTotal = lineas.reduce((sum, linea) => {
      const tipoIva = linea.tipoIva ?? 21;
      const ivaLinea = (linea.subtotal * tipoIva) / 100;
      return sum + ivaLinea;
    }, 0);
    
    const total = baseImponible + cuotaIvaTotal;

    // Concatenar conceptos para el campo concepto
    const conceptosConcatenados = lineas.map(l => l.concepto).join(' | ');

    setFormData(prev => ({
      ...prev,
      baseImponible: baseImponible.toFixed(2),
      cuotaIva: cuotaIvaTotal.toFixed(2),
      total: total.toFixed(2),
      concepto: conceptosConcatenados,
    }));
  };
  

  const eliminarLinea = (lineaId) => {
    const nuevasLineas = formData.lineas.filter(l => l.id !== lineaId);
    setFormData(prev => ({
      ...prev,
      lineas: nuevasLineas,
    }));
    recalcularTotales(nuevasLineas);
  };

  const handleProveedorChange = (e) => {
    const proveedorId = e.target.value;

    if (!proveedorId) {
      setFormData(prev => ({
        ...prev,
        proveedorId: '',
        proveedorNombre: '',
        proveedorNif: '',
      }));
      return;
    }

    const proveedor = proveedores.find(p => p.id === parseInt(proveedorId));
    if (proveedor) {
      setFormData(prev => ({
        ...prev,
        proveedorId: proveedor.id,
        proveedorNombre: proveedor.nombre,
        proveedorNif: proveedor.nif || '',
      }));
    }
  };

  const formatearFechaParaEnvio = (fecha) => {
    if (!fecha) return '';
    
    // Si ya está en formato dd/MM/yyyy, devolverlo tal cual
    if (typeof fecha === 'string' && fecha.match(/^\d{2}\/\d{2}\/\d{4}$/)) {
      return fecha;
    }
    
    // Si es YYYY-MM-DD (input date), convertir a dd/MM/yyyy
    if (typeof fecha === 'string' && fecha.match(/^\d{4}-\d{2}-\d{2}$/)) {
      const [year, month, day] = fecha.split('-');
      return `${day}/${month}/${year}`;
    }
    
    return '';
  };

  const guardarFactura = async (e) => {
    e.preventDefault();

    if (!formData.numeroFactura || !formData.proveedorNombre) {
      alert('Por favor completa: Número de Factura y Proveedor');
      return;
    }

    if (formData.lineas.length === 0) {
      alert('Por favor agrega al menos una línea de concepto');
      return;
    }

    const baseImponibleNum = parseFloat(formData.baseImponible) || 0;
    const totalNum = parseFloat(formData.total) || 0;

    if (baseImponibleNum <= 0) {
      alert('La Base Imponible debe ser mayor a 0');
      return;
    }

    if (totalNum <= 0) {
      alert('El Total debe ser mayor a 0');
      return;
    }

    try {
      // Formatear fechas correctamente a dd/MM/yyyy
      const fechaFacturaFormato = formatearFechaParaEnvio(formData.fechaFactura);
      if (!fechaFacturaFormato) {
        alert('Por favor ingresa una fecha válida para la factura');
        return;
      }

      // Preparar los datos para enviar al backend
      const datosEnvio = {
        numeroFactura: formData.numeroFactura.trim(),
        proveedorId: formData.proveedorId ? parseInt(formData.proveedorId) : null,
        proveedorNombre: formData.proveedorNombre.trim(),
        proveedorNif: (formData.proveedorNif || '').trim(),
        fechaFactura: fechaFacturaFormato,
        fechaVencimiento: formatearFechaParaEnvio(formData.fechaVencimiento) || '',
        fechaPago: formatearFechaParaEnvio(formData.fechaPago) || '',
        categoria: formData.categoria || 'SUMINISTROS',
        concepto: formData.concepto || '',
        baseImponible: baseImponibleNum,
        tipoIva: parseFloat(formData.tipoIva) ?? 21,
        cuotaIva: parseFloat(formData.cuotaIva) || 0,
        tipoIrpf: parseFloat(formData.tipoIrpf) || 0,
        cuotaIrpf: parseFloat(formData.cuotaIrpf) || 0,
        total: totalNum,
        estado: formData.estado || 'PENDIENTE',
        metodoPago: formData.metodoPago || 'TRANSFERENCIA',
        notas: (formData.notas || '').trim()
      };

      console.log('Enviando datos:', datosEnvio);

      if (editandoFactura) {
        await facturaRecibidaService.update(editandoFactura, datosEnvio);
        alert('Factura actualizada correctamente');
      } else {
        await facturaRecibidaService.create(datosEnvio);
        alert('Factura creada correctamente');
      }
      cerrarModal();
      cargarFacturas();
    } catch (err) {
      console.error('Error completo:', err.response?.data || err.message);
      console.error('Detalles del error:', err.response?.data?.errors || err.response?.data?.message);
      alert('Error al guardar factura: ' + (err.response?.data?.message || err.message));
    }
  };

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

      {/* Modal */}
      {modalAbierto && (
        <div className="fixed inset-0 bg-blue-900 bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-7xl w-full max-h-[95vh] flex flex-col">
            <div className="px-10 py-6 border-b border-gray-200 bg-linear-to-r from-orange-50 to-orange-100">
              <h3 className="text-3xl font-bold text-gray-900">
                {editandoFactura ? 'Editar Factura Recibida' : 'Nueva Factura Recibida'}
              </h3>
            </div>

            <div className="px-10 py-8 overflow-y-auto flex-1">
              <form onSubmit={guardarFactura} className="space-y-8">
                <div>
                  <h4 className="text-2xl font-bold text-gray-900 mb-4 pb-3 border-b-2 border-orange-300">
                    Datos de la Factura
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                    <div>
                      <label className="block text-base font-semibold text-gray-700 mb-2">
                        Nº Factura*
                      </label>
                      <input
                        type="text"
                        name="numeroFactura"
                        value={formData.numeroFactura}
                        onChange={handleInputChange}
                        className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:ring-2 focus:ring-orange-500 focus:border-orange-500 focus:border-transparent"
                        placeholder="Ej: FAC-2026-001"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-base font-semibold text-gray-700 mb-2">
                        Fecha*
                      </label>
                      <input
                        type="date"
                        name="fechaFactura"
                        value={formData.fechaFactura}
                        onChange={handleInputChange}
                        className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-base font-semibold text-gray-700 mb-2">
                        Vencimiento
                      </label>
                      <input
                        type="date"
                        name="fechaVencimiento"
                        value={formData.fechaVencimiento}
                        onChange={handleInputChange}
                        className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                      />
                    </div>
                  </div>
                </div>

                <div>
                  <h4 className="text-2xl font-bold text-gray-900 mb-4 pb-3 border-b-2 border-orange-300">
                    Datos del Proveedor
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                    <div className="md:col-span-2">
                      <label className="block text-base font-semibold text-gray-700 mb-2">
                        Proveedor*
                      </label>
                      <select
                        name="proveedorId"
                        value={formData.proveedorId}
                        onChange={handleProveedorChange}
                        className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                        required
                      >
                        <option value="">-- Selecciona un proveedor --</option>
                        {proveedores.map(proveedor => (
                          <option key={proveedor.id} value={proveedor.id}>
                            {proveedor.nombre} {proveedor.nif && `(${proveedor.nif})`}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div>
                      <label className="block text-base font-semibold text-gray-700 mb-2">
                        NIF/CIF (autocompletado)
                      </label>
                      <input
                        type="text"
                        value={formData.proveedorNif}
                        className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base bg-gray-50"
                        readOnly
                      />
                    </div>
                    <div>
                      <label className="block text-base font-semibold text-gray-700 mb-2">
                        Categoría
                      </label>
                      <select
                        name="categoria"
                        value={formData.categoria}
                        onChange={handleInputChange}
                        className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                      >
                        <option value="">-- Selecciona una categoría --</option>
                        {categorias.map(cat => (
                          <option key={cat.valor} value={cat.valor}>
                            {cat.descripcion}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>

                {/* Sección: Líneas de Factura */}
                <div>
                  <h4 className="text-2xl font-bold text-gray-900 mb-4 pb-3 border-b-2 border-orange-300">
                    Líneas de Factura
                  </h4>

                  <div className="bg-orange-50 p-5 rounded-lg mb-4">
                    <div className="grid grid-cols-1 md:grid-cols-12 gap-4 items-end">
                      <div className="md:col-span-4">
                        <label className="block text-base font-semibold text-gray-700 mb-2">
                          Concepto
                        </label>
                        <input
                          type="text"
                          name="concepto"
                          value={nuevaLinea.concepto}
                          onChange={handleNuevaLineaChange}
                          className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                          placeholder="Descripción del servicio/producto"
                        />
                      </div>
                      <div className="md:col-span-2">
                        <label className="block text-base font-semibold text-gray-700 mb-2">
                          Cantidad
                        </label>
                        <input
                          type="number"
                          name="cantidad"
                          value={nuevaLinea.cantidad}
                          onChange={handleNuevaLineaChange}
                          step="0.01"
                          min="1"
                          className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                        />
                      </div>
                      <div className="md:col-span-2">
                        <label className="block text-base font-semibold text-gray-700 mb-2">
                          P. Unitario (€)
                        </label>
                        <input
                          type="number"
                          name="precioUnitario"
                          value={nuevaLinea.precioUnitario}
                          onChange={handleNuevaLineaChange}
                          step="0.01"
                          className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                          placeholder="0,00"
                        />
                      </div>
                      <div className="md:col-span-2">
                        <label className="block text-base font-semibold text-gray-700 mb-2">
                          IVA (%)
                        </label>
                        <select
                          name="tipoIva"
                          value={nuevaLinea.tipoIva}
                          onChange={handleNuevaLineaChange}
                          className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                        >
                          <option value="0">0%</option>
                          <option value="4">4%</option>
                          <option value="10">10%</option>
                          <option value="21">21%</option>
                        </select>
                      </div>
                      <div className="md:col-span-2">
                        <button
                          type="button"
                          onClick={agregarLinea}
                          className="w-full bg-orange-500 hover:bg-orange-600 text-white px-4 py-3 rounded-lg font-semibold text-base transition-colors"
                        >
                          + Agregar
                        </button>
                      </div>
                    </div>
                  </div>

                  {formData.lineas.length > 0 ? (
                    <div className="overflow-x-auto border border-gray-200 rounded-lg mb-4">
                      <table className="w-full">
                        <thead className="bg-gray-100">
                          <tr>
                            <th className="px-4 py-3 text-left text-base font-semibold text-gray-700">Concepto</th>
                            <th className="px-4 py-3 text-center text-base font-semibold text-gray-700">Cantidad</th>
                            <th className="px-4 py-3 text-right text-base font-semibold text-gray-700">P. Unit.</th>
                            <th className="px-4 py-3 text-center text-base font-semibold text-gray-700">IVA %</th>
                            <th className="px-4 py-3 text-right text-base font-semibold text-gray-700">Subtotal</th>
                            <th className="px-4 py-3 text-center text-base font-semibold text-gray-700">Acción</th>
                          </tr>
                        </thead>
                        <tbody>
                          {formData.lineas.map((linea) => (
                            <tr key={linea.id} className="border-t border-gray-200 hover:bg-gray-50">
                              <td className="px-4 py-3 text-base">{linea.concepto}</td>
                              <td className="px-4 py-3 text-base text-center">{linea.cantidad}</td>
                              <td className="px-4 py-3 text-base text-right">{linea.precioUnitario.toFixed(2)} €</td>
                              <td className="px-4 py-3 text-base text-center font-semibold text-orange-600">{(linea.tipoIva ?? 21).toFixed(2)} %</td>
                              <td className="px-4 py-3 text-base text-right font-semibold">{linea.subtotal.toFixed(2)} €</td>
                              <td className="px-4 py-3 text-center">
                                <button
                                  type="button"
                                  onClick={() => eliminarLinea(linea.id)}
                                  className="text-red-600 hover:text-red-800 font-semibold text-base"
                                >
                                  Eliminar
                                </button>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : (
                    <div className="text-center py-8 bg-gray-50 rounded-lg border-2 border-dashed border-gray-300 mb-4">
                      <p className="text-gray-500">No hay líneas agregadas. Utilice el formulario de arriba para agregar líneas.</p>
                    </div>
                  )}
                </div>

                <div>
                  <h4 className="text-2xl font-bold text-gray-900 mb-4 pb-3 border-b-2 border-orange-300">
                    Importes (Calculados Automáticamente)
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                    <div>
                      <label className="block text-base font-semibold text-gray-700 mb-2">
                        Base Imponible
                      </label>
                      <input
                        type="text"
                        value={formData.baseImponible ? `${formData.baseImponible} €` : '0,00 €'}
                        className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base bg-gray-50"
                        readOnly
                      />
                    </div>
                    <div>
                      <label className="block text-base font-semibold text-gray-700 mb-2">
                        IVA de importación
                      </label>
                      <input
                        type="text"
                        value={formData.cuotaIva ? `${formData.cuotaIva} €` : '0,00 €'}
                        className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base bg-gray-50"
                        readOnly
                      />
                    </div>
                  </div>
                  <div className="mt-5">
                    <label className="block text-base font-semibold text-gray-700 mb-2">
                      Factura total
                    </label>
                    <input
                      type="text"
                      value={formData.total ? `${formData.total} €` : '0,00 €'}
                      className="w-full border-2 border-green-500 rounded-lg px-4 py-3 bg-green-50 text-lg font-bold text-green-700"
                      readOnly
                    />
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-5 mt-5">
                    <div>
                      <label className="block text-base font-semibold text-gray-700 mb-2">
                        Estado
                      </label>
                      <select
                        name="estado"
                        value={formData.estado}
                        onChange={handleInputChange}
                        className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                      >
                        <option value="PENDIENTE">Pendiente</option>
                        <option value="PAGADA">Pagada</option>
                        <option value="RECHAZADA">Rechazada</option>
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
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                      >
                        <option value="">-- Selecciona un método --</option>
                        {metodosPago.map((metodo) => (
                          <option key={metodo.valor} value={metodo.valor}>
                            {metodo.descripcion}
                          </option>
                        ))}
                      </select>
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
                    className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                    rows="3"
                    placeholder="Notas adicionales"
                  />
                </div>
              </form>
            </div>

            <div className="px-10 py-6 border-t border-gray-200 bg-gray-50 flex justify-end gap-3 rounded-b-xl">
              <button
                type="button"
                onClick={cerrarModal}
                className="px-6 py-3 border-2 border-gray-300 rounded-lg font-semibold text-base text-gray-700 hover:bg-gray-100 transition-colors"
              >
                Cancelar
              </button>
              <button
                type="submit"
                onClick={guardarFactura}
                className="px-6 py-3 bg-orange-600 hover:bg-orange-700 text-white text-base font-semibold rounded-lg transition-colors shadow-md hover:shadow-lg"
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

export default FacturasRecibidas;