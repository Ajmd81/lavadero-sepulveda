// src/pages/admin/AlbaranForm.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { albaranService } from '../../services/albaranService';
import clienteService from '../../services/clienteService';
import { FiSave, FiX, FiPlus, FiTrash2 } from 'react-icons/fi';

const AlbaranForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;

  const [clientes, setClientes] = useState([]);
  const [formData, setFormData] = useState({
    clienteId: '',
    fecha: new Date().toISOString().split('T')[0],
    lineas: [
      {
        concepto: '',
        cantidad: 1,
        precioUnitario: 0,
        tipoIva: 21
      }
    ]
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadClientes();
    if (isEdit) {
      loadAlbaran();
    }
  }, [id]);

  const loadClientes = async () => {
    try {
      const data = await clienteService.getAll();
      setClientes(data);
    } catch (error) {
      console.error('Error cargando clientes:', error);
    }
  };

  const loadAlbaran = async () => {
    try {
      const data = await albaranService.getById(id);
      setFormData({
        clienteId: data.clienteId,
        fecha: data.fecha,
        lineas: data.lineas
      });
    } catch (error) {
      console.error('Error cargando albarán:', error);
      alert('Error al cargar el albarán');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.clienteId) {
      alert('Selecciona un cliente');
      return;
    }

    if (formData.lineas.length === 0) {
      alert('Añade al menos una línea');
      return;
    }

    try {
      setLoading(true);
      if (isEdit) {
        await albaranService.update(id, formData);
      } else {
        await albaranService.create(formData);
      }
      navigate('/admin/facturacion');
    } catch (error) {
      console.error('Error guardando albarán:', error);
      alert('Error al guardar el albarán');
    } finally {
      setLoading(false);
    }
  };

  const handleLineaChange = (index, field, value) => {
    const newLineas = [...formData.lineas];
    newLineas[index][field] = value;
    setFormData({ ...formData, lineas: newLineas });
  };

  const addLinea = () => {
    setFormData({
      ...formData,
      lineas: [
        ...formData.lineas,
        {
          concepto: '',
          cantidad: 1,
          precioUnitario: 0,
          tipoIva: 21
        }
      ]
    });
  };

  const removeLinea = (index) => {
    if (formData.lineas.length === 1) {
      alert('Debe haber al menos una línea');
      return;
    }
    const newLineas = formData.lineas.filter((_, i) => i !== index);
    setFormData({ ...formData, lineas: newLineas });
  };

  const calcularLinea = (linea) => {
    const subtotal = linea.cantidad * linea.precioUnitario;
    const iva = subtotal * (linea.tipoIva / 100);
    const total = subtotal + iva;
    return { subtotal, iva, total };
  };

  const calcularTotales = () => {
    let baseImponible = 0;
    let totalIva = 0;
    
    formData.lineas.forEach(linea => {
      const { subtotal, iva } = calcularLinea(linea);
      baseImponible += subtotal;
      totalIva += iva;
    });

    return {
      baseImponible,
      iva: totalIva,
      total: baseImponible + totalIva
    };
  };

  const totales = calcularTotales();

  return (
    <div className="albaran-form-container">
      <div className="page-header">
        <h1>{isEdit ? 'Editar Albarán' : 'Nuevo Albarán'}</h1>
      </div>

      <form onSubmit={handleSubmit} className="albaran-form">
        <div className="form-section">
          <h3>Datos del Albarán</h3>
          
          <div className="form-row">
            <div className="form-group">
              <label>Cliente *</label>
              <select
                value={formData.clienteId}
                onChange={(e) => setFormData({ ...formData, clienteId: e.target.value })}
                required
              >
                <option value="">Seleccionar cliente...</option>
                {clientes.map(cliente => (
                  <option key={cliente.id} value={cliente.id}>
                    {cliente.nombre}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Fecha *</label>
              <input
                type="date"
                value={formData.fecha}
                onChange={(e) => setFormData({ ...formData, fecha: e.target.value })}
                required
              />
            </div>
          </div>
        </div>

        <div className="form-section">
          <div className="section-header">
            <h3>Líneas del Albarán</h3>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={addLinea}
            >
              <FiPlus /> Añadir línea
            </button>
          </div>

          <div className="lineas-table">
            <table>
              <thead>
                <tr>
                  <th style={{ width: '35%' }}>Concepto</th>
                  <th style={{ width: '10%' }}>Cant.</th>
                  <th style={{ width: '15%' }}>Precio Unit.</th>
                  <th style={{ width: '10%' }}>IVA %</th>
                  <th style={{ width: '15%' }}>Subtotal</th>
                  <th style={{ width: '15%' }}>Total</th>
                  <th style={{ width: '5%' }}></th>
                </tr>
              </thead>
              <tbody>
                {formData.lineas.map((linea, index) => {
                  const { subtotal, total } = calcularLinea(linea);
                  return (
                    <tr key={index}>
                      <td>
                        <input
                          type="text"
                          value={linea.concepto}
                          onChange={(e) => handleLineaChange(index, 'concepto', e.target.value)}
                          placeholder="Descripción del servicio/producto"
                          required
                        />
                      </td>
                      <td>
                        <input
                          type="number"
                          value={linea.cantidad}
                          onChange={(e) => handleLineaChange(index, 'cantidad', parseInt(e.target.value) || 1)}
                          min="1"
                          required
                        />
                      </td>
                      <td>
                        <input
                          type="number"
                          step="0.01"
                          value={linea.precioUnitario}
                          onChange={(e) => handleLineaChange(index, 'precioUnitario', parseFloat(e.target.value) || 0)}
                          min="0"
                          required
                        />
                      </td>
                      <td>
                        <select
                          value={linea.tipoIva}
                          onChange={(e) => handleLineaChange(index, 'tipoIva', parseFloat(e.target.value))}
                        >
                          <option value="0">0%</option>
                          <option value="4">4%</option>
                          <option value="10">10%</option>
                          <option value="21">21%</option>
                        </select>
                      </td>
                      <td className="text-right">
                        {subtotal.toFixed(2)} €
                      </td>
                      <td className="text-right">
                        <strong>{total.toFixed(2)} €</strong>
                      </td>
                      <td>
                        <button
                          type="button"
                          className="btn-icon btn-delete"
                          onClick={() => removeLinea(index)}
                          disabled={formData.lineas.length === 1}
                        >
                          <FiTrash2 />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>

        <div className="form-section totales-section">
          <div className="totales-grid">
            <div className="total-row">
              <span>Base Imponible:</span>
              <strong>{totales.baseImponible.toFixed(2)} €</strong>
            </div>
            <div className="total-row">
              <span>IVA:</span>
              <strong>{totales.iva.toFixed(2)} €</strong>
            </div>
            <div className="total-row total-final">
              <span>TOTAL:</span>
              <strong>{totales.total.toFixed(2)} €</strong>
            </div>
          </div>
        </div>

        <div className="form-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => navigate('/admin/facturacion')}
          >
            <FiX /> Cancelar
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={loading}
          >
            <FiSave /> {loading ? 'Guardando...' : 'Guardar Albarán'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default AlbaranForm;