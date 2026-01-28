import { useState } from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { LogOut, Menu, X, Settings } from 'lucide-react';

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
    { icon: '/assets/icons/panel.png', label: 'Dashboard', path: '/admin/dashboard' },
    { icon: '/assets/icons/cliente.png', label: 'Clientes', path: '/admin/clientes' },
    { icon: '/assets/icons/citas.png', label: 'Citas', path: '/admin/citas' },
    { icon: '/assets/icons/calendario.png?v=1', label: 'Calendario', path: '/admin/calendario' },
    { icon: '/assets/icons/facturacion.png', label: 'Facturación', path: '/admin/facturacion' },
    { icon: '/assets/icons/proveedor.png', label: 'Proveedores', path: '/admin/proveedores' },
    { icon: '/assets/icons/invoice.png', label: 'Gastos', path: '/admin/gastos' },
    { icon: '/assets/icons/contabilidad.png', label: 'Contabilidad', path: '/admin/contabilidad' },
    { icon: '/assets/icons/estado-financiero.png', label: 'Resumen financiero', path: '/admin/resumen-financiero' },
    { icon: '/assets/icons/modeloFiscal.png', label: 'Modelos fiscales', path: '/admin/modelos-fiscales' },
  ];

  const isActive = (path) => {
    if (path === '/admin') return location.pathname === path;
    return location.pathname.startsWith(path);
  };

  return (
    <div className="min-h-screen bg-gray-100 flex">
      <aside className={`bg-gradient-to-b from-blue-900 to-blue-800 text-white transition-all duration-300 ${sidebarOpen ? 'w-72' : 'w-20'} flex flex-col shadow-2xl`}>
        <div className="p-4 border-b border-blue-700">
          <div className="flex items-center justify-between mb-3">
            {sidebarOpen && (
              <div className="flex items-center gap-3 flex-1">
                <img src="/assets/icons/logo_crm.png" alt="Logo" className="w-12 h-12 object-contain" />
                <div>
                  <h1 className="text-lg font-bold leading-tight">Lavadero</h1>
                  <p className="text-xs text-blue-300">CRM Web</p>
                </div>
              </div>
            )}
            {!sidebarOpen && (
              <img src="/assets/icons/logo_crm.png" alt="Logo" className="w-10 h-10 object-contain mx-auto" />
            )}
            <button onClick={() => setSidebarOpen(!sidebarOpen)} className="p-2 hover:bg-blue-700 rounded-lg transition-colors flex-shrink-0">
              {sidebarOpen ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>

        <nav className="flex-1 p-3 overflow-y-auto">
          {menuItems.map((item) => (
            <Link key={item.path} to={item.path} className={`flex items-center gap-3 p-3 rounded-lg mb-1 transition-all ${isActive(item.path) ? 'bg-blue-700 shadow-lg' : 'hover:bg-blue-700/50'}`} title={!sidebarOpen ? item.label : ''}>
              <img src={item.icon} alt={item.label} className="w-8 h-8 object-contain flex-shrink-0" />
              {sidebarOpen && <span className="font-medium text-sm">{item.label}</span>}
            </Link>
          ))}

          <Link to="/admin/configuracion" className={`flex items-center gap-3 p-3 rounded-lg mb-1 transition-all ${isActive('/admin/configuracion') ? 'bg-blue-700 shadow-lg' : 'hover:bg-blue-700/50'}`} title={!sidebarOpen ? 'Configuración' : ''}>
            <img src="/assets/icons/configuracion.png" alt="Configuración" className="w-8 h-8 object-contain flex-shrink-0" />
            {sidebarOpen && <span className="font-medium text-sm">Configuración</span>}
          </Link>
        </nav>

        <div className="p-4 border-t border-blue-700">
          {sidebarOpen ? (
            <div className="mb-3">
              <p className="text-xs text-blue-300">Usuario:</p>
              <p className="font-medium text-sm truncate">{user?.username || 'Admin'}</p>
            </div>
          ) : (
            <div className="w-10 h-10 bg-blue-700 rounded-full flex items-center justify-center mx-auto mb-3">
              <span className="text-lg font-bold">{(user?.username || 'A')[0].toUpperCase()}</span>
            </div>
          )}
          <button onClick={handleLogout} className={`w-full flex items-center gap-2 p-2 bg-red-600 hover:bg-red-700 rounded-lg transition-colors ${!sidebarOpen ? 'justify-center' : ''}`} title={!sidebarOpen ? 'Cerrar Sesión' : ''}>
            <LogOut size={20} />
            {sidebarOpen && <span className="text-sm">Cerrar Sesión</span>}
          </button>
        </div>
      </aside>

      <main className="flex-1 overflow-auto">
        <div className="container mx-auto p-6">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default AdminLayout;