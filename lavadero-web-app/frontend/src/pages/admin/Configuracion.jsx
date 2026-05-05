import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import {
  Building2, FileText, Settings as SettingsIcon,
  Save, Upload, X, AlertCircle, CheckCircle
} from 'lucide-react';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const Configuracion = () => {
  const queryClient = useQueryClient();
  const [tabActiva, setTabActiva] = useState('empresa');
  const [mensaje, setMensaje] = useState(null);

  // ── Estado local único de verdad ─────────────────────────────────────────
  const [config, setConfig] = useState({
    emisorNombre: '',
    emisorNif: '',
    emisorDireccion: '',
    emisorCodigoPostal: '',
    emisorCiudad: '',
    emisorProvincia: '',
    emisorTelefono: '',
    emisorEmail: '',
    emisorWeb: '',
    logoBase64: '',
    colorPrimario: '#2196F3',
    colorSecundario: '#1976D2',
    mostrarLogo: true,
    textoGracias: 'Gracias por confiar en nosotros',
    pieFactura: '',
    cuentaBancaria: ''
  });

  // ── Query para cargar configuración ──────────────────────────────────────
  const { data: configuracion, isLoading } = useQuery({
    queryKey: ['plantilla-factura'],
    queryFn: async () => {
      const { data } = await axios.get(`${API_URL}/config/plantilla-factura`);
      return data;
    }
  });

  // ✅ Cuando llegan los datos del servidor, inicializar el estado local UNA sola vez
  useEffect(() => {
    if (configuracion) {
      setConfig({
        emisorNombre: configuracion.emisorNombre || '',
        emisorNif: configuracion.emisorNif || '',
        emisorDireccion: configuracion.emisorDireccion || '',
        emisorCodigoPostal: configuracion.emisorCodigoPostal || '',
        emisorCiudad: configuracion.emisorCiudad || '',
        emisorProvincia: configuracion.emisorProvincia || '',
        emisorTelefono: configuracion.emisorTelefono || '',
        emisorEmail: configuracion.emisorEmail || '',
        emisorWeb: configuracion.emisorWeb || '',
        logoBase64: configuracion.logoBase64 || '',
        colorPrimario: configuracion.colorPrimario || '#2196F3',
        colorSecundario: configuracion.colorSecundario || '#1976D2',
        mostrarLogo: configuracion.mostrarLogo ?? true,
        textoGracias: configuracion.textoGracias || 'Gracias por confiar en nosotros',
        pieFactura: configuracion.pieFactura || '',
        cuentaBancaria: configuracion.cuentaBancaria || ''
      });
    }
  }, [configuracion]);

  // ✅ Handler limpio: solo actualiza el campo que cambia, sin mezclar con el servidor
  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setConfig(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  // ── Subir logo ────────────────────────────────────────────────────────────
  const handleLogoUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (file.size > 2 * 1024 * 1024) {
      setMensaje({ tipo: 'error', texto: 'El logo no debe superar 2MB' });
      return;
    }

    const reader = new FileReader();
    reader.onloadend = () => {
      setConfig(prev => ({ ...prev, logoBase64: reader.result }));
    };
    reader.readAsDataURL(file);
  };

  // ── Eliminar logo ─────────────────────────────────────────────────────────
  const handleEliminarLogo = () => {
    setConfig(prev => ({ ...prev, logoBase64: '' }));
  };

  // ── Mutation para guardar ─────────────────────────────────────────────────
  const guardarMutation = useMutation({
    mutationFn: async (data) => {
      const backendData = {
        id: 1,
        ...data,
        logoAncho: 150,
        logoAlto: 60,
        colorTexto: '#333333',
        colorTextoClaro: '#666666',
        colorFondo: '#FFFFFF',
        colorFondoAlt: '#F5F5F5',
        colorBorde: '#E0E0E0',
        colorExito: '#4CAF50',
        tituloFactura: 'FACTURA',
        tituloFacturaSimplificada: 'FACTURA SIMPLIFICADA',
        condicionesPago: 'Pago al contado',
        textoIva: 'IVA incluido según normativa vigente',
        mostrarDatosContacto: true,
        mostrarCuentaBancaria: !!data.cuentaBancaria,
        mostrarCondicionesPago: true,
        mostrarTextoGracias: true,
        mostrarMarcaAgua: false,
        usarFilasAlternas: true
      };
      const response = await axios.post(`${API_URL}/config/plantilla-factura`, backendData);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['plantilla-factura']);
      setMensaje({ tipo: 'exito', texto: 'Configuración guardada correctamente' });
      setTimeout(() => setMensaje(null), 3000);
    },
    onError: (error) => {
      setMensaje({
        tipo: 'error',
        texto: error.response?.data?.mensaje || 'Error al guardar la configuración'
      });
      setTimeout(() => setMensaje(null), 5000);
    }
  });

  const handleGuardar = () => guardarMutation.mutate(config);

  // ── Tabs ──────────────────────────────────────────────────────────────────
  const tabs = [
    { id: 'empresa', label: 'Datos Empresa', icon: Building2 },
    { id: 'factura', label: 'Plantilla Facturas', icon: FileText },
    { id: 'sistema', label: 'Sistema', icon: SettingsIcon }
  ];

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
      </div>
    );
  }

  // ── Render ────────────────────────────────────────────────────────────────
  return (
    <div className="space-y-6">

      {/* Header */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center gap-3">
          <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <img src="/assets/icons/configuracion.png" alt="Configuración" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Configuración del Sistema</h1>
            <p className="text-gray-600 mt-1">Gestiona los ajustes de tu empresa y personaliza las facturas</p>
          </div>
        </div>
      </div>

      {/* Mensajes */}
      {mensaje && (
        <div className={`rounded-lg p-4 flex items-center justify-between ${mensaje.tipo === 'exito'
            ? 'bg-green-50 border border-green-200'
            : 'bg-red-50 border border-red-200'
          }`}>
          <div className="flex items-center">
            {mensaje.tipo === 'exito'
              ? <CheckCircle className="text-green-600 mr-2" size={20} />
              : <AlertCircle className="text-red-600 mr-2" size={20} />
            }
            <span className={mensaje.tipo === 'exito' ? 'text-green-800' : 'text-red-800'}>
              {mensaje.texto}
            </span>
          </div>
          <button onClick={() => setMensaje(null)}>
            <X size={20} className="text-gray-500" />
          </button>
        </div>
      )}

      {/* Tabs */}
      <div className="bg-white rounded-lg shadow">
        <div className="border-b border-gray-200">
          <div className="flex overflow-x-auto">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setTabActiva(tab.id)}
                  className={`flex items-center px-6 py-4 font-medium transition-colors whitespace-nowrap ${tabActiva === tab.id
                      ? 'border-b-2 border-blue-600 text-blue-600'
                      : 'text-gray-600 hover:text-gray-900'
                    }`}
                >
                  <Icon size={20} className="mr-2" />
                  {tab.label}
                </button>
              );
            })}
          </div>
        </div>

        <div className="p-6">

          {/* ── Tab Empresa ──────────────────────────────────────────────── */}
          {tabActiva === 'empresa' && (
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Datos de la Empresa</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Nombre Comercial *
                    </label>
                    <input
                      type="text"
                      name="emisorNombre"
                      value={config.emisorNombre}
                      onChange={handleChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      CIF/NIF *
                    </label>
                    <input
                      type="text"
                      name="emisorNif"
                      value={config.emisorNif}
                      onChange={handleChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    />
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Dirección *
                    </label>
                    <input
                      type="text"
                      name="emisorDireccion"
                      value={config.emisorDireccion}
                      onChange={handleChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Código Postal
                    </label>
                    <input
                      type="text"
                      name="emisorCodigoPostal"
                      value={config.emisorCodigoPostal}
                      onChange={handleChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Ciudad
                    </label>
                    <input
                      type="text"
                      name="emisorCiudad"
                      value={config.emisorCiudad}
                      onChange={handleChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Provincia
                    </label>
                    <input
                      type="text"
                      name="emisorProvincia"
                      value={config.emisorProvincia}
                      onChange={handleChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Teléfono
                    </label>
                    <input
                      type="tel"
                      name="emisorTelefono"
                      value={config.emisorTelefono}
                      onChange={handleChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Email
                    </label>
                    <input
                      type="email"
                      name="emisorEmail"
                      value={config.emisorEmail}
                      onChange={handleChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Página Web
                    </label>
                    <input
                      type="url"
                      name="emisorWeb"
                      value={config.emisorWeb}
                      onChange={handleChange}
                      placeholder="https://www.ejemplo.com"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                </div>
              </div>

              {/* Logo */}
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Logotipo de la Empresa</h3>
                <div className="flex items-start gap-6">
                  <div className="flex-1">
                    <label className="block">
                      <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-500 transition-colors cursor-pointer">
                        <input
                          type="file"
                          accept="image/png,image/jpeg,image/jpg"
                          onChange={handleLogoUpload}
                          className="hidden"
                        />
                        <Upload className="mx-auto text-gray-400 mb-2" size={40} />
                        <p className="text-sm text-gray-600">
                          Click para subir logo<br />
                          <span className="text-xs text-gray-500">PNG, JPG (máx. 2MB)</span>
                        </p>
                      </div>
                    </label>
                  </div>

                  {config.logoBase64 && (
                    <div className="flex-1">
                      <div className="border border-gray-300 rounded-lg p-4 relative">
                        <img
                          src={config.logoBase64}
                          alt="Logo"
                          className="max-w-full h-32 object-contain mx-auto"
                        />
                        <button
                          onClick={handleEliminarLogo}
                          className="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full hover:bg-red-600"
                        >
                          <X size={16} />
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* ── Tab Plantilla Facturas ────────────────────────────────────── */}
          {tabActiva === 'factura' && (
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Diseño y Colores</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

                  <div>
                    <label className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        name="mostrarLogo"
                        checked={config.mostrarLogo}
                        onChange={handleChange}
                        className="w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500"
                      />
                      <span className="text-sm font-medium text-gray-700">Incluir logo en factura</span>
                    </label>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Color Primario
                    </label>
                    <div className="flex gap-2">
                      <input
                        type="color"
                        name="colorPrimario"
                        value={config.colorPrimario}
                        onChange={handleChange}
                        className="w-12 h-10 rounded border border-gray-300"
                      />
                      <input
                        type="text"
                        name="colorPrimario"
                        value={config.colorPrimario}
                        onChange={handleChange}
                        className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono text-sm"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Color Secundario
                    </label>
                    <div className="flex gap-2">
                      <input
                        type="color"
                        name="colorSecundario"
                        value={config.colorSecundario}
                        onChange={handleChange}
                        className="w-12 h-10 rounded border border-gray-300"
                      />
                      <input
                        type="text"
                        name="colorSecundario"
                        value={config.colorSecundario}
                        onChange={handleChange}
                        className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono text-sm"
                      />
                    </div>
                  </div>

                </div>
              </div>

              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Textos de la Factura</h3>
                <div className="space-y-4">

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Texto del Pie de Página
                    </label>
                    <textarea
                      name="textoGracias"
                      value={config.textoGracias}
                      onChange={handleChange}
                      rows="2"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Términos y Condiciones
                    </label>
                    <textarea
                      name="pieFactura"
                      value={config.pieFactura}
                      onChange={handleChange}
                      rows="3"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Cuenta Bancaria / IBAN
                    </label>
                    <input
                      type="text"
                      name="cuentaBancaria"
                      value={config.cuentaBancaria}
                      onChange={handleChange}
                      placeholder="ES00 0000 0000 0000 0000 0000"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono"
                    />
                  </div>

                </div>
              </div>
            </div>
          )}

          {/* ── Tab Sistema ───────────────────────────────────────────────── */}
          {tabActiva === 'sistema' && (
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Información del Sistema</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="p-4 bg-gray-50 rounded-lg">
                    <p className="text-sm text-gray-600">Versión del Sistema</p>
                    <p className="text-lg font-bold text-gray-900">1.0.0</p>
                  </div>
                  <div className="p-4 bg-gray-50 rounded-lg">
                    <p className="text-sm text-gray-600">Última Actualización</p>
                    <p className="text-lg font-bold text-gray-900">Mayo 2026</p>
                  </div>
                </div>
              </div>
            </div>
          )}

        </div>
      </div>

      {/* Botones de acción */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex justify-end gap-4">
          <button
            onClick={() => {
              if (configuracion) setConfig({ ...configuracion });
            }}
            className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Cancelar
          </button>
          <button
            onClick={handleGuardar}
            disabled={guardarMutation.isPending}
            className="flex items-center px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Save size={20} className="mr-2" />
            {guardarMutation.isPending ? 'Guardando...' : 'Guardar Configuración'}
          </button>
        </div>
      </div>

    </div>
  );
};

export default Configuracion;