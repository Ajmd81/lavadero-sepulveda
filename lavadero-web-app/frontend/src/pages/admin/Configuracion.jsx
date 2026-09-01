import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import {
  Building2, FileText, Settings as SettingsIcon, Clock,
  Save, Upload, X, AlertCircle, CheckCircle
} from 'lucide-react';
import api from '../../services/api';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const Configuracion = () => {
  const queryClient = useQueryClient();
  const [tabActiva, setTabActiva] = useState('empresa');
  const [mensaje, setMensaje] = useState(null);

  // ── Queries - Datos desde servidor ─────────────────────────────────────────
  const { data: configuracion, isLoading } = useQuery({
    queryKey: ['plantilla-factura'],
    queryFn: async () => {
      const { data } = await axios.get(`${API_URL}/config/plantilla-factura`);
      return data;
    }
  });

  const { data: configuracionHorarios, isLoading: isLoadingHorarios } = useQuery({
    queryKey: ['configuracion-horario'],
    queryFn: async () => {
      const { data } = await api.get('/configuracion-horario');
      return data;
    }
  });

  // ── Estado local SOLO para cambios en progreso (no sincronizados con queries) ─
  const [configEnEdicion, setConfigEnEdicion] = useState(null);
  const [horarioEnEdicion, setHorarioEnEdicion] = useState(null);

  // ── Handlers para iniciar edición ──────────────────────────────────────────
  const iniciarEdicionEmpresa = () => {
    if (configuracion && !configEnEdicion) {
      setConfigEnEdicion({
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
  };

  const iniciarEdicionHorario = () => {
    if (configuracionHorarios && !horarioEnEdicion) {
      const horaApertura = configuracionHorarios.horaApertura
        ? configuracionHorarios.horaApertura.substring(0, 5)
        : '08:00';
      const horaCierre = configuracionHorarios.horaCierre
        ? configuracionHorarios.horaCierre.substring(0, 5)
        : '20:00';

      setHorarioEnEdicion({
        duracionCitaMinutos: configuracionHorarios.duracionCitaMinutos || 60,
        citasPorHora: configuracionHorarios.citasPorHora || 2,
        modoHorario: configuracionHorarios.modoHorario || 'COMPLETO',
        horaApertura,
        horaCierre,
      });
    }
  };

  // Inicializar edición cuando tab cambia
  useState(() => {
    if (tabActiva === 'empresa') {
      iniciarEdicionEmpresa();
    } else if (tabActiva === 'horarios') {
      iniciarEdicionHorario();
    }
  });

  // ── Handlers para cambios en formularios ────────────────────────────────────
  const handleChangeEmpresa = (e) => {
    const { name, value, type, checked } = e.target;
    setConfigEnEdicion(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleChangeHorario = (e) => {
    const { name, value } = e.target;
    setHorarioEnEdicion(prev => ({
      ...prev,
      [name]: name === 'duracionCitaMinutos' || name === 'citasPorHora'
        ? parseInt(value)
        : value,
    }));
  };

  const handleLogoUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (file.size > 2 * 1024 * 1024) {
      setMensaje({ tipo: 'error', texto: 'El logo no debe superar 2MB' });
      return;
    }

    const reader = new FileReader();
    reader.onloadend = () => {
      setConfigEnEdicion(prev => ({ ...prev, logoBase64: reader.result }));
    };
    reader.readAsDataURL(file);
  };

  const handleEliminarLogo = () => {
    setConfigEnEdicion(prev => ({ ...prev, logoBase64: '' }));
  };

  // ── Mutations ──────────────────────────────────────────────────────────────
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
      queryClient.invalidateQueries({ queryKey: ['plantilla-factura'] });
      setConfigEnEdicion(null);
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

  const guardarHorarioMutation = useMutation({
    mutationFn: async (data) => {
      const payload = {
        ...data,
        horaApertura: data.horaApertura + ':00',
        horaCierre: data.horaCierre + ':00',
      };
      const response = await api.put('/configuracion-horario', payload);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['configuracion-horario'] });
      setHorarioEnEdicion(null);
      setMensaje({ tipo: 'exito', texto: 'Horarios guardados correctamente' });
      setTimeout(() => setMensaje(null), 3000);
    },
    onError: (error) => {
      setMensaje({
        tipo: 'error',
        texto: error.response?.data?.message || 'Error al guardar los horarios'
      });
      setTimeout(() => setMensaje(null), 5000);
    }
  });

  const handleGuardar = () => guardarMutation.mutate(configEnEdicion);

  const handleGuardarHorario = () => {
    if (horarioEnEdicion.horaApertura >= horarioEnEdicion.horaCierre) {
      setMensaje({ 
        tipo: 'error', 
        texto: 'La hora de apertura debe ser menor a la hora de cierre.' 
      });
      return;
    }

    const minutosPorCita = 60 / horarioEnEdicion.citasPorHora;
    if (horarioEnEdicion.duracionCitaMinutos > minutosPorCita) {
      setMensaje({
        tipo: 'error',
        texto: `La duración de ${horarioEnEdicion.duracionCitaMinutos} minutos no es compatible con ${horarioEnEdicion.citasPorHora} citas por hora.`
      });
      return;
    }

    guardarHorarioMutation.mutate(horarioEnEdicion);
  };

  // ── Calcular slots ─────────────────────────────────────────────────────────
  const calcularSlots = () => {
    if (!horarioEnEdicion?.horaApertura || !horarioEnEdicion?.horaCierre) return [];

    const apertura = new Date(`2000-01-01T${horarioEnEdicion.horaApertura}`);
    const cierre = new Date(`2000-01-01T${horarioEnEdicion.horaCierre}`);
    const duracion = horarioEnEdicion.duracionCitaMinutos;
    const slots = [];

    let horaNormal = new Date(apertura);

    while (horaNormal < cierre) {
      const horaFormato = horaNormal.getHours().toString().padStart(2, '0') +
                          ':' + horaNormal.getMinutes().toString().padStart(2, '0');

      const horaNumber = horaNormal.getHours();
      const esMañana = horaNumber < 14;
      const esTarde = horaNumber >= 14;

      let mostrar = false;
      if (horarioEnEdicion.modoHorario === 'COMPLETO') {
        mostrar = true;
      } else if (horarioEnEdicion.modoHorario === 'SOLO_MAÑANA' && esMañana) {
        mostrar = true;
      } else if (horarioEnEdicion.modoHorario === 'SOLO_TARDE' && esTarde) {
        mostrar = true;
      }

      if (mostrar) {
        slots.push(horaFormato);
      }

      horaNormal = new Date(horaNormal.getTime() + duracion * 60000);
    }

    return slots;
  };

  const slots = calcularSlots();

  // ── Opciones para selects ──────────────────────────────────────────────────
  const duracionesDisponibles = [30, 45, 60];
  const citasPorHoraDisponibles = [1, 2, 3, 4];
  const modosHorario = [
    { value: 'SOLO_MAÑANA', label: 'Solo Mañana (hasta 14:00)' },
    { value: 'SOLO_TARDE', label: 'Solo Tarde (desde 14:00)' },
    { value: 'COMPLETO', label: 'Completo (Mañana y Tarde)' },
  ];

  // ── Tabs ───────────────────────────────────────────────────────────────────
  const tabs = [
    { id: 'empresa', label: 'Datos Empresa', icon: Building2 },
    { id: 'factura', label: 'Plantilla Facturas', icon: FileText },
    { id: 'horarios', label: 'Horarios', icon: Clock },
    { id: 'sistema', label: 'Sistema', icon: SettingsIcon }
  ];

  if (isLoading || isLoadingHorarios) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
      </div>
    );
  }

  // ── Render ─────────────────────────────────────────────────────────────────
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
            <p className="text-gray-600 mt-1">Gestiona los ajustes de tu empresa, facturas y horarios</p>
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
                  onClick={() => {
                    setTabActiva(tab.id);
                    if (tab.id === 'empresa') iniciarEdicionEmpresa();
                    if (tab.id === 'horarios') iniciarEdicionHorario();
                  }}
                  className={`flex items-center px-6 py-4 font-medium text-sm whitespace-nowrap border-b-2 transition-colors ${
                    tabActiva === tab.id
                      ? 'text-blue-600 border-blue-600'
                      : 'text-gray-600 border-transparent hover:text-gray-900'
                  }`}
                >
                  <Icon size={18} className="mr-2" />
                  {tab.label}
                </button>
              );
            })}
          </div>
        </div>

        {/* Contenido de Tabs */}
        <div className="p-6">

          {/* ── Tab Empresa ───────────────────────────────────────────────────── */}
          {tabActiva === 'empresa' && configEnEdicion && (
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Datos de la Empresa</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Nombre de la Empresa
                    </label>
                    <input
                      type="text"
                      name="emisorNombre"
                      value={configEnEdicion.emisorNombre}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      NIF/CIF
                    </label>
                    <input
                      type="text"
                      name="emisorNif"
                      value={configEnEdicion.emisorNif}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Dirección
                    </label>
                    <input
                      type="text"
                      name="emisorDireccion"
                      value={configEnEdicion.emisorDireccion}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Código Postal
                    </label>
                    <input
                      type="text"
                      name="emisorCodigoPostal"
                      value={configEnEdicion.emisorCodigoPostal}
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
                      name="emisorCiudad"
                      value={configEnEdicion.emisorCiudad}
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
                      name="emisorProvincia"
                      value={configEnEdicion.emisorProvincia}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Teléfono
                    </label>
                    <input
                      type="text"
                      name="emisorTelefono"
                      value={configEnEdicion.emisorTelefono}
                      onChange={handleChangeEmpresa}
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
                      value={configEnEdicion.emisorEmail}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Sitio Web
                    </label>
                    <input
                      type="url"
                      name="emisorWeb"
                      value={configEnEdicion.emisorWeb}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>
              </div>

              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Logo de la Empresa</h3>
                <div className="flex gap-6 flex-wrap">
                  <div className="flex-1 min-w-[300px]">
                    <label htmlFor="logo-upload" className="block cursor-pointer">
                      <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-500 transition-colors cursor-pointer">
                        <input
                          id="logo-upload"
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

                  {configEnEdicion.logoBase64 && (
                    <div className="flex-1">
                      <div className="border border-gray-300 rounded-lg p-4 relative">
                        <img
                          src={configEnEdicion.logoBase64}
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
          {tabActiva === 'factura' && configEnEdicion && (
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Diseño y Colores</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

                  <div>
                    <label className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        name="mostrarLogo"
                        checked={configEnEdicion.mostrarLogo}
                        onChange={handleChangeEmpresa}
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
                        value={configEnEdicion.colorPrimario}
                        onChange={handleChangeEmpresa}
                        className="w-12 h-10 rounded border border-gray-300"
                      />
                      <input
                        type="text"
                        name="colorPrimario"
                        value={configEnEdicion.colorPrimario}
                        onChange={handleChangeEmpresa}
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
                        value={configEnEdicion.colorSecundario}
                        onChange={handleChangeEmpresa}
                        className="w-12 h-10 rounded border border-gray-300"
                      />
                      <input
                        type="text"
                        name="colorSecundario"
                        value={configEnEdicion.colorSecundario}
                        onChange={handleChangeEmpresa}
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
                      value={configEnEdicion.textoGracias}
                      onChange={handleChangeEmpresa}
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
                      value={configEnEdicion.pieFactura}
                      onChange={handleChangeEmpresa}
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
                      value={configEnEdicion.cuentaBancaria}
                      onChange={handleChangeEmpresa}
                      placeholder="ES00 0000 0000 0000 0000 0000"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono"
                    />
                  </div>

                </div>
              </div>
            </div>
          )}

          {/* ── Tab Horarios ──────────────────────────────────────────────── */}
          {tabActiva === 'horarios' && horarioEnEdicion && (
            <div className="space-y-6">

              {/* Horario del Establecimiento */}
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Horario del Establecimiento</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Hora de Apertura
                    </label>
                    <input
                      type="time"
                      name="horaApertura"
                      value={horarioEnEdicion.horaApertura}
                      onChange={handleChangeHorario}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                    <p className="mt-1 text-xs text-gray-500">Ej: 08:00</p>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Hora de Cierre
                    </label>
                    <input
                      type="time"
                      name="horaCierre"
                      value={horarioEnEdicion.horaCierre}
                      onChange={handleChangeHorario}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                    <p className="mt-1 text-xs text-gray-500">Ej: 20:00</p>
                  </div>
                </div>
              </div>

              {/* Modo de Horario */}
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Modo de Horario</h3>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-3">
                    Selecciona cómo abres el lavadero
                  </label>
                  <div className="space-y-3">
                    {modosHorario.map(modo => (
                      <label key={modo.value} className="flex items-center cursor-pointer">
                        <input
                          type="radio"
                          name="modoHorario"
                          value={modo.value}
                          checked={horarioEnEdicion.modoHorario === modo.value}
                          onChange={handleChangeHorario}
                          className="h-4 w-4 text-blue-500 rounded-full"
                        />
                        <span className="ml-3 text-gray-700">{modo.label}</span>
                      </label>
                    ))}
                  </div>
                </div>
              </div>

              {/* Duración de Citas */}
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Duración y Disponibilidad</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Duración de Cada Cita (minutos)
                    </label>
                    <select
                      name="duracionCitaMinutos"
                      value={horarioEnEdicion.duracionCitaMinutos}
                      onChange={handleChangeHorario}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    >
                      {duracionesDisponibles.map(dur => (
                        <option key={dur} value={dur}>
                          {dur} minutos
                        </option>
                      ))}
                    </select>
                    <p className="mt-1 text-xs text-gray-500">
                      Cuánto tiempo toma cada servicio de lavado
                    </p>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Citas por Hora
                    </label>
                    <select
                      name="citasPorHora"
                      value={horarioEnEdicion.citasPorHora}
                      onChange={handleChangeHorario}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    >
                      {citasPorHoraDisponibles.map(num => (
                        <option key={num} value={num}>
                          {num} {num === 1 ? 'cita' : 'citas'}
                        </option>
                      ))}
                    </select>
                    <p className="mt-1 text-xs text-gray-500">
                      Cuántas citas pueden agendar simultáneamente
                    </p>
                  </div>
                </div>
              </div>

              {/* Preview de Slots */}
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">
                  Vista Previa de Horarios Disponibles
                </h3>
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <p className="text-sm text-gray-600 mb-4">
                    Estos son los slots disponibles que clientes podrán ver en el calendario:
                  </p>
                  <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-2">
                    {slots.length > 0 ? (
                      slots.map((slot, idx) => (
                        <div
                          key={idx}
                          className="bg-white border border-blue-300 rounded px-3 py-2 text-center text-sm font-medium text-blue-700"
                        >
                          {slot}
                        </div>
                      ))
                    ) : (
                      <p className="text-sm text-gray-500 col-span-full">
                        No hay slots disponibles con la configuración actual
                      </p>
                    )}
                  </div>
                  <p className="mt-3 text-xs text-gray-500">
                    Total: <strong>{slots.length}</strong> slots disponibles por día
                  </p>
                </div>
              </div>

              {/* Info adicional */}
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <h4 className="font-semibold text-blue-900 mb-2">💡 Información Útil</h4>
                <ul className="text-sm text-blue-800 space-y-1">
                  <li>• La duración de la cita debe ser compatible con las citas por hora</li>
                  <li>• Los cambios se aplican inmediatamente a las nuevas citas</li>
                  <li>• Las citas ya agendadas mantienen su horario original</li>
                  <li>• Usa "Días Cerrados" para bloquear fechas específicas</li>
                </ul>
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
              if (tabActiva === 'horarios') {
                setHorarioEnEdicion(null);
              } else {
                setConfigEnEdicion(null);
              }
            }}
            className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Cancelar
          </button>
          <button
            onClick={tabActiva === 'horarios' ? handleGuardarHorario : handleGuardar}
            disabled={tabActiva === 'horarios' ? guardarHorarioMutation.isPending : guardarMutation.isPending}
            className="flex items-center px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Save size={20} className="mr-2" />
            {(tabActiva === 'horarios' ? guardarHorarioMutation.isPending : guardarMutation.isPending)
              ? 'Guardando...'
              : 'Guardar Configuración'
            }
          </button>
        </div>
      </div>

    </div>
  );
};

export default Configuracion;