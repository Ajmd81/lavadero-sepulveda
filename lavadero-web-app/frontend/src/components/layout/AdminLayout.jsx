import { useState } from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { LogOut, Menu, X, Settings, ChevronRight } from 'lucide-react';

const AdminLayout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const handleLogout = () => {
    logout();
    navigate('/admin/login');
  };

  const menuItems = [
    { icon: '/assets/icons/panel.png', label: 'Dashboard', path: '/admin/dashboard', category: 'principal' },
    { icon: '/assets/icons/cliente.png', label: 'Clientes', path: '/admin/clientes', category: 'gestión' },
    { icon: '/assets/icons/citas.png', label: 'Citas', path: '/admin/citas', category: 'gestión' },
    { icon: '/assets/icons/calendario.png?v=1', label: 'Calendario', path: '/admin/calendario', category: 'gestión' },
    { icon: '/assets/icons/facturacion.png', label: 'Facturación', path: '/admin/facturacion', category: 'financiero' },
    { icon: '/assets/icons/proveedor.png', label: 'Proveedores', path: '/admin/proveedores', category: 'gestión' },
    { icon: '/assets/icons/contabilidad.png', label: 'Contabilidad', path: '/admin/contabilidad', category: 'financiero' },
    { icon: '/assets/icons/estado-financiero.png', label: 'Resumen financiero', path: '/admin/resumen-financiero', category: 'financiero' },
    { icon: '/assets/icons/modeloFiscal.png', label: 'Modelos fiscales', path: '/admin/modelos-fiscales', category: 'financiero' },
    { icon: '/assets/icons/perfil.png', label: 'Mi Perfil', path: '/admin/mi-perfil', category: 'usuario' },
  ];

  const isActive = (path) => {
    if (path === '/admin') return location.pathname === path;
    return location.pathname.startsWith(path);
  };

  const getCategoryGradient = (category) => {
    const gradients = {
      principal: 'from-amber-500 to-orange-500',
      gestión: 'from-cyan-500 to-blue-500',
      financiero: 'from-emerald-500 to-teal-500',
      usuario: 'from-purple-500 to-pink-500'
    };
    return gradients[category] || 'from-blue-500 to-cyan-500';
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 flex">
      <aside className={`bg-gradient-to-b from-slate-900 via-slate-800 to-slate-900 text-white transition-all duration-500 ${sidebarOpen ? 'w-72' : 'w-20'} flex flex-col shadow-2xl border-r border-slate-700`}>
        {/* Header con Logo */}
        <div className="p-4 border-b border-slate-700 bg-gradient-to-r from-slate-800 to-slate-700">
          <div className="flex items-center justify-between mb-3">
            {sidebarOpen && (
              <div className="flex items-center gap-3 flex-1">
                <div className="relative">
                  <div className="absolute inset-0 bg-gradient-to-r from-amber-500 to-orange-500 rounded-lg opacity-75 blur"></div>
                  <img src="/assets/icons/logo_crm.png" alt="Logo" className="relative w-12 h-12 object-contain" />
                </div>
                <div>
                  <h1 className="text-lg font-bold leading-tight bg-gradient-to-r from-amber-400 to-orange-400 bg-clip-text text-transparent">Lavadero</h1>
                  <p className="text-xs text-slate-400">CRM Web</p>
                </div>
              </div>
            )}
            {!sidebarOpen && (
              <div className="relative mx-auto">
                <div className="absolute inset-0 bg-gradient-to-r from-amber-500 to-orange-500 rounded-lg opacity-50 blur"></div>
                <img src="/assets/icons/logo_crm.png" alt="Logo" className="relative w-10 h-10 object-contain" />
              </div>
            )}
            <button onClick={() => setSidebarOpen(!sidebarOpen)} className="p-2 hover:bg-slate-700 rounded-lg transition-all duration-300 flex-shrink-0 hover:scale-110">
              {sidebarOpen ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>

        {/* Navegación */}
        <nav className="flex-1 p-3 overflow-y-auto scrollbar-hide">
          {menuItems.map((item) => (
            <Link 
              key={item.path} 
              to={item.path}
              className={`flex items-center gap-3 px-4 py-3 rounded-xl mb-2 transition-all duration-300 relative group overflow-hidden ${
                isActive(item.path) 
                  ? `bg-gradient-to-r ${getCategoryGradient(item.category)} shadow-lg shadow-slate-900/50 scale-105` 
                  : 'hover:bg-slate-700/50 hover:translate-x-1'
              }`}
              title={!sidebarOpen ? item.label : ''}
            >
              {/* Efecto de brillo en hover */}
              {!isActive(item.path) && (
                <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white to-transparent opacity-0 group-hover:opacity-10 transition-opacity duration-300"></div>
              )}
              
              {/* Indicador activo */}
              {isActive(item.path) && (
                <div className="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-white via-white/80 to-transparent"></div>
              )}
              
              <img src={item.icon} alt={item.label} className={`w-8 h-8 object-contain flex-shrink-0 transition-transform duration-300 ${isActive(item.path) ? 'scale-110' : 'group-hover:scale-110'}`} />
              {sidebarOpen && (
                <div className="flex-1 flex items-center justify-between">
                  <span className="font-semibold text-sm">{item.label}</span>
                  {isActive(item.path) && <ChevronRight size={18} />}
                </div>
              )}
            </Link>
          ))}

          {/* Separador visual */}
          {sidebarOpen && <div className="my-4 h-px bg-gradient-to-r from-slate-700 via-slate-600 to-slate-700"></div>}

          {/* Configuración */}
          <Link 
            to="/admin/configuracion"
            className={`flex items-center gap-3 px-4 py-3 rounded-xl mb-2 transition-all duration-300 relative group overflow-hidden ${
              isActive('/admin/configuracion') 
                ? `bg-gradient-to-r from-purple-500 to-pink-500 shadow-lg shadow-slate-900/50 scale-105` 
                : 'hover:bg-slate-700/50 hover:translate-x-1'
            }`}
            title={!sidebarOpen ? 'Configuración' : ''}
          >
            {!isActive('/admin/configuracion') && (
              <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white to-transparent opacity-0 group-hover:opacity-10 transition-opacity duration-300"></div>
            )}
            
            {isActive('/admin/configuracion') && (
              <div className="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-white via-white/80 to-transparent"></div>
            )}
            
            <img src="/assets/icons/configuracion.png" alt="Configuración" className={`w-8 h-8 object-contain flex-shrink-0 transition-transform duration-300 ${isActive('/admin/configuracion') ? 'scale-110 rotate-180' : 'group-hover:scale-110 group-hover:rotate-90'}`} />
            {sidebarOpen && (
              <div className="flex-1 flex items-center justify-between">
                <span className="font-semibold text-sm">Configuración</span>
                {isActive('/admin/configuracion') && <ChevronRight size={18} />}
              </div>
            )}
          </Link>
        </nav>

        {/* Footer - Usuario y Logout */}
        <div className="p-4 border-t border-slate-700 bg-gradient-to-r from-slate-800 to-slate-700">
          {sidebarOpen ? (
            <div className="mb-3">
              <p className="text-xs text-slate-400 uppercase tracking-wider">Usuario</p>
              <p className="font-semibold text-sm truncate text-white mt-1">{user?.username || 'Admin'}</p>
            </div>
          ) : (
            <div className="w-10 h-10 bg-gradient-to-br from-amber-500 to-orange-500 rounded-full flex items-center justify-center mx-auto mb-3 shadow-lg">
              <span className="text-lg font-bold text-white">{(user?.username || 'A')[0].toUpperCase()}</span>
            </div>
          )}
          <button 
            onClick={handleLogout} 
            className={`w-full flex items-center gap-2 p-3 bg-gradient-to-r from-red-600 to-red-700 hover:from-red-700 hover:to-red-800 rounded-xl transition-all duration-300 group hover:shadow-lg hover:shadow-red-700/50 hover:scale-105 ${!sidebarOpen ? 'justify-center' : ''}`}
            title={!sidebarOpen ? 'Cerrar Sesión' : ''}
          >
            <LogOut size={20} className="group-hover:animate-bounce" />
            {sidebarOpen && <span className="text-sm font-semibold">Cerrar Sesión</span>}
          </button>
        </div>
      </aside>

      {/* Contenido Principal */}
      <main className="flex-1 overflow-auto">
        <div className="container mx-auto p-6">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default AdminLayout;