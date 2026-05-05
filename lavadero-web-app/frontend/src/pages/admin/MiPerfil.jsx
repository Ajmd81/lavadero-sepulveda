import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { User, Lock, Mail, CreditCard, Loader2 } from 'lucide-react';

const MiPerfil = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [cargando, setCargando] = useState(true);

  const [datosForm, setDatosForm] = useState({
    nombreCompleto: '',
    email: '',
    dni: ''
  });

  const [usernameForm, setUsernameForm] = useState(user?.username || '');

  const [passwordForm, setPasswordForm] = useState({
    passwordActual: '',
    passwordNueva: '',
    passwordConfirm: ''
  });

  // ── Cargar datos reales del perfil al montar ──────────────────────────────
  useEffect(() => {
    const cargarPerfil = async () => {
      try {
        const res = await api.get(`/auth/perfil?username=${user.username}`);
        setDatosForm({
          nombreCompleto: res.data.nombreCompleto || '',
          email: res.data.email || '',
          dni: res.data.dni || ''
        });
        setUsernameForm(res.data.username || user?.username || '');
      } catch (error) {
        console.error('Error al cargar perfil:', error);
        alert('❌ No se pudieron cargar los datos del perfil');
      } finally {
        setCargando(false);
      }
    };
    cargarPerfil();
  }, []);

  // ── Actualizar datos personales ───────────────────────────────────────────
  const handleActualizarDatos = async (e) => {
    e.preventDefault();

    // Validación básica de DNI en frontend (el backend también valida)
    if (datosForm.dni && !/^[0-9]{8}[A-Z]$/.test(datosForm.dni)) {
      alert('❌ Formato de DNI inválido. Ejemplo correcto: 12345678A');
      return;
    }

    try {
      await api.put('/auth/perfil/datos', {
        username: user.username,
        nombreCompleto: datosForm.nombreCompleto,
        email: datosForm.email,
        dni: datosForm.dni
      });
      alert('✅ Datos actualizados correctamente');
    } catch (error) {
      alert('❌ ' + (error.response?.data?.error || error.message));
    }
  };

  // ── Cambiar username ──────────────────────────────────────────────────────
  const handleCambiarUsername = async (e) => {
    e.preventDefault();

    if (!window.confirm('¿Cambiar tu username? Tendrás que volver a iniciar sesión.')) {
      return;
    }

    try {
      await api.put('/auth/perfil/username', {
        currentUsername: user.username,
        nuevoUsername: usernameForm
      });
      alert('✅ Username actualizado. Inicia sesión nuevamente.');
      logout();
      navigate('/admin/login');
    } catch (error) {
      alert('❌ ' + (error.response?.data?.error || error.message));
    }
  };

  // ── Cambiar contraseña ────────────────────────────────────────────────────
  const handleCambiarPassword = async (e) => {
    e.preventDefault();

    if (passwordForm.passwordNueva !== passwordForm.passwordConfirm) {
      alert('❌ Las contraseñas no coinciden');
      return;
    }

    if (passwordForm.passwordNueva.length < 6) {
      alert('❌ La contraseña debe tener al menos 6 caracteres');
      return;
    }

    try {
      await api.put('/auth/perfil/password', {
        username: user.username,
        passwordActual: passwordForm.passwordActual,
        passwordNueva: passwordForm.passwordNueva
      });
      alert('✅ Contraseña actualizada correctamente');
      setPasswordForm({ passwordActual: '', passwordNueva: '', passwordConfirm: '' });
    } catch (error) {
      alert('❌ ' + (error.response?.data?.error || error.message));
    }
  };

  // ── Loading ───────────────────────────────────────────────────────────────
  if (cargando) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="animate-spin text-blue-600" size={40} />
      </div>
    );
  }

  // ── Render ────────────────────────────────────────────────────────────────
  return (
    <div className="space-y-6">

      {/* Header */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center gap-3">
          <User size={32} className="text-blue-600" />
          <div>
            <h1 className="text-3xl font-bold">Mi Perfil</h1>
            <p className="text-gray-500">Usuario: {user?.username}</p>
          </div>
        </div>
      </div>

      {/* Datos Personales */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
          <User className="text-blue-600" />
          Datos Personales
        </h2>

        <form onSubmit={handleActualizarDatos} className="space-y-4">

          {/* Nombre completo */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Nombre Completo
            </label>
            <input
              type="text"
              value={datosForm.nombreCompleto}
              onChange={(e) => setDatosForm({ ...datosForm, nombreCompleto: e.target.value })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              placeholder="Tu nombre completo"
            />
          </div>

          {/* Email */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Email
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-3 text-gray-400" size={16} />
              <input
                type="email"
                value={datosForm.email}
                onChange={(e) => setDatosForm({ ...datosForm, email: e.target.value })}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="tu@email.com"
              />
            </div>
          </div>

          {/* DNI */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              DNI / NIF
            </label>
            <div className="relative">
              <CreditCard className="absolute left-3 top-3 text-gray-400" size={16} />
              <input
                type="text"
                value={datosForm.dni}
                onChange={(e) =>
                  setDatosForm({ ...datosForm, dni: e.target.value.toUpperCase() })
                }
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="12345678A"
                maxLength={9}
              />
            </div>
            <p className="text-xs text-gray-400 mt-1">8 dígitos seguidos de una letra mayúscula</p>
          </div>

          <button
            type="submit"
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors font-semibold"
          >
            Guardar Cambios
          </button>
        </form>
      </div>

      {/* Grid: Cambiar Username + Cambiar Contraseña */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* Cambiar Username */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
            <User className="text-orange-600" />
            Cambiar Username
          </h2>

          <div className="mb-4 p-3 bg-orange-50 border border-orange-200 rounded text-sm text-orange-800">
            ⚠️ Tendrás que volver a iniciar sesión tras el cambio
          </div>

          <form onSubmit={handleCambiarUsername} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Nuevo Username
              </label>
              <input
                type="text"
                value={usernameForm}
                onChange={(e) => setUsernameForm(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500"
                required
              />
            </div>
            <button
              type="submit"
              className="w-full bg-orange-600 text-white px-4 py-2 rounded-lg hover:bg-orange-700 transition-colors font-semibold"
            >
              Cambiar Username
            </button>
          </form>
        </div>

        {/* Cambiar Contraseña */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
            <Lock className="text-red-600" />
            Cambiar Contraseña
          </h2>

          <form onSubmit={handleCambiarPassword} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Contraseña Actual *
              </label>
              <input
                type="password"
                placeholder="Tu contraseña actual"
                value={passwordForm.passwordActual}
                onChange={(e) =>
                  setPasswordForm({ ...passwordForm, passwordActual: e.target.value })
                }
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Nueva Contraseña * <span className="text-gray-400">(mín. 6 caracteres)</span>
              </label>
              <input
                type="password"
                placeholder="Nueva contraseña"
                value={passwordForm.passwordNueva}
                onChange={(e) =>
                  setPasswordForm({ ...passwordForm, passwordNueva: e.target.value })
                }
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                required
                minLength={6}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Confirmar Nueva Contraseña *
              </label>
              <input
                type="password"
                placeholder="Confirmar contraseña"
                value={passwordForm.passwordConfirm}
                onChange={(e) =>
                  setPasswordForm({ ...passwordForm, passwordConfirm: e.target.value })
                }
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                required
                minLength={6}
              />
            </div>

            <button
              type="submit"
              className="w-full bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors font-semibold"
            >
              Cambiar Contraseña
            </button>
          </form>
        </div>

      </div>
    </div>
  );
};

export default MiPerfil;