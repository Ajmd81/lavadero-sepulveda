import { useState } from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { LogOut, Menu, X, Settings } from 'lucide-react';
import CustomIcon from '../../components/CustomIcon';

const AdminLayout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const handleLogout = () => {
    logout();
    navigate('/admin/login');
  };

  // Menú con iconos personalizados del CRM
  const menuItems = [
    { 
      customIcon: 'dashboard', 
      label: 'Dashboard', 
      path: '/admin' 
    },
    { 
      customIcon: 'cliente', 
      label: 'Clientes', 
      path: '/admin/clientes' 
    },
    { 
      customIcon: 'citas', 
      label: 'Citas', 
      path: '/admin/citas' 
    },
    { 
      customIcon: 'citas', 
      label: 'Calendario', 
      path: '/admin/calendario' 
    },
    { 
      customIcon: 'facturacion', 
      label: 'Facturación', 
      path: '/admin/facturacion' 
    },
    { 
      customIcon: 'proveedor', 
      label: 'Proveedores', 
      path: '/admin/proveedores' 
    },
    { 
      customIcon: 'modeloFiscal', 
      label: 'Gastos', 
      path: '/admin/gastos' 
    },
    { 
      customIcon: 'contabilidad', 
      label: 'Contabilidad', 
      path: '/admin/contabilidad' 
    },
    { 
      customIcon: 'estadoFinanciero', 
      label: 'Resumen Financiero', 
      path: '/admin/resumen-financiero' 
    },
  ];

  const isActive = (path) => {
    if (path === '/admin') {
      return location.pathname === path;
    }
    return location.pathname.startsWith(path);
  };

  return (
    <div className="min-h-screen bg-gray-100 flex">
      {/* Sidebar */}
      <aside className={`bg-gradient-to-b from-blue-900 to-blue-800 text-white transition-all duration-300 ${ 
        sidebarOpen ? 'w-64' : 'w-20'
      } flex flex-col shadow-2xl`}>
        
        {/* Header del Sidebar */}
        <div className="p-4 flex items-center justify-between border-b border-blue-700">
          {sidebarOpen && (
            <div className="flex items-center gap-3">
              <CustomIcon name="logo" size={40} />
              <div>
                <h1 className="text-xl font-bold">Lavadero</h1>
                <p className="text-xs text-blue-300">CRM Web</p>
              </div>
            </div>
          )}
          {!sidebarOpen && (
            <CustomIcon name="logo" size={40} className="mx-auto" />
          )}
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-2 hover:bg-blue-700 rounded-lg transition-colors"
          >
            {sidebarOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>

        {/* Navegación */}
        <nav className="flex-1 p-4 overflow-y-auto">
          {menuItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`flex items-center gap-3 p-3 rounded-lg mb-2 transition-all ${
                isActive(item.path)
                  ? 'bg-blue-700 shadow-lg scale-105'
                  : 'hover:bg-blue-700/50'
              }`}
              title={!sidebarOpen ? item.label : ''}
            >
              <CustomIcon 
                name={item.customIcon} 
                size={24} 
                className={`${!sidebarOpen ? 'mx-auto' : ''}`}
              />
              {sidebarOpen && (
                <span className="font-medium">{item.label}</span>
              )}
            </Link>
          ))}

          {/* Configuración */}
          <Link
            to="/admin/configuracion"
            className={`flex items-center gap-3 p-3 rounded-lg mb-2 transition-all ${
              isActive('/admin/configuracion')
                ? 'bg-blue-700 shadow-lg scale-105'
                : 'hover:bg-blue-700/50'
            }`}
            title={!sidebarOpen ? 'Configuración' : ''}
          >
            <Settings size={24} className={`${!sidebarOpen ? 'mx-auto' : ''}`} />
            {sidebarOpen && (
              <span className="font-medium">Configuración</span>
            )}
          </Link>
        </nav>

        {/* Footer del Sidebar */}
        <div className="p-4 border-t border-blue-700">
          {sidebarOpen ? (
            <div className="mb-3">
              <p className="text-sm text-blue-300">Usuario:</p>
              <p className="font-medium truncate">{user?.username || 'Admin'}</p>
            </div>
          ) : (
            <div className="w-10 h-10 bg-blue-700 rounded-full flex items-center justify-center mx-auto mb-3">
              <span className="text-lg font-bold">
                {(user?.username || 'A')[0].toUpperCase()}
              </span>
            </div>
          )}
          
          <button
            onClick={handleLogout}
            className={`w-full flex items-center gap-2 p-2 bg-red-600 hover:bg-red-700 rounded-lg transition-colors ${
              !sidebarOpen ? 'justify-center' : ''
            }`}
            title={!sidebarOpen ? 'Cerrar Sesión' : ''}
          >
            <LogOut size={20} />
            {sidebarOpen && <span>Cerrar Sesión</span>}
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
