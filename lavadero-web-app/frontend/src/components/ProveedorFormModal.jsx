import { useState } from 'react';
import proveedorService from '../services/proveedorService';

/**
 * Formulario de proveedor a pantalla completa (crear y editar).
 *
 * Es el MISMO formulario en todas partes:
 *   - Gestión de Proveedores (crear / editar)
 *   - Nueva Factura Recibida → "+ Nuevo proveedor" (crear y seleccionar)
 *
 * Props:
 *   proveedor      Proveedor a editar, o null para crear uno nuevo
 *   onClose        Cerrar sin guardar
 *   onSaved        Recibe el proveedor guardado (con su id) devuelto por el backend
 *   textoGuardar   Texto opcional del botón de guardar al crear
 *   zIndex         Clase z-* opcional (por encima de otro modal, p. ej. 'z-[60]')
 */

const CATEGORIAS_PROVEEDOR = [
  { valor: 'Suministros', etiqueta: 'Suministros' },
  { valor: 'Servicios', etiqueta: 'Servicios' },
  { valor: 'Mantenimiento', etiqueta: 'Mantenimiento' },
  { valor: 'Equipamiento', etiqueta: 'Equipamiento' },
];

const FORMAS_PAGO_PROVEEDOR = [
  { valor: 'TRANSFERENCIA', etiqueta: 'Transferencia' },
  { valor: 'CHEQUE', etiqueta: 'Cheque' },
  { valor: 'EFECTIVO', etiqueta: 'Efectivo' },
  { valor: 'TARJETA', etiqueta: 'Tarjeta' },
];

const PROVEEDOR_VACIO = {
  nombre: '',
  nif: '',
  email: '',
  telefono: '',
  contacto: '',
  categoria: '',
  direccion: '',
  ciudad: '',
  codigoPostal: '',
  formaPago: 'TRANSFERENCIA',
  activo: true,
};

