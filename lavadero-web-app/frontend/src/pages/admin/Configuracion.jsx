import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import {
  Building2, FileText, Mail, Settings as SettingsIcon,
  Save, Upload, X, AlertCircle, CheckCircle, Eye, Image as ImageIcon
} from 'lucide-react';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const Configuracion = () => {
  const queryClient = useQueryClient();
  const [tabActiva, setTabActiva] = useState('empresa');
  const [mensaje, setMensaje] = useState(null);
  const [previsualizacion, setPrevisualizacion] = useState(false);

  // Estados para configuración de empresa
  const [configEmpresa, setConfigEmpresa] = useState({
    nombre: '',
    cif: '',
    direccion: '',
    codigoPostal: '',
    ciudad: '',
    provincia: '',
    telefono: '',
    email: '',
    web: '',
    logoBase64: ''
  });

  // Estados para configuración de factura
  const [configFactura, setConfigFactura] = useState({
    prefijoFactura: 'F',
    siguienteNumero: 1,
    serie: new Date().getFullYear().toString(),
    formatoNumero: '{PREFIJO}-{SERIE}-{NUMERO}',
    incluirLogo: true,
    colorPrimario: '#3b82f6',
    colorSecundario: '#1e40af',
    mostrarQR: false,
    textoEncabezado: '',
    textoPie: 'Gracias por confiar en nuestros servicios',
    terminosCondiciones: 'El pago de esta factura implica la aceptación de las condiciones generales de venta.',
    informacionBancaria: '',
    iban: '',
    mostrarIVA: true,
    tipoIVA: 21,
    recargoEquivalencia: false,
    porcentajeRecargo: 5.2
  });

  // Estados para configuración de email
  const [configEmail, setConfigEmail] = useState({
    asuntoConfirmacion: 'Confirmación de reserva - Lavadero Sepúlveda',
    plantillaConfirmacion: '',
    enviarCopiaPropietario: true,
    emailCopia: ''
  });

  // Query para obtener configuración
  const { data: configuracion, isLoading } = useQuery({
    queryKey: ['configuracion'],
    queryFn: async () => {
      const { data } = await axios.get(`${API_URL}/configuracion`);
      return data;
    }
  });

  // Cargar datos cuando se obtienen
  useEffect(() => {
    if (configuracion) {
      if (configuracion.empresa) {
        setConfigEmpresa(prev => ({ ...prev, ...configuracion.empresa }));
      }
      if (configuracion.factura) {
        setConfigFactura(prev => ({ ...prev, ...configuracion.factura }));
      }
      if (configuracion.email) {
        setConfigEmail(prev => ({ ...prev, ...configuracion.email }));
      }
    }
  }, [configuracion]);

  // Mutation para guardar configuración
  const guardarMutation = useMutation({
    mutationFn: async (data) => {
      const response = await axios.put(`${API_URL}/configuracion`, data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['configuracion']);
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

  // Manejar cambios en formularios
  const handleChangeEmpresa = (e) => {
    const { name, value } = e.target;
    setConfigEmpresa(prev => ({ ...prev, [name]: value }));
  };

  const handleChangeFactura = (e) => {
    const { name, value, type, checked } = e.target;
    setConfigFactura(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleChangeEmail = (e) => {
    const { name, value, type, checked } = e.target;
    setConfigEmail(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  // Subir logo
  const handleLogoUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) {
        setMensaje({ tipo: 'error', texto: 'El logo no debe superar 2MB' });
        return;
      }

      const reader = new FileReader();
      reader.onloadend = () => {
        setConfigEmpresa(prev => ({ ...prev, logoBase64: reader.result }));
      };
      reader.readAsDataURL(file);
    }
  };

  // Eliminar logo
  const handleEliminarLogo = () => {
    setConfigEmpresa(prev => ({ ...prev, logoBase64: '' }));
  };

  // Guardar configuración
  const handleGuardar = () => {
    const dataToSave = {
      empresa: configEmpresa,
      factura: configFactura,
      email: configEmail
    };
    guardarMutation.mutate(dataToSave);
  };

  // Generar preview de número de factura
  const generarPreviewNumero = () => {
    return configFactura.formatoNumero
      .replace('{PREFIJO}', configFactura.prefijoFactura)
      .replace('{SERIE}', configFactura.serie)
      .replace('{NUMERO}', String(configFactura.siguienteNumero).padStart(4, '0'));
  };

  // Tabs
  const tabs = [
    { id: 'empresa', label: 'Datos Empresa', icon: Building2 },
    { id: 'factura', label: 'Plantilla Facturas', icon: FileText },
    { id: 'email', label: 'Configuración Email', icon: Mail },
    { id: 'sistema', label: 'Sistema', icon: SettingsIcon }
  ];

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

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
        <div className={`rounded-lg p-4 flex items-center justify-between ${mensaje.tipo === 'exito' ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'
          }`}>
          <div className="flex items-center">
            {mensaje.tipo === 'exito' ? (
              <CheckCircle className="text-green-600 mr-2" size={20} />
            ) : (
              <AlertCircle className="text-red-600 mr-2" size={20} />
            )}
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
          {/* Tab Empresa */}
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
                      name="nombre"
                      value={configEmpresa.nombre}
                      onChange={handleChangeEmpresa}
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
                      name="cif"
                      value={configEmpresa.cif}
                      onChange={handleChangeEmpresa}
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
                      name="direccion"
                      value={configEmpresa.direccion}
                      onChange={handleChangeEmpresa}
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
                      name="codigoPostal"
                      value={configEmpresa.codigoPostal}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Ciudad
                    </label>
                    <input
                      type="text"
                      name="ciudad"
                      value={configEmpresa.ciudad}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Provincia
                    </label>
                    <input
                      type="text"
                      name="provincia"
                      value={configEmpresa.provincia}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Teléfono *
                    </label>
                    <input
                      type="tel"
                      name="telefono"
                      value={configEmpresa.telefono}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Email *
                    </label>
                    <input
                      type="email"
                      name="email"
                      value={configEmpresa.email}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Página Web
                    </label>
                    <input
                      type="url"
                      name="web"
                      value={configEmpresa.web}
                      onChange={handleChangeEmpresa}
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

                  {configEmpresa.logoBase64 && (
                    <div className="flex-1">
                      <div className="border border-gray-300 rounded-lg p-4 relative">
                        <img
                          src={configEmpresa.logoBase64}
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

          {/* Tab Plantilla Facturas */}
          {tabActiva === 'factura' && (
            <div className="space-y-6">
              {/* Numeración */}
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Numeración de Facturas</h3>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Prefijo
                    </label>
                    <input
                      type="text"
                      name="prefijoFactura"
                      value={configFactura.prefijoFactura}
                      onChange={handleChangeFactura}
                      placeholder="F"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Serie (Año)
                    </label>
                    <input
                      type="text"
                      name="serie"
                      value={configFactura.serie}
                      onChange={handleChangeFactura}
                      placeholder="2025"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Siguiente Número
                    </label>
                    <input
                      type="number"
                      name="siguienteNumero"
                      value={configFactura.siguienteNumero}
                      onChange={handleChangeFactura}
                      min="1"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Vista Previa
                    </label>
                    <div className="px-3 py-2 bg-blue-50 border border-blue-300 rounded-lg font-mono text-blue-900">
                      {generarPreviewNumero()}
                    </div>
                  </div>

                  <div className="md:col-span-4">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Formato de Numeración
                    </label>
                    <input
                      type="text"
                      name="formatoNumero"
                      value={configFactura.formatoNumero}
                      onChange={handleChangeFactura}
                      placeholder="{PREFIJO}-{SERIE}-{NUMERO}"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono"
                    />
                    <p className="text-xs text-gray-500 mt-1">
                      Variables disponibles: {'{PREFIJO}'}, {'{SERIE}'}, {'{NUMERO}'}
                    </p>
                  </div>
                </div>
              </div>

              {/* Diseño */}
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Diseño y Colores</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div>
                    <label className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        name="incluirLogo"
                        checked={configFactura.incluirLogo}
                        onChange={handleChangeFactura}
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
                        value={configFactura.colorPrimario}
                        onChange={handleChangeFactura}
                        className="w-12 h-10 rounded border border-gray-300"
                      />
                      <input
                        type="text"
                        value={configFactura.colorPrimario}
                        onChange={(e) => setConfigFactura(prev => ({ ...prev, colorPrimario: e.target.value }))}
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
                        value={configFactura.colorSecundario}
                        onChange={handleChangeFactura}
                        className="w-12 h-10 rounded border border-gray-300"
                      />
                      <input
                        type="text"
                        value={configFactura.colorSecundario}
                        onChange={(e) => setConfigFactura(prev => ({ ...prev, colorSecundario: e.target.value }))}
                        className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono text-sm"
                      />
                    </div>
                  </div>
                </div>
              </div>

              {/* Textos */}
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Textos de la Factura</h3>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Texto del Encabezado
                    </label>
                    <textarea
                      name="textoEncabezado"
                      value={configFactura.textoEncabezado}
                      onChange={handleChangeFactura}
                      rows="2"
                      placeholder="Texto opcional en el encabezado de la factura"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Texto del Pie de Página
                    </label>
                    <textarea
                      name="textoPie"
                      value={configFactura.textoPie}
                      onChange={handleChangeFactura}
                      rows="2"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Términos y Condiciones
                    </label>
                    <textarea
                      name="terminosCondiciones"
                      value={configFactura.terminosCondiciones}
                      onChange={handleChangeFactura}
                      rows="3"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Información Bancaria
                    </label>
                    <textarea
                      name="informacionBancaria"
                      value={configFactura.informacionBancaria}
                      onChange={handleChangeFactura}
                      rows="2"
                      placeholder="Banco, titular de la cuenta, etc."
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      IBAN
                    </label>
                    <input
                      type="text"
                      name="iban"
                      value={configFactura.iban}
                      onChange={handleChangeFactura}
                      placeholder="ES00 0000 0000 0000 0000 0000"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono"
                    />
                  </div>
                </div>
              </div>

              {/* IVA */}
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Configuración de IVA</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div>
                    <label className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        name="mostrarIVA"
                        checked={configFactura.mostrarIVA}
                        onChange={handleChangeFactura}
                        className="w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500"
                      />
                      <span className="text-sm font-medium text-gray-700">Mostrar desglose de IVA</span>
                    </label>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      % IVA por defecto
                    </label>
                    <select
                      name="tipoIVA"
                      value={configFactura.tipoIVA}
                      onChange={handleChangeFactura}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    >
                      <option value="0">0% (Exento)</option>
                      <option value="4">4% (Superreducido)</option>
                      <option value="10">10% (Reducido)</option>
                      <option value="21">21% (General)</option>
                    </select>
                  </div>

                  <div>
                    <label className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        name="recargoEquivalencia"
                        checked={configFactura.recargoEquivalencia}
                        onChange={handleChangeFactura}
                        className="w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500"
                      />
                      <span className="text-sm font-medium text-gray-700">Recargo de equivalencia</span>
                    </label>
                  </div>
                </div>
              </div>

              {/* Botón de previsualización */}
              <div className="pt-4 border-t border-gray-200">
                <button
                  onClick={() => setPrevisualizacion(true)}
                  className="flex items-center px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
                >
                  <Eye size={20} className="mr-2" />
                  Previsualizar Factura
                </button>
              </div>
            </div>
          )}

          {/* Tab Email */}
          {tabActiva === 'email' && (
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Configuración de Emails</h3>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Asunto del Email de Confirmación
                    </label>
                    <input
                      type="text"
                      name="asuntoConfirmacion"
                      value={configEmail.asuntoConfirmacion}
                      onChange={handleChangeEmail}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="flex items-center space-x-2 mb-4">
                      <input
                        type="checkbox"
                        name="enviarCopiaPropietario"
                        checked={configEmail.enviarCopiaPropietario}
                        onChange={handleChangeEmail}
                        className="w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500"
                      />
                      <span className="text-sm font-medium text-gray-700">
                        Enviar copia de confirmaciones al propietario
                      </span>
                    </label>

                    {configEmail.enviarCopiaPropietario && (
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Email para copias
                        </label>
                        <input
                          type="email"
                          name="emailCopia"
                          value={configEmail.emailCopia}
                          onChange={handleChangeEmail}
                          placeholder="propietario@lavadero.com"
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        />
                      </div>
                    )}
                  </div>

                  <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
                    <p className="text-sm text-blue-900">
                      <strong>Nota:</strong> La plantilla de email de confirmación se puede personalizar
                      modificando el archivo <code className="bg-blue-100 px-2 py-1 rounded">confirmacion-cita.html</code>
                      en la carpeta de templates del backend.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Tab Sistema */}
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
                    <p className="text-lg font-bold text-gray-900">Enero 2025</p>
                  </div>

                  <div className="p-4 bg-gray-50 rounded-lg md:col-span-2">
                    <p className="text-sm text-gray-600">Base de Datos</p>
                    <p className="text-lg font-bold text-gray-900">PostgreSQL</p>
                  </div>
                </div>
              </div>

              <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
                <h4 className="font-bold text-yellow-900 mb-2">⚠️ Zona de Mantenimiento</h4>
                <p className="text-sm text-yellow-800 mb-4">
                  Las operaciones de mantenimiento pueden afectar al funcionamiento del sistema.
                  Realízalas con precaución.
                </p>
                <div className="space-y-2">
                  <button className="w-full md:w-auto px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition-colors">
                    Limpiar Caché
                  </button>
                  <button className="w-full md:w-auto px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors ml-0 md:ml-2">
                    Reiniciar Numeración (Peligroso)
                  </button>
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
            onClick={() => window.location.reload()}
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