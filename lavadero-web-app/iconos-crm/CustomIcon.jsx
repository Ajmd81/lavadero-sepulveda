/**
 * CustomIcon - Componente para usar los iconos personalizados del CRM
 * 
 * Uso:
 * <CustomIcon name="dashboard" size={24} className="..." />
 */

const CustomIcon = ({ name, size = 24, className = '' }) => {
  // Mapeo de nombres a rutas de imágenes
  const iconMap = {
    // Dashboard y análisis
    dashboard: '/assets/icons/panel.png',
    analisis: '/assets/icons/analisis.png',
    estadoFinanciero: '/assets/icons/estado-financiero.png',
    
    // Clientes
    cliente: '/assets/icons/cliente.png',
    
    // Citas y agenda
    citas: '/assets/icons/citas.png',
    
    // Facturación
    facturacion: '/assets/icons/facturacion.png',
    facturaEmitida: '/assets/icons/facturaEmitida.png',
    invoice: '/assets/icons/invoice.png',
    
    // Contabilidad
    contabilidad: '/assets/icons/contabilidad.png',
    modeloFiscal: '/assets/icons/modeloFiscal.png',
    
    // Proveedores
    proveedor: '/assets/icons/proveedor.png',
    
    // Otros
    carrito: '/assets/icons/carro-de-la-compra.png',
    
    // Logo
    logo: '/assets/icons/logo_crm.png',
  };

  const iconSrc = iconMap[name];

  if (!iconSrc) {
    console.warn(`Icono "${name}" no encontrado`);
    return null;
  }

  return (
    <img
      src={iconSrc}
      alt={name}
      width={size}
      height={size}
      className={className}
      style={{ objectFit: 'contain' }}
    />
  );
};

export default CustomIcon;

// Exportar también los nombres disponibles para autocompletado
export const availableIcons = [
  'dashboard',
  'analisis',
  'estadoFinanciero',
  'cliente',
  'citas',
  'facturacion',
  'facturaEmitida',
  'invoice',
  'contabilidad',
  'modeloFiscal',
  'proveedor',
  'carrito',
  'logo'
];
