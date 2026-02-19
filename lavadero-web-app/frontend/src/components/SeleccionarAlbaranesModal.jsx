// src/components/SeleccionarAlbaranesModal.jsx
import React, { useState, useEffect } from 'react';
import { albaranService } from '../../services/albaranService';
import { X, Check } from 'lucide-react';
import '../styles/Modal.css';

const SeleccionarAlbaranesModal = ({ isOpen, onClose, clienteId, onSeleccionar }) => {
  const [albaranes, setAlbaranes] = useState([]);
  const [seleccionados, setSeleccionados] = useState([]);

  useEffect(() => {
    const cargarAlbaranes = async () => {
      try {
        const data = await albaranService.getPendientesFacturar(clienteId);
        setAlbaranes(data);
      } catch (error) {
        console.error('Error cargando albaranes:', error);
      }
    };

    if (isOpen && clienteId) {
      cargarAlbaranes();
    }
  }, [isOpen, clienteId]);

  const toggleSeleccion = (albaranId) => {
    if (seleccionados.includes(albaranId)) {
      setSeleccionados(seleccionados.filter(id => id !== albaranId));
    } else {
      setSeleccionados([...seleccionados, albaranId]);
    }
  };

  const handleConfirmar = () => {
    const albaranesSeleccionados = albaranes.filter(a => 
      seleccionados.includes(a.id)
    );
    onSeleccionar(albaranesSeleccionados);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Seleccionar Albaranes</h2>
          <button className="btn-close" onClick={onClose}>
            <X />
          </button>
        </div>

        <div className="modal-body">
          {albaranes.length === 0 ? (
            <p>No hay albaranes pendientes de facturar para este cliente</p>
          ) : (
            <table className="modal-table">
              <thead>
                <tr>
                  <th style={{ width: '50px' }}></th>
                  <th>Número</th>
                  <th>Fecha</th>
                  <th className="text-right">Total</th>
                </tr>
              </thead>
              <tbody>
                {albaranes.map(albaran => (
                  <tr 
                    key={albaran.id}
                    className={seleccionados.includes(albaran.id) ? 'selected' : ''}
                    onClick={() => toggleSeleccion(albaran.id)}
                  >
                    <td>
                      <input
                        type="checkbox"
                        checked={seleccionados.includes(albaran.id)}
                        onChange={() => toggleSeleccion(albaran.id)}
                      />
                    </td>
                    <td>{albaran.numero}</td>
                    <td>{new Date(albaran.fecha).toLocaleDateString('es-ES')}</td>
                    <td className="text-right">{albaran.total?.toFixed(2)} €</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>
            Cancelar
          </button>
          <button 
            className="btn btn-primary"
            onClick={handleConfirmar}
            disabled={seleccionados.length === 0}
          >
            <Check /> Confirmar ({seleccionados.length})
          </button>
        </div>
      </div>
    </div>
  );
};

export default SeleccionarAlbaranesModal;