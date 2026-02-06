import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { FiUser, FiLock } from 'react-icons/fi';

const MiPerfil = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  
  // ✅ Inicializar directamente con el valor del usuario
  const [usernameForm, setUsernameForm] = useState(user?.username || '');
  const [passwordForm, setPasswordForm] = useState({
    passwordActual: '',
    passwordNueva: '',
    passwordConfirm: ''
  });

  // ✅ NO USAR useEffect para setUsernameForm

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

  const handleCambiarPassword = async (e) => {
    e.preventDefault();
    
    if (passwordForm.passwordNueva !== passwordForm.passwordConfirm) {
      alert('Las contraseñas no coinciden');
      return;
    }
    
    if (passwordForm.passwordNueva.length < 6) {
      alert('La contraseña debe tener al menos 6 caracteres');
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

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center gap-3">
          <FiUser size={32} className="text-blue-600" />
          <div>
            <h1 className="text-3xl font-bold">Mi Perfil</h1>
            <p className="text-gray-500">Usuario: {user?.username}</p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Cambiar Username */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
            <FiUser className="text-orange-600" />
            Cambiar Username
          </h2>
          
          <div className="mb-4 p-3 bg-orange-50 border border-orange-200 rounded text-sm text-orange-800">
            ⚠️ Tendrás que volver a iniciar sesión
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
              className="w-full bg-orange-600 text-white px-4 py-2 rounded-lg hover:bg-orange-700 transition-colors"
            >
              Cambiar Username
            </button>
          </form>
        </div>

        {/* Cambiar Contraseña */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
            <FiLock className="text-red-600" />
            Cambiar Contraseña
          </h2>
          
          <form onSubmit={handleCambiarPassword} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Contraseña Actual *
              </label>
              <input
                type="password"
                placeholder="Introduce tu contraseña actual"
                value={passwordForm.passwordActual}
                onChange={(e) => setPasswordForm({ ...passwordForm, passwordActual: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                required
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Nueva Contraseña * (mínimo 6 caracteres)
              </label>
              <input
                type="password"
                placeholder="Introduce la nueva contraseña"
                value={passwordForm.passwordNueva}
                onChange={(e) => setPasswordForm({ ...passwordForm, passwordNueva: e.target.value })}
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
                placeholder="Confirma la nueva contraseña"
                value={passwordForm.passwordConfirm}
                onChange={(e) => setPasswordForm({ ...passwordForm, passwordConfirm: e.target.value })}
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