const ProveedorFormModal = ({ proveedor, onClose, onSaved, textoGuardar, zIndex = 'z-50' }) => {
  const editando = !!proveedor?.id;

  const [datos, setDatos] = useState(() => ({
    ...PROVEEDOR_VACIO,
    ...Object.fromEntries(
      Object.entries(proveedor || {}).filter(([, v]) => v !== null && v !== undefined)
    ),
  }));
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setDatos(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const guardar = async (e) => {
    e.preventDefault();
    if (!datos.nombre.trim() || !datos.email.trim()) {
      setError('Nombre y email son obligatorios');
      return;
    }

    setGuardando(true);
    setError('');
    try {
      const payload = Object.fromEntries(
        Object.entries(datos).map(([k, v]) => [k, typeof v === 'string' ? v.trim() : v])
      );
      payload.nif = (payload.nif || '').toUpperCase();

      const response = editando
        ? await proveedorService.update(proveedor.id, payload)
        : await proveedorService.create(payload);

      onSaved(response.data);
    } catch (err) {
      setError(
        err.response?.data?.mensaje ||
        err.response?.data?.error ||
        err.response?.data?.message ||
        'No se pudo guardar el proveedor: ' + err.message
      );
    } finally {
      setGuardando(false);
    }
  };

  const input = 'w-full bg-white border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500';
  const label = 'block text-base font-semibold text-gray-700 mb-2';
  const seccion = 'text-2xl font-bold text-gray-900 mb-4 pb-3 border-b-2 border-orange-300';

  return (
    <div className={`fixed inset-0 ${zIndex} bg-white flex flex-col`} role="dialog" aria-modal="true">
      {/* Cabecera */}
      <div className="shrink-0 flex items-center justify-between px-10 py-6 border-b border-gray-200 bg-linear-to-r from-orange-50 to-orange-100">
        <h3 className="text-3xl font-bold text-gray-900">
          {editando ? 'Editar Proveedor' : 'Nuevo Proveedor'}
        </h3>
        <button
          type="button"
          onClick={onClose}
          aria-label="Cerrar"
          className="text-gray-500 hover:text-gray-900 text-3xl leading-none px-2"
        >
          ✕
        </button>
      </div>

      <form onSubmit={guardar} className="flex-1 min-h-0 flex flex-col">
        {/* Contenido con scroll */}
        <div className="flex-1 min-h-0 overflow-y-auto px-10 py-8 space-y-8">
          {error && (
            <div className="p-4 bg-red-50 border border-red-300 text-red-800 rounded-lg">{error}</div>
          )}

          <div>
            <h4 className={seccion}>Datos del proveedor</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label className={label}>Nombre*</label>
                <input name="nombre" value={datos.nombre} onChange={handleChange} className={input} placeholder="Nombre proveedor" required autoFocus />
              </div>
              <div>
                <label className={label}>NIF/CIF</label>
                <input name="nif" value={datos.nif} onChange={handleChange} className={input} placeholder="B12345678" />
              </div>
              <div>
                <label className={label}>Email*</label>
                <input type="email" name="email" value={datos.email} onChange={handleChange} className={input} placeholder="email@ejemplo.com" required />
              </div>
              <div>
                <label className={label}>Teléfono</label>
                <input type="tel" name="telefono" value={datos.telefono} onChange={handleChange} className={input} placeholder="600000000" />
              </div>
              <div>
                <label className={label}>Contacto</label>
                <input name="contacto" value={datos.contacto} onChange={handleChange} className={input} placeholder="Nombre contacto" />
              </div>
              <div>
                <label className={label}>Categoría</label>
                <select name="categoria" value={datos.categoria} onChange={handleChange} className={input}>
                  <option value="">Seleccionar categoría</option>
                  {CATEGORIAS_PROVEEDOR.map(c => (
                    <option key={c.valor} value={c.valor}>{c.etiqueta}</option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          <div>
            <h4 className={seccion}>Dirección</h4>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
              <div className="md:col-span-3">
                <label className={label}>Dirección</label>
                <input name="direccion" value={datos.direccion} onChange={handleChange} className={input} placeholder="Calle, número" />
              </div>
              <div>
                <label className={label}>Ciudad</label>
                <input name="ciudad" value={datos.ciudad} onChange={handleChange} className={input} placeholder="Ciudad" />
              </div>
              <div>
                <label className={label}>Código Postal</label>
                <input name="codigoPostal" value={datos.codigoPostal} onChange={handleChange} className={input} placeholder="CP" inputMode="numeric" maxLength={5} />
              </div>
            </div>
          </div>

          <div>
            <h4 className={seccion}>Pago y estado</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-5 items-end">
              <div>
                <label className={label}>Forma de Pago</label>
                <select name="formaPago" value={datos.formaPago} onChange={handleChange} className={input}>
                  {FORMAS_PAGO_PROVEEDOR.map(f => (
                    <option key={f.valor} value={f.valor}>{f.etiqueta}</option>
                  ))}
                </select>
              </div>
              <label className="flex items-center gap-3 py-3">
                <input type="checkbox" name="activo" checked={!!datos.activo} onChange={handleChange} className="w-5 h-5 accent-orange-600" />
                <span className="text-base font-semibold text-gray-700">Activo</span>
              </label>
            </div>
          </div>
        </div>

        {/* Botones fijos abajo */}
        <div className="shrink-0 px-10 py-6 border-t border-gray-200 bg-gray-50 flex justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            className="px-6 py-3 border-2 border-gray-300 rounded-lg font-semibold text-base text-gray-700 hover:bg-gray-100 transition-colors"
          >
            Cancelar
          </button>
          <button
            type="submit"
            disabled={guardando}
            className="px-6 py-3 bg-orange-600 hover:bg-orange-700 disabled:opacity-50 text-white text-base font-semibold rounded-lg transition-colors shadow-md"
          >
            {guardando ? 'Guardando...' : editando ? 'Actualizar' : (textoGuardar || 'Crear')}
          </button>
        </div>
      </form>
    </div>
  );
};

export default ProveedorFormModal;