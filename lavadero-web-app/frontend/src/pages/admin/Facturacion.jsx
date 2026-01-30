import { useState } from 'react';
import FacturasEmitidas from './FacturasEmitidas';
import FacturasRecibidas from './FacturasRecibidas';
import Gastos from './Gastos';

const Facturacion = () => {
  const [tabActiva, setTabActiva] = useState('emitidas');

  const pestanas = [
    { id: 'emitidas', label: 'Facturas Emitidas', icon: '/assets/icons/facturacion.png' },
    { id: 'recibidas', label: 'Facturas Recibidas', icon: '/assets/icons/facturaEmitida.png' },
    { id: 'gastos', label: 'Gastos', icon: '/assets/icons/invoice.png' },
  ];

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center gap-3">
          <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <img src="/assets/icons/facturacion.png" alt="Facturación" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
          </div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Factura</h1>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow">
        <div className="flex border-b">
          {pestanas.map(pestana => (
            <button
              key={pestana.id}
              onClick={() => setTabActiva(pestana.id)}
              className={`flex-1 px-6 py-4 font-semibold text-center transition-colors flex items-center justify-center gap-2 ${tabActiva === pestana.id
                ? 'border-b-2 border-blue-600 text-blue-600 bg-blue-50'
                : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                }`}
            >
              <img src={pestana.icon} alt={pestana.label} className="w-6 h-6 object-contain" />
              {pestana.label}
            </button>
          ))}
        </div>
      </div>

      <div>
        {tabActiva === 'emitidas' && <FacturasEmitidas />}
        {tabActiva === 'recibidas' && <FacturasRecibidas />}
        {tabActiva === 'gastos' && <Gastos />}
      </div>
    </div>
  );
};

export default Facturacion;
