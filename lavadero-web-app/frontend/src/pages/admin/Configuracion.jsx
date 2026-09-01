import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import {
  Building2, FileText, Settings as SettingsIcon, Clock,
  Save, Upload, X, AlertCircle, CheckCircle, Edit2, Check
} from 'lucide-react';
import api from '../../services/api';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const Configuracion = () => {
  const queryClient = useQueryClient();
  const [tabActiva, setTabActiva] = useState('empresa');
  const [mensaje, setMensaje] = useState(null);
  const [configuracionEnEdicion, setConfiguracionEnEdicion] = useState(null);

  // ── Queries ────────────────────────────────────────────────────────────────
  const { data: configuracion, isLoading } = useQuery({
    queryKey: ['plantilla-factura'],
    queryFn: async () => {
      const { data } = await axios.get(`${API_URL}/config/plantilla-factura`);
      return data;
    }
  });

  // Query para obtener todos los horarios
  const { data: horarios = [], isLoading: isLoadingHorarios } = useQuery({
    queryKey: ['horarios'],
    queryFn: async () => {
      const { data } = await api.get('/horarios');
      return data;
    }
  });

  // Query para configuración global (duración citas, citas por hora)
  const { data: configuracionHorarios } = useQuery({
    queryKey: ['configuracion-horario'],
    queryFn: async () => {
      const { data } = await api.get('/configuracion-horario');
      return data;
    }
  });

  // ── Estado local para formulario empresa ────────────────────────────────────
  const [configEnEdicion, setConfigEnEdicion] = useState(null);

  // ── Estado para editar horarios ─────────────────────────────────────────────
  const [horariosEnEdicion, setHorariosEnEdicion] = useState({});
  const [editandoDia, setEditandoDia] = useState(null);

  // ── Handlers para Empresa ──────────────────────────────────────────────────
  const handleChangeEmpresa = (e) => {
    const { name, value, type, checked } = e.target;
    setConfigEnEdicion(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
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

  // ── Handlers para Horarios ─────────────────────────────────────────────────
  const iniciarEdicionHorario = (horario) => {
    setEditandoDia(horario.diaSemana);
    setHorariosEnEdicion({
      [horario.diaSemana]: { ...horario }
    });
  };

  const handleChangeHorario = (diaSemana, field, value) => {
    setHorariosEnEdicion(prev => ({
      ...prev,
      [diaSemana]: {
        ...prev[diaSemana],
        [field]: value === '' ? null : value
      }
    }));
  };

  const cancelarEdicionHorario = () => {
    setEditandoDia(null);
    setHorariosEnEdicion({});
  };

  // ── Mutations ──────────────────────────────────────────────────────────────
  const guardarEmpresaMutation = useMutation({
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
      setMensaje({ tipo: 'exito', texto: 'Configuración de empresa guardada' });
      setTimeout(() => setMensaje(null), 3000);
    },
    onError: (error) => {
      setMensaje({
        tipo: 'error',
        texto: error.response?.data?.mensaje || 'Error al guardar'
      });
    }
  });

  // Mutation para guardar un horario individual
  const guardarHorarioMutation = useMutation({
    mutationFn: async (horario) => {
      const payload = {
        ...horario,
        aperturaMañana: horario.aperturaMañana ? horario.aperturaMañana + ':00' : null,
        cierreMañana: horario.cierreMañana ? horario.cierreMañana + ':00' : null,
        aperturaTarde: horario.aperturaTarde ? horario.aperturaTarde + ':00' : null,
        cierreTarde: horario.cierreTarde ? horario.cierreTarde + ':00' : null,
      };
      const response = await api.put(`/horarios/${horario.diaSemana}`, payload);
      return response.data;
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['horarios'] });
      cancelarEdicionHorario();
      setMensaje({ tipo: 'exito', texto: `Horario de ${data.diaSemana} actualizado` });
      setTimeout(() => setMensaje(null), 3000);
    },
    onError: (error) => {
      setMensaje({
        tipo: 'error',
        texto: error.response?.data?.message || 'Error al guardar horario'
      });
    }
  });

  // ── Inicializar edición cuando cambia de tab ───────────────────────────────
  useState(() => {
    if (tabActiva === 'empresa' && configuracion && !configEnEdicion) {
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
  });

  // ── Tabs ───────────────────────────────────────────────────────────────────
  const tabs = [
    { id: 'empresa', label: 'Datos Empresa', icon: Building2 },
    { id: 'factura', label: 'Plantilla Facturas', icon: FileText },
    { id: 'horarios', label: 'Horarios Semana', icon: Clock },
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
                  onClick={() => setTabActiva(tab.id)}
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

          {/* ── Tab Horarios ──────────────────────────────────────────────────── */}
          {tabActiva === 'horarios' && (
            <div className="space-y-6">

              {/* Info de configuración global */}
              {configuracionHorarios && (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <p className="text-sm text-blue-900">
                    <strong>Duración de citas:</strong> {configuracionHorarios.duracionCitaMinutos} minutos |
                    <strong className="ml-3">Citas por hora:</strong> {configuracionHorarios.citasPorHora}
                  </p>
                </div>
              )}

              {/* Tabla de horarios */}
              <div className="overflow-x-auto">
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="bg-gray-100 border-b-2 border-gray-300">
                      <th className="px-4 py-3 text-left font-semibold text-gray-900">Día</th>
                      <th className="px-4 py-3 text-left font-semibold text-gray-900">Apertura Mañana</th>
                      <th className="px-4 py-3 text-left font-semibold text-gray-900">Cierre Mañana</th>
                      <th className="px-4 py-3 text-left font-semibold text-gray-900">Apertura Tarde</th>
                      <th className="px-4 py-3 text-left font-semibold text-gray-900">Cierre Tarde</th>
                      <th className="px-4 py-3 text-center font-semibold text-gray-900">Estado</th>
                      <th className="px-4 py-3 text-center font-semibold text-gray-900">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {horarios.map((horario, idx) => {
                      const editando = editandoDia === horario.diaSemana;
                      const datosEdicion = horariosEnEdicion[horario.diaSemana];

                      return (
                        <tr key={horario.diaSemana} className={idx % 2 === 0 ? 'bg-gray-50' : 'bg-white'}>
                          <td className="px-4 py-3 font-medium text-gray-900">{horario.diaSemana}</td>

                          {/* Apertura Mañana */}
                          <td className="px-4 py-3">
                            {editando ? (
                              <input
                                type="time"
                                value={datosEdicion.aperturaMañana || ''}
                                onChange={(e) => handleChangeHorario(horario.diaSemana, 'aperturaMañana', e.target.value)}
                                className="w-24 px-2 py-1 border border-gray-300 rounded"
                              />
                            ) : (
                              <span className="text-gray-700">{horario.aperturaMañana || '—'}</span>
                            )}
                          </td>

                          {/* Cierre Mañana */}
                          <td className="px-4 py-3">
                            {editando ? (
                              <input
                                type="time"
                                value={datosEdicion.cierreMañana || ''}
                                onChange={(e) => handleChangeHorario(horario.diaSemana, 'cierreMañana', e.target.value)}
                                className="w-24 px-2 py-1 border border-gray-300 rounded"
                              />
                            ) : (
                              <span className="text-gray-700">{horario.cierreMañana || '—'}</span>
                            )}
                          </td>

                          {/* Apertura Tarde */}
                          <td className="px-4 py-3">
                            {editando ? (
                              <input
                                type="time"
                                value={datosEdicion.aperturaTarde || ''}
                                onChange={(e) => handleChangeHorario(horario.diaSemana, 'aperturaTarde', e.target.value)}
                                className="w-24 px-2 py-1 border border-gray-300 rounded"
                              />
                            ) : (
                              <span className="text-gray-700">{horario.aperturaTarde || '—'}</span>
                            )}
                          </td>

                          {/* Cierre Tarde */}
                          <td className="px-4 py-3">
                            {editando ? (
                              <input
                                type="time"
                                value={datosEdicion.cierreTarde || ''}
                                onChange={(e) => handleChangeHorario(horario.diaSemana, 'cierreTarde', e.target.value)}
                                className="w-24 px-2 py-1 border border-gray-300 rounded"
                              />
                            ) : (
                              <span className="text-gray-700">{horario.cierreTarde || '—'}</span>
                            )}
                          </td>

                          {/* Estado */}
                          <td className="px-4 py-3 text-center">
                            {horario.activo ? (
                              <span className="inline-block px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-medium">
                                Abierto
                              </span>
                            ) : (
                              <span className="inline-block px-3 py-1 bg-red-100 text-red-800 rounded-full text-sm font-medium">
                                Cerrado
                              </span>
                            )}
                          </td>

                          {/* Acciones */}
                          <td className="px-4 py-3 text-center">
                            {editando ? (
                              <div className="flex gap-2 justify-center">
                                <button
                                  onClick={() => guardarHorarioMutation.mutate(datosEdicion)}
                                  disabled={guardarHorarioMutation.isPending}
                                  className="p-2 bg-green-500 text-white rounded hover:bg-green-600 disabled:opacity-50"
                                  title="Guardar"
                                >
                                  <Check size={18} />
                                </button>
                                <button
                                  onClick={cancelarEdicionHorario}
                                  className="p-2 bg-gray-500 text-white rounded hover:bg-gray-600"
                                  title="Cancelar"
                                >
                                  <X size={18} />
                                </button>
                              </div>
                            ) : (
                              <button
                                onClick={() => iniciarEdicionHorario(horario)}
                                className="p-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                                title="Editar"
                              >
                                <Edit2 size={18} />
                              </button>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>

              {/* Info */}
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <h4 className="font-semibold text-blue-900 mb-2">💡 Información</h4>
                <ul className="text-sm text-blue-800 space-y-1">
                  <li>• Deja en blanco para cerrar ese horario (ej: viernes sin tarde)</li>
                  <li>• Debe haber separación entre cierre de mañana y apertura de tarde</li>
                  <li>• Los cambios se aplican a nuevas citas automáticamente</li>
                </ul>
              </div>
            </div>
          )}

          {/* ── Tab Empresa ───────────────────────────────────────────────────── */}
          {tabActiva === 'empresa' && configEnEdicion && (
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Datos de la Empresa</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Nombre</label>
                    <input
                      type="text"
                      name="emisorNombre"
                      value={configEnEdicion.emisorNombre}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">NIF/CIF</label>
                    <input
                      type="text"
                      name="emisorNif"
                      value={configEnEdicion.emisorNif}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">Dirección</label>
                    <input
                      type="text"
                      name="emisorDireccion"
                      value={configEnEdicion.emisorDireccion}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Código Postal</label>
                    <input
                      type="text"
                      name="emisorCodigoPostal"
                      value={configEnEdicion.emisorCodigoPostal}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Ciudad</label>
                    <input
                      type="text"
                      name="emisorCiudad"
                      value={configEnEdicion.emisorCiudad}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Provincia</label>
                    <input
                      type="text"
                      name="emisorProvincia"
                      value={configEnEdicion.emisorProvincia}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                    <input
                      type="text"
                      name="emisorTelefono"
                      value={configEnEdicion.emisorTelefono}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                    <input
                      type="email"
                      name="emisorEmail"
                      value={configEnEdicion.emisorEmail}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">Sitio Web</label>
                    <input
                      type="url"
                      name="emisorWeb"
                      value={configEnEdicion.emisorWeb}
                      onChange={handleChangeEmpresa}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </div>
              </div>

              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">Logo</h3>
                <div className="flex gap-6">
                  <div className="flex-1">
                    <label htmlFor="logo-upload" className="block cursor-pointer">
                      <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-500">
                        <input
                          id="logo-upload"
                          type="file"
                          accept="image/png,image/jpeg"
                          onChange={handleLogoUpload}
                          className="hidden"
                        />
                        <Upload className="mx-auto text-gray-400 mb-2" size={40} />
                        <p className="text-sm text-gray-600">Click para subir</p>
                      </div>
                    </label>
                  </div>
                  {configEnEdicion.logoBase64 && (
                    <div className="flex-1">
                      <img
                        src={configEnEdicion.logoBase64}
                        alt="Logo"
                        className="max-w-full h-32 object-contain"
                      />
                      <button
                        onClick={handleEliminarLogo}
                        className="mt-2 px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600 text-sm"
                      >
                        Eliminar
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* ── Tab Factura y Sistema ─ (reducido para brevedad) ──────────────── */}
          {tabActiva === 'factura' && configEnEdicion && (
            <div>
              <h3 className="text-lg font-bold text-gray-900 mb-4">Plantilla de Facturas</h3>
              <p className="text-gray-600">Configuración de colores y textos para facturas...</p>
            </div>
          )}

          {tabActiva === 'sistema' && (
            <div>
              <h3 className="text-lg font-bold text-gray-900 mb-4">Sistema</h3>
              <p className="text-gray-600">Versión 1.0.0 | Mayo 2026</p>
            </div>
          )}

        </div>
      </div>

      {/* Botones de acción */}
      {tabActiva === 'empresa' && configEnEdicion && (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-end gap-4">
            <button
              onClick={() => setConfigEnEdicion(null)}
              className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              onClick={() => guardarEmpresaMutation.mutate(configEnEdicion)}
              disabled={guardarEmpresaMutation.isPending}
              className="flex items-center px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
              <Save size={20} className="mr-2" />
              {guardarEmpresaMutation.isPending ? 'Guardando...' : 'Guardar'}
            </button>
          </div>
        </div>
      )}

    </div>
  );
};

export default Configuracion;