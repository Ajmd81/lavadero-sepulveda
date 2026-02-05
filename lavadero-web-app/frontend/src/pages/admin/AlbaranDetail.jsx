// src/pages/admin/AlbaranDetail.jsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { albaranService } from '../../services/albaranService';
import { FiArrowLeft, FiEdit2, FiFileText, FiPrinter } from 'react-icons/fi';

const AlbaranDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [albaran, setAlbaran] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAlbaran();
  }, [id]);

  const loadAlbaran = async () => {
    try {
      const data = await albaranService.getById(id);
      setAlbaran(data);
    } catch (error) {
      console.error('Error cargando albarán:', error);
      alert('Error al cargar el albarán');
    } finally {
      setLoading(false);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  if (loading) {
    return <div className="loading">Cargando...</div>;
  }

  if (!albaran) {
    return <div>Albarán no encontrado</div>;
  }

  return (
    <div className="albaran-detail-container">
      <div className="detail-actions no-print">
        <button className="btn btn-secondary" onClick={() => navigate('/admin/facturacion')}>
          <FiArrowLeft /> Volver
        </button>
        <div>
          {albaran.estado !== 'FACTURADO' && (
            <button 
              className="btn btn-primary"
              onClick={() => navigate(`/admin/facturacion/albaranes/${id}/editar`)}
            >
              <FiEdit2 /> Editar
            </button>
          )}
          <button className="btn btn-secondary" onClick={handlePrint}>
            <FiPrinter /> Imprimir
          </button>
        </div>
      </div>

      <div className="albaran-document">
        <div className="document-header">
          <div className="company-info">
            <h1>LAVADERO SEPÚLVEDA</h1>
            <p>C/ Ejemplo, 123</p>
            <p>14001 Córdoba</p>
            <p>CIF: B12345678</p>
          </div>
          <div className="document-title">
            <h2>ALBARÁN</h2>
            <p className="document-number">{albaran.numero}</p>
            <p className="document-date">
              Fecha: {new Date(albaran.fecha).toLocaleDateString('es-ES')}
            </p>
          </div>
        </div>

        <div className="client-info">
          <h3>Cliente</h3>
          <p><strong>{albaran.clienteNombre}</strong></p>
        </div>

        <table className="detail-table">
          <thead>
            <tr>
              <th>Concepto</th>
              <th className="text-center">Cantidad</th>
              <th className="text-right">Precio Unit.</th>
              <th className="text-center">IVA %</th>
              <th className="text-right">Subtotal</th>
              <th className="text-right">Total</th>
            </tr>
          </thead>
          <tbody>
            {albaran.lineas.map((linea, index) => (
              <tr key={index}>
                <td>{linea.concepto}</td>
                <td className="text-center">{linea.cantidad}</td>
                <td className="text-right">{linea.precioUnitario?.toFixed(2)} €</td>
                <td className="text-center">{linea.tipoIva}%</td>
                <td className="text-right">{linea.subtotal?.toFixed(2)} €</td>
                <td className="text-right"><strong>{linea.total?.toFixed(2)} €</strong></td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="document-totals">
          <div className="total-row">
            <span>Base Imponible:</span>
            <span>{albaran.baseImponible?.toFixed(2)} €</span>
          </div>
          <div className="total-row">
            <span>IVA:</span>
            <span>{albaran.iva?.toFixed(2)} €</span>
          </div>
          <div className="total-row total-final">
            <span>TOTAL:</span>
            <span><strong>{albaran.total?.toFixed(2)} €</strong></span>
          </div>
        </div>

        <div className="document-footer">
          <p>Estado: <strong>{albaran.estado}</strong></p>
          {albaran.facturaId && (
            <p>Facturado en: Factura #{albaran.facturaId}</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default AlbaranDetail;