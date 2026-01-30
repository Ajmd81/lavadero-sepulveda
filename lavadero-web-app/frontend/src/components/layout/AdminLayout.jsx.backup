import { useState } from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { 
  LayoutDashboard, Calendar, Users, FileText, DollarSign, 
  Package, TrendingUp, Settings, LogOut, Menu, X, CalendarDays
} from 'lucide-react';

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
    { icon: LayoutDashboard, label: 'Dashboard', path: '/admin' },
    { icon: Users, label: 'Clientes', path: '/admin/clientes' },
    { icon: Calendar, label: 'Citas', path: '/admin/citas' },
    { icon: CalendarDays, label: 'Calendario', path: '/admin/calendario' },
    { icon: FileText, label: 'Facturación', path: '/admin/facturacion' },
    { icon: Package, label: 'Proveedores', path: '/admin/proveedores' },
    { icon: DollarSign, label: 'Gastos', path: '/admin/gastos' },
    { icon: TrendingUp, label: 'Contabilidad', path: '/admin/contabilidad' },
    { icon: TrendingUp, label: 'Resumen Financiero', path: '/admin/resumen-financiero' },
    { icon: Settings, label: 'Configuración', path: '/admin/configuracion' },
  ];

  const isActive = (path) => {
    return location.pathname === path;
  };

  return (
    <div className="min-h-screen bg-gray-100 flex">
      {/* Sidebar */}
      <aside className={`bg-blue-900 text-white transition-all duration-300 ${ 
        sidebarOpen ? 'w-64' : 'w-20'
      } flex flex-col`}>
        <div className="p-4 flex items-center justify-between border-b border-blue-800">
          {sidebarOpen && <h1 className="text-xl font-bold">CRM Lavadero</h1>}
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-2 hover:bg-blue-800 rounded"
          >
            {sidebarOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>

        <nav className="flex-1 p-4">
          {menuItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`flex items-center gap-3 p-3 rounded mb-2 transition-colors ${
                isActive(item.path)
                  ? 'bg-blue-700 text-white'
                  : 'hover:bg-blue-800'
              }`}
            >
              <item.icon size={20} />
              {sidebarOpen && <span>{item.label}</span>}
            </Link>
          ))}
        </nav>

        <div className="p-4 border-t border-blue-800">
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 p-3 w-full hover:bg-blue-800 rounded"
          >
            <LogOut size={20} />
            {sidebarOpen && <span>Cerrar Sesión</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        <header className="bg-white shadow-sm p-4">
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-bold text-gray-800">
              {menuItems.find(item => isActive(item.path))?.label || 'Dashboard'}
            </h2>
            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-600">
                Bienvenido, <strong>{user?.nombre || 'Admin'}</strong>
              </span>
            </div>
          </div>
        </header>

        <main className="flex-1 p-6 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;
