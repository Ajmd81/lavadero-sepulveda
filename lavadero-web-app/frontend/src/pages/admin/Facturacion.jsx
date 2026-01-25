import { useState } from 'react';
import FacturasEmitidas from './FacturasEmitidas';
import FacturasRecibidas from './FacturasRecibidas';
import Gastos from './Gastos';

const Facturacion = () => {
  const [tabActiva, setTabActiva] = useState('emitidas');

  const pestanas = [
    { id: 'emitidas', label: 'Facturas Emitidas', icon: '📤' },
    { id: 'recibidas', label: 'Facturas Recibidas', icon: '📥' },
    { id: 'gastos', label: 'Gastos', icon: '💰' },
  ];

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow">
        <div className="flex border-b">
          {pestanas.map(pestana => (
            <button
              key={pestana.id}
              onClick={() => setTabActiva(pestana.id)}
              className={`flex-1 px-6 py-4 font-semibold text-center transition-colors ${
                tabActiva === pestana.id
                  ? 'border-b-2 border-blue-600 text-blue-600 bg-blue-50'
                  : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
              }`}
            >
              <span className="mr-2">{pestana.icon}</span>
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
