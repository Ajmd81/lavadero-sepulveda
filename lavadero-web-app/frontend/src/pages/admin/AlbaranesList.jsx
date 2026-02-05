// src/pages/admin/AlbaranesList.jsx
import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { albaranService } from '../../services/albaranService';
import { 
  FiPlus, FiEdit2, FiTrash2, FiEye, FiFileText, 
  FiFilter, FiSearch, FiCheck, FiClock 
} from 'react-icons/fi';
import '../../AlbaranesList.css';

const AlbaranesList = () => {
  const navigate = useNavigate();
  const [albaranes, setAlbaranes] = useState([]);
  const [filteredAlbaranes, setFilteredAlbaranes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [estadoFilter, setEstadoFilter] = useState('TODOS');

  useEffect(() => {
    loadAlbaranes();
  }, []);

  useEffect(() => {
    filterAlbaranes();
  }, [searchTerm, estadoFilter, albaranes]);

  const loadAlbaranes = async () => {
    try {
      setLoading(true);
      const data = await albaranService.getAll();
      setAlbaranes(data);
    } catch (error) {
      console.error('Error cargando albaranes:', error);
      alert('Error al cargar los albaranes');
    } finally {
      setLoading(false);
    }
  };

  const filterAlbaranes = () => {
    let filtered = [...albaranes];

    // Filtrar por búsqueda
    if (searchTerm) {
      filtered = filtered.filter(alb =>
        alb.numero.toLowerCase().includes(searchTerm.toLowerCase()) ||
        alb.clienteNombre.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Filtrar por estado
    if (estadoFilter !== 'TODOS') {
      filtered = filtered.filter(alb => alb.estado === estadoFilter);
    }

    setFilteredAlbaranes(filtered);
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Estás seguro de eliminar este albarán?')) {
      try {
        await albaranService.delete(id);
        loadAlbaranes();
      } catch (error) {
        alert('Error al eliminar el albarán: ' + error.message);
      }
    }
  };

  const handleCambiarEstado = async (id, nuevoEstado) => {
    try {
      await albaranService.cambiarEstado(id, nuevoEstado);
      loadAlbaranes();
    } catch (error) {
      alert('Error al cambiar estado: ' + error.message);
    }
  };

  const getEstadoBadge = (estado) => {
    const badges = {
      PENDIENTE: { color: 'warning', icon: FiClock, text: 'Pendiente' },
      ENTREGADO: { color: 'success', icon: FiCheck, text: 'Entregado' },
      FACTURADO: { color: 'info', icon: FiFileText, text: 'Facturado' }
    };
    
    const badge = badges[estado] || badges.PENDIENTE;
    const Icon = badge.icon;
    
    return (
      <span className={`badge badge-${badge.color}`}>
        <Icon size={14} /> {badge.text}
      </span>
    );
  };

  if (loading) {
    return <div className="loading">Cargando albaranes...</div>;
  }

  return (
    <div className="albaranes-list-container">
      <div className="page-header">
        <div>
          <h1>Albaranes</h1>
          <p className="subtitle">Gestión de albaranes de entrega</p>
        </div>
        <button 
          className="btn btn-primary"
          onClick={() => navigate('/facturacion/albaranes/nuevo')}
        >
          <FiPlus /> Nuevo Albarán
        </button>
      </div>

      <div className="filters-section">
        <div className="search-box">
          <FiSearch />
          <input
            type="text"
            placeholder="Buscar por número o cliente..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="filter-group">
          <FiFilter />
          <select 
            value={estadoFilter}
            onChange={(e) => setEstadoFilter(e.target.value)}
          >
            <option value="TODOS">Todos los estados</option>
            <option value="PENDIENTE">Pendiente</option>
            <option value="ENTREGADO">Entregado</option>
            <option value="FACTURADO">Facturado</option>
          </select>
        </div>
      </div>

      <div className="stats-cards">
        <div className="stat-card warning">
          <div className="stat-value">
            {albaranes.filter(a => a.estado === 'PENDIENTE').length}
          </div>
          <div className="stat-label">Pendientes</div>
        </div>
        <div className="stat-card success">
          <div className="stat-value">
            {albaranes.filter(a => a.estado === 'ENTREGADO').length}
          </div>
          <div className="stat-label">Entregados</div>
        </div>
        <div className="stat-card info">
          <div className="stat-value">
            {albaranes.filter(a => a.estado === 'FACTURADO').length}
          </div>
          <div className="stat-label">Facturados</div>
        </div>
      </div>

      <div className="table-container">
        <table className="albaranes-table">
          <thead>
            <tr>
              <th>Número</th>
              <th>Cliente</th>
              <th>Fecha</th>
              <th>Estado</th>
              <th className="text-right">Total</th>
              <th className="actions-column">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {filteredAlbaranes.length === 0 ? (
              <tr>
                <td colSpan="6" className="no-data">
                  No se encontraron albaranes
                </td>
              </tr>
            ) : (
              filteredAlbaranes.map(albaran => (
                <tr key={albaran.id}>
                  <td>
                    <strong>{albaran.numero}</strong>
                  </td>
                  <td>{albaran.clienteNombre}</td>
                  <td>
                    {new Date(albaran.fecha).toLocaleDateString('es-ES')}
                  </td>
                  <td>
                    {getEstadoBadge(albaran.estado)}
                  </td>
                  <td className="text-right">
                    <strong>{albaran.total?.toFixed(2)} €</strong>
                  </td>
                  <td className="actions-cell">
                    <button
                      className="btn-icon btn-view"
                      onClick={() => navigate(`/facturacion/albaranes/${albaran.id}`)}
                      title="Ver detalle"
                    >
                      <FiEye />
                    </button>
                    
                    {albaran.estado !== 'FACTURADO' && (
                      <>
                        <button
                          className="btn-icon btn-edit"
                          onClick={() => navigate(`/facturacion/albaranes/${albaran.id}/editar`)}
                          title="Editar"
                        >
                          <FiEdit2 />
                        </button>
                        
                        <button
                          className="btn-icon btn-delete"
                          onClick={() => handleDelete(albaran.id)}
                          title="Eliminar"
                        >
                          <FiTrash2 />
                        </button>
                      </>
                    )}

                    {albaran.estado === 'PENDIENTE' && (
                      <button
                        className="btn-sm btn-success"
                        onClick={() => handleCambiarEstado(albaran.id, 'ENTREGADO')}
                      >
                        Marcar entregado
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AlbaranesList;