import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import AdminLayout from './components/layout/AdminLayout';

// Pages
import Login from './pages/admin/Login';
import Dashboard from './pages/admin/Dashboard';
import Clientes from './pages/admin/Clientes';
import Citas from './pages/admin/Citas';
import Calendario from './pages/admin/Calendario';
import Facturacion from './pages/admin/Facturacion';
import Proveedores from './pages/admin/Proveedores';
import Gastos from './pages/admin/Gastos';
import Contabilidad from './pages/admin/Contabilidad';
import ResumenFinanciero from './pages/admin/ResumenFinanciero';
import ModelosFiscales from './pages/admin/ModelosFiscales';
import Configuracion from './pages/admin/Configuracion';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/admin/login" element={<Login />} />

            <Route path="/admin" element={
              <ProtectedRoute>
                <AdminLayout />
              </ProtectedRoute>
            }>
              <Route index element={<Dashboard />} />
              <Route path="clientes" element={<Clientes />} />
              <Route path="citas" element={<Citas />} />
              <Route path="calendario" element={<Calendario />} />
              <Route path="facturacion" element={<Facturacion />} />
              <Route path="proveedores" element={<Proveedores />} />
              <Route path="gastos" element={<Gastos />} />
              <Route path="contabilidad" element={<Contabilidad />} />
              <Route path="resumen-financiero" element={<ResumenFinanciero />} />
              <Route path="modelos-fiscales" element={<ModelosFiscales />} />
              <Route path="configuracion" element={<Configuracion />} />
            </Route>

            <Route path="/" element={<Navigate to="/admin" replace />} />
            <Route path="*" element={<Navigate to="/admin" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
