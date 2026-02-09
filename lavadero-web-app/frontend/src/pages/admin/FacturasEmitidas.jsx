import { useState, useEffect } from 'react';
import facturaService from '../../services/facturaService';
import clienteService from '../../services/clienteService';
import enumsService from '../../services/enumsService';
import jsPDF from 'jspdf';
import 'jspdf-autotable';
import axios from 'axios';


const FacturasEmitidas = () => {
  const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'
  const [facturas, setFacturas] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [metodosPago, setMetodosPago] = useState([]);
  const [tiposLavado, setTiposLavado] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [modalCobrarAbierto, setModalCobrarAbierto] = useState(false);
  const [facturaParaCobrar, setFacturaParaCobrar] = useState(null);
  const [metodoPagoCobro, setMetodoPagoCobro] = useState('EFECTIVO');
  const [editandoFactura, setEditandoFactura] = useState(null);
  const [paginaActual, setPaginaActual] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [totalFacturas, setTotalFacturas] = useState(0);
  const [formData, setFormData] = useState({
    numero: '',
    fecha: '',
    tipo: 'SIMPLIFICADA',
    estado: 'PENDIENTE',
    metodoPago: 'EFECTIVO',
    clienteId: '',
    clienteNombre: '',
    clienteNif: '',
    clienteDireccion: '',
    clienteEmail: '',
    clienteTelefono: '',
    lineas: [],
    baseImponible: '',
    tipoIva: '21',
    importeIva: '',
    total: '',
    observaciones: '',
  });

  const [nuevaLinea, setNuevaLinea] = useState({
    concepto: '',
    cantidad: 1,
    precioUnitario: '',
  });

  useEffect(() => {
    // Inicializar datos en componente
    const inicializar = async () => {
      try {
        await cargarMetodosPago();
        await cargarTiposLavado();
        await cargarClientes();
        await cargarFacturas();
      } catch (err) {
        console.error('❌ Error en inicialización:', err);
        setFacturas([]);
        setClientes([]);
      }
    };
    
    inicializar();
  }, []);

  // Cargar todas las facturas
  const cargarFacturas = async (page = 0) => {
    setLoading(true);
    try {
      const response = await facturaService.getAll(page, 20, 'numero');
      const data = response.data;

      if (data && data.content && Array.isArray(data.content)) {
        setFacturas(data.content);
        setTotalPaginas(data.totalPages);
        setTotalFacturas(data.totalElements);
        setPaginaActual(data.currentPage);
        setError(null);
      } else {
        console.warn('⚠️ Formato de respuesta inesperado:', data);
        setFacturas([]);
        setTotalPaginas(0);
        setTotalFacturas(0);
      }
    } catch (err) {
      setError('Error al cargar las facturas: ' + err.message);
      console.error('❌ Error cargar facturas:', err);
      setFacturas([]);
      setTotalPaginas(0);
      setTotalFacturas(0);
    } finally {
      setLoading(false);
    }
  };

  const irAlaPagina = (page) => {
    if (page >= 0 && page < totalPaginas) {
      cargarFacturas(page);
    }
  };

  // Cargar clientes
  const cargarMetodosPago = async () => {
    try {
      const response = await enumsService.obtenerMetodosPago();
      if (response.data && Array.isArray(response.data)) {
        setMetodosPago(response.data);
      }
    } catch (err) {
      console.error('Error al cargar métodos de pago:', err);
      // Fallback
      setMetodosPago([
        { valor: 'EFECTIVO', descripcion: 'Efectivo' },
        { valor: 'TARJETA', descripcion: 'Tarjeta' },
        { valor: 'TRANSFERENCIA', descripcion: 'Transferencia' },
        { valor: 'BIZUM', descripcion: 'Bizum' },
        { valor: 'CHEQUE', descripcion: 'Cheque' },
        { valor: 'DOMICILIACION', descripcion: 'Domiciliación' },
      ]);
    }
  };

  const cargarTiposLavado = async () => {
    try {
      const response = await enumsService.obtenerTiposLavado();
      if (response.data && Array.isArray(response.data)) {
        setTiposLavado(response.data);
      }
    } catch (err) {
      console.error('Error al cargar tipos de lavado:', err);
      setTiposLavado([]);
    }
  };

  const cargarClientes = async () => {
    try {
      // Usar getAllPaginated con tamaño grande para obtener todos los clientes en una sola llamada
      const response = await clienteService.getAllPaginated(0, 1000, 'nombre', 'asc');
      let clientesData = response.data || [];
      
      // Si es un objeto con content (paginado), extraer content
      if (clientesData && clientesData.content && Array.isArray(clientesData.content)) {
        clientesData = clientesData.content;
      }
      
      // Validar que sea un array
      if (!Array.isArray(clientesData)) {
        console.warn('⚠️ response.data no es un array:', typeof clientesData);
        clientesData = [];
      }
      
      console.log('✅ Clientes cargados:', clientesData.length);
      setClientes(clientesData);
    } catch (err) {
      console.error('❌ Error al cargar clientes:', err);
      setClientes([]);
    }
  };

  // Abrir modal para crear nueva factura
  const abrirModalNuevo = () => {
    setFormData({
      numero: '',
      fecha: new Date().toISOString().split('T')[0],
      tipo: 'SIMPLIFICADA',
      estado: 'PENDIENTE',
      metodoPago: 'EFECTIVO',
      clienteId: '',
      clienteNombre: '',
      clienteNif: '',
      clienteDireccion: '',
      clienteEmail: '',
      clienteTelefono: '',
      lineas: [],
      baseImponible: '',
      tipoIva: '21',
      importeIva: '',
      total: '',
      observaciones: '',
    });
    setNuevaLinea({
      concepto: '',
      cantidad: 1,
      precioUnitario: '',
    });
    setEditandoFactura(null);
    setModalAbierto(true);
  };

  // Abrir modal para editar factura
  const abrirModalEditar = (factura) => {
    setFormData({
      ...factura,
      lineas: factura.lineas || [],
    });
    setEditandoFactura(factura.id);
    setModalAbierto(true);
  };

  // Cerrar modal
  const cerrarModal = () => {
    setModalAbierto(false);
    setEditandoFactura(null);
  };

  // Manejar cambios en los inputs
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));

    if (name === 'tipoIva' && formData.lineas.length > 0) {
      recalcularTotales(formData.lineas, value);
    }
  };

  // Manejar selección de cliente
  const handleClienteChange = (e) => {
    const clienteId = e.target.value;

    if (!clienteId) {
      setFormData(prev => ({
        ...prev,
        clienteId: '',
        clienteNombre: '',
        clienteNif: '',
        clienteDireccion: '',
        clienteEmail: '',
        clienteTelefono: '',
      }));
      return;
    }

    const cliente = clientes.find(c => c.id === parseInt(clienteId));
    if (cliente) {
      setFormData(prev => ({
        ...prev,
        clienteId: cliente.id,
        clienteNombre: `${cliente.nombre} ${cliente.apellidos || ''}`.trim(),
        clienteNif: cliente.nif || '',
        clienteDireccion: cliente.direccion || '',
        clienteEmail: cliente.email || '',
        clienteTelefono: cliente.telefono || '',
      }));
    }
  };

  // Manejar cambios en la nueva línea
  const handleNuevaLineaChange = (e) => {
    const { name, value } = e.target;
    setNuevaLinea(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  // Agregar línea
  const agregarLinea = () => {
    if (!nuevaLinea.concepto || !nuevaLinea.cantidad || !nuevaLinea.precioUnitario) {
      alert('Por favor completa todos los campos de la línea');
      return;
    }

    const cantidad = parseFloat(nuevaLinea.cantidad);
    const precioUnitario = parseFloat(nuevaLinea.precioUnitario);
    const subtotal = cantidad * precioUnitario;

    const linea = {
      id: Date.now(),
      concepto: nuevaLinea.concepto,
      cantidad: cantidad,
      precioUnitario: precioUnitario,
      subtotal: subtotal,
    };

    const nuevasLineas = [...formData.lineas, linea];
    setFormData(prev => ({
      ...prev,
      lineas: nuevasLineas,
    }));

    recalcularTotales(nuevasLineas, formData.tipoIva);

    setNuevaLinea({
      concepto: '',
      cantidad: 1,
      precioUnitario: '',
    });
  };

  // Eliminar línea
  const eliminarLinea = (lineaId) => {
    const nuevasLineas = formData.lineas.filter(l => l.id !== lineaId);
    setFormData(prev => ({
      ...prev,
      lineas: nuevasLineas,
    }));
    recalcularTotales(nuevasLineas, formData.tipoIva);
  };

  // Recalcular totales
  const recalcularTotales = (lineas, tipoIva) => {
    const baseImponible = lineas.reduce((sum, linea) => sum + linea.subtotal, 0);
    const iva = parseFloat(tipoIva) ?? 21;
    const importeIva = (baseImponible * iva) / 100;
    const total = baseImponible + importeIva;

    setFormData(prev => ({
      ...prev,
      baseImponible: baseImponible.toFixed(2),
      importeIva: importeIva.toFixed(2),
      total: total.toFixed(2),
    }));
  };

  // Guardar factura
  const guardarFactura = async (e) => {
    e.preventDefault();

    if (!formData.numero || !formData.clienteNombre || !formData.total) {
      alert('Por favor completa los campos obligatorios');
      return;
    }

    if (formData.lineas.length === 0) {
      alert('Debes agregar al menos una línea a la factura');
      return;
    }

    try {
      setLoading(true);
      
      // Preparar datos limpios para enviar al backend
      const lineasLimpias = formData.lineas.map(linea => ({
        concepto: linea.concepto,
        cantidad: parseInt(linea.cantidad) || 1,
        precioUnitario: parseFloat(linea.precioUnitario) || 0,
        subtotal: parseFloat(linea.subtotal) || 0
        // NO incluir id de frontend (Date.now()) - el backend generará su propio ID
      }));

      const datosLimpios = {
        tipo: formData.tipo || 'SIMPLIFICADA',
        estado: formData.estado || 'PENDIENTE',
        metodoPago: formData.metodoPago || 'EFECTIVO',
        clienteNombre: formData.clienteNombre,
        clienteNif: formData.clienteNif || null,
        clienteDireccion: formData.clienteDireccion || null,
        clienteEmail: formData.clienteEmail || null,
        clienteTelefono: formData.clienteTelefono || null,
        lineas: lineasLimpias,
        baseImponible: parseFloat(formData.baseImponible) || 0,
        tipoIva: parseFloat(formData.tipoIva) || 21,
        importeIva: parseFloat(formData.importeIva) || 0,
        total: parseFloat(formData.total) || 0,
        observaciones: formData.observaciones || null
      };
      
      if (editandoFactura) {
        // Actualizar factura existente
        const params = {};
        
        // Convertir fecha de yyyy-MM-dd a dd/MM/yyyy si es necesario
        if (formData.fecha && formData.fecha.includes('-')) {
          const [year, month, day] = formData.fecha.split('-');
          params.fechaEmision = `${day}/${month}/${year}`;
          console.log(`📅 Fecha convertida: ${formData.fecha} → ${params.fechaEmision}`);
        }

        console.log('📤 Actualizando factura:', { id: editandoFactura, data: datosLimpios });
        await facturaService.update(editandoFactura, datosLimpios);
        alert('✅ Factura actualizada correctamente');
        
        cerrarModal();
        // Mantener en la misma página al actualizar
        await cargarFacturas(paginaActual);
      } else {
        // Crear nueva factura con número y fecha personalizados
        const params = {};
        
        if (formData.numero) {
          params.numeroFactura = formData.numero;
        }
        
        // Convertir fecha de yyyy-MM-dd a dd/MM/yyyy
        if (formData.fecha) {
          const [year, month, day] = formData.fecha.split('-');
          params.fechaEmision = `${day}/${month}/${year}`;
          console.log(`📅 Fecha convertida: ${formData.fecha} → ${params.fechaEmision}`);
        }

        console.log('📤 Enviando factura:', { data: datosLimpios, params });
        console.log('📤 JSON a enviar:', JSON.stringify(datosLimpios, null, 2));
        await facturaService.createManual(datosLimpios, params);
        alert('✅ Factura creada correctamente');
        
        cerrarModal();
        // Cargar la última página para ver la nueva factura
        // Primero cargar página 0 para actualizar totalPaginas
        const response = await facturaService.getAll(0, 20, 'numero');
        const nuevoTotal = response.data?.totalPages || 1;
        await cargarFacturas(Math.max(0, nuevoTotal - 1));
      }
    } catch (err) {
      console.error('❌ Error al guardar factura:', err);
      console.error('❌ Response completo:', err.response);
      console.error('❌ Response data:', err.response?.data);
      console.error('❌ Response status:', err.response?.status);
      
      // Extraer mensaje de error detallado
      let errorMessage = 'Error desconocido';
      
      if (err.response?.data) {
        const responseData = err.response.data;
        
        // Si es un objeto con detalles
        if (typeof responseData === 'object') {
          errorMessage = responseData.message 
            || responseData.error 
            || responseData.details
            || JSON.stringify(responseData);
            
          // Si hay errores de validación, mostrarlos
          if (responseData.errors) {
            errorMessage += '\n\nErrores de validación:\n' + 
              Object.entries(responseData.errors)
                .map(([field, msg]) => `- ${field}: ${msg}`)
                .join('\n');
          }
        } else {
          errorMessage = String(responseData);
        }
      } else {
        errorMessage = err.message;
      }
      
      alert('❌ Error al guardar factura:\n\n' + errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // Eliminar factura
  const eliminarFactura = async (id) => {
    if (!window.confirm('¿Estás seguro de que deseas eliminar esta factura?')) {
      return;
    }

    try {
      await facturaService.delete(id);
      alert('Factura eliminada correctamente');
      cargarFacturas();
    } catch (err) {
      alert('Error al eliminar factura: ' + err.message);
      console.error(err);
    }
  };

  // Abrir modal para cobrar factura
  const abrirModalCobrar = (factura) => {
    setFacturaParaCobrar(factura);
    setMetodoPagoCobro('EFECTIVO');
    setModalCobrarAbierto(true);
  };

  // Cerrar modal de cobro
  const cerrarModalCobrar = () => {
    setModalCobrarAbierto(false);
    setFacturaParaCobrar(null);
    setMetodoPagoCobro('EFECTIVO');
  };

  // Procesar pago de factura
  const procesarPago = async () => {
    if (!facturaParaCobrar) {
      alert('Error: no hay factura seleccionada');
      return;
    }

    try {
      setLoading(true);
      await facturaService.marcarComoPagada(facturaParaCobrar.id, {
        metodoPago: metodoPagoCobro
      });
      
      alert(`✅ Factura ${facturaParaCobrar.numero} marcada como pagada (${metodoPagoCobro})`);
      cerrarModalCobrar();
      await cargarFacturas();
    } catch (err) {
      console.error('Error al procesar pago:', err);
      alert('❌ Error al marcar como pagada: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  // ✅ FUNCIÓN GENERAR PDF FORMATO JAVAFX
  const generarPdf = async (facturaId) => {
    try {
      // 1. Obtener configuración del sistema
      const configResponse = await axios.get(`${API_URL}/config/plantilla-factura`);
      const plantilla = configResponse.data;
      
      // Mapear datos del backend al formato esperado
      const config = {
        empresa: {
          nombre: plantilla.emisorNombre || 'Antonio Jesús Martínez Díaz',
          cif: plantilla.emisorNif || '44372738L',
          direccion: plantilla.emisorDireccion || '',
          codigoPostal: plantilla.emisorCodigoPostal || '',
          ciudad: plantilla.emisorCiudad || '',
          provincia: plantilla.emisorProvincia || '',
          telefono: plantilla.emisorTelefono || '',
          email: plantilla.emisorEmail || '',
          logoBase64: plantilla.logoBase64 || ''
        },
        factura: {
          colorPrimario: plantilla.colorPrimario || '#2196F3',
          mostrarLogo: plantilla.mostrarLogo !== false,
          textoPie: plantilla.textoGracias || 'Gracias por confiar en nosotros',
          iban: plantilla.cuentaBancaria || ''
        }
      };
      
      // 2. Obtener factura completa
      const facturaResponse = await facturaService.getById(facturaId);
      const factura = facturaResponse.data;

      // 3. Crear documento PDF
      const doc = new jsPDF();
      const pageWidth = doc.internal.pageSize.width;
      const pageHeight = doc.internal.pageSize.height;
      const margin = 20;
      let yPos = 20;

      // Convertir hex a RGB
      const hexToRgb = (hex) => {
        const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
        return result ? {
          r: parseInt(result[1], 16),
          g: parseInt(result[2], 16),
          b: parseInt(result[3], 16)
        } : { r: 33, g: 150, b: 243 }; // Default blue
      };

      const primaryRgb = hexToRgb(config.factura.colorPrimario);

      // ========== LOGO ARRIBA DERECHA (pequeño) ==========
      if (config.factura.mostrarLogo && config.empresa.logoBase64) {
        try {
          const logoWidth = 35;
          const logoHeight = 35;
          doc.addImage(config.empresa.logoBase64, 'PNG', margin, yPos -10, logoWidth, logoHeight);
        } catch (err) {
          console.warn('Error añadiendo logo:', err);
        }
      }

      // ========== TÍTULO "FACTURA" GRANDE DERECHA ==========
      doc.setFontSize(24);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(primaryRgb.r, primaryRgb.g, primaryRgb.b);
      doc.text('FACTURA', pageWidth - margin, yPos + 5, { align: 'right' });
      
      yPos += 12;
      
      // Número y fecha a la derecha
      doc.setFontSize(11);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(60, 60, 60);
      doc.text(`Nº: ${factura.numero}`, pageWidth - margin, yPos, { align: 'right' });
      yPos += 6;
      doc.text(`Fecha: ${formatearFecha(factura.fecha)}`, pageWidth - margin, yPos, { align: 'right' });
      
      // ========== LÍNEA SEPARADORA ==========
      yPos += 8;
      doc.setDrawColor(primaryRgb.r, primaryRgb.g, primaryRgb.b);
      doc.setLineWidth(0.8);
      doc.line(margin, yPos, pageWidth - margin, yPos);
      yPos += 10;

      // ========== DATOS EMPRESA IZQUIERDA (compacto) ==========
      doc.setFontSize(11);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(60, 60, 60);
      doc.text(config.empresa.nombre, margin, yPos);
      
      yPos += 6;
      doc.setFontSize(9);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(80, 80, 80);
      
      if (config.empresa.cif) {
        doc.text(`NIF: ${config.empresa.cif}`, margin, yPos);
        yPos += 4.5;
      }
      
      if (config.empresa.direccion) {
        const direccionCompleta = [
          config.empresa.direccion,
          config.empresa.codigoPostal && config.empresa.ciudad 
            ? `${config.empresa.codigoPostal} ${config.empresa.ciudad}` 
            : config.empresa.ciudad || config.empresa.codigoPostal
        ].filter(Boolean).join(', ');
        
        doc.text(direccionCompleta, margin, yPos);
        yPos += 4.5;
      }
      
      if (config.empresa.telefono) {
        doc.text(`Tel: ${config.empresa.telefono}`, margin, yPos);
        yPos += 4.5;
      }
      
      if (config.empresa.email) {
        doc.text(config.empresa.email, margin, yPos);
        yPos += 4.5;
      }

      yPos += 6;

      // ========== BLOQUE CLIENTE CON FONDO AZUL ==========
      const clienteHeaderHeight = 6;
      doc.setFillColor(primaryRgb.r, primaryRgb.g, primaryRgb.b);
      doc.rect(margin, yPos, pageWidth - 2 * margin, clienteHeaderHeight, 'F');
      
      // Texto "CLIENTE" en blanco
      doc.setFontSize(10);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(255, 255, 255);
      doc.text('CLIENTE', margin + 3, yPos + 4);
      
      yPos += 9;
      
      // Datos del cliente
      doc.setFontSize(10);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(60, 60, 60);
      doc.text(factura.clienteNombre || 'Sin nombre', margin + 3, yPos);
      
      yPos += 5;
      doc.setFontSize(9);
      doc.setFont('helvetica', 'normal');
      
      if (factura.clienteNif) {
        doc.text(factura.clienteNif, margin + 3, yPos);
        yPos += 4.5;
      }
      
      if (factura.clienteDireccion) {
        const direccionCliente = doc.splitTextToSize(factura.clienteDireccion, pageWidth - 2 * margin - 6);
        doc.text(direccionCliente, margin + 3, yPos);
        yPos += (direccionCliente.length * 4.5);
      }

      yPos += 8;

      // ========== TABLA DE LÍNEAS ==========
      const lineasTabla = factura.lineas && factura.lineas.length > 0
        ? factura.lineas.map(linea => [
            linea.concepto || 'Sin concepto',
            linea.cantidad?.toString() || '1',
            `${(linea.precioUnitario || 0).toFixed(2)} €`,
            `${(linea.subtotal || 0).toFixed(2)} €`
          ])
        : [['Sin líneas registradas', '-', '-', '-']];

      doc.autoTable({
        startY: yPos,
        head: [['Concepto', 'Cant.', 'Precio', 'Importe']],
        body: lineasTabla,
        theme: 'grid',
        headStyles: {
          fillColor: [primaryRgb.r, primaryRgb.g, primaryRgb.b],
          textColor: 255,
          fontStyle: 'bold',
          halign: 'center',
          fontSize: 10
        },
        styles: {
          fontSize: 9,
          cellPadding: 4,
          textColor: [60, 60, 60]
        },
        columnStyles: {
          0: { cellWidth: 'auto', halign: 'left' },
          1: { halign: 'center', cellWidth: 20 },
          2: { halign: 'right', cellWidth: 30 },
          3: { halign: 'right', cellWidth: 30 }
        },
        margin: { left: margin, right: margin }
      });

      yPos = doc.lastAutoTable.finalY + 10;

      // ========== TOTALES ALINEADOS DERECHA ==========
      const totalesX = pageWidth - margin - 60;
      
      doc.setFont('helvetica', 'normal');
      doc.setFontSize(10);
      doc.setTextColor(60, 60, 60);

      // Base Imponible
      doc.text('Base Imponible:', totalesX, yPos);
      doc.text(`${parseFloat(factura.baseImponible || 0).toFixed(2)} €`, totalesX + 55, yPos, { align: 'right' });
      yPos += 6;

      // IVA
      doc.text(`IVA (${factura.tipoIva || 21}%):`, totalesX, yPos);
      doc.text(`${parseFloat(factura.importeIva || 0).toFixed(2)} €`, totalesX + 55, yPos, { align: 'right' });
      
      yPos += 2;
      doc.setLineWidth(0.3);
      doc.setDrawColor(primaryRgb.r, primaryRgb.g, primaryRgb.b);
      doc.line(totalesX, yPos, totalesX + 55, yPos);
      yPos += 5;

      // TOTAL
      doc.setFont('helvetica', 'bold');
      doc.setFontSize(12);
      doc.setTextColor(primaryRgb.r, primaryRgb.g, primaryRgb.b);
      doc.text('TOTAL:', totalesX, yPos);
      doc.text(`${parseFloat(factura.total || 0).toFixed(2)} €`, totalesX + 55, yPos, { align: 'right' });

      yPos += 15;

      // ========== TEXTO LEGAL RGPD (compacto) ==========
      const textoLegal = "De conformidad con el artículo 13 de la sección 2 del Reglamento (UE) 2016/679 del Parlamento Europeo y del Consejo, de 27 de abril de 2016, relativo a la protección de las personas físicas en lo que respecta al tratamiento de datos personales y a la libre circulación de estos datos, le informamos que el responsable del tratamiento es " + config.empresa.nombre + ", que dicho tratamiento se lleva a cabo para la gestión contable, fiscal y administrativa y para el envío de comunicaciones comerciales sobre productos y servicios de la empresa que puedan ser de su interés. La base legal que permite legitimar este tratamiento es la ejecución del contrato de venta. Se comunicarán datos a terceros para poder llevar a cabo las finalidades objeto de este contrato. Puede usted acceder, rectificar y suprimir sus datos, así como otros derechos, dirigiéndose por escrito a " + config.empresa.email + ". Puede usted obtener información ampliada sobre protección de datos solicitándola a " + config.empresa.email + ".";
      
      // Verificar espacio
      if (yPos > pageHeight - 80) {
        doc.addPage();
        yPos = 20;
      }

      doc.setFontSize(7);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(100, 100, 100);
      const lineasLegal = doc.splitTextToSize(textoLegal, pageWidth - 2 * margin);
      doc.text(lineasLegal, margin, yPos);
      yPos += (lineasLegal.length * 2.5) + 5;

      // ========== FORMA DE PAGO ==========
      if (factura.metodoPago) {
        doc.setFontSize(9);
        doc.setFont('helvetica', 'normal');
        doc.setTextColor(60, 60, 60);
        
        // Mapear método de pago a texto legible
        const metodoPagoTexto = {
          'EFECTIVO': 'Efectivo',
          'TARJETA': 'Tarjeta',
          'TRANSFERENCIA': 'Transferencia',
          'BIZUM': 'Bizum',
          'CHEQUE': 'Cheque',
          'DOMICILIACION': 'Domiciliación'
        };
        
        const textoPago = `Forma de pago: ${metodoPagoTexto[factura.metodoPago] || factura.metodoPago}`;
        doc.text(textoPago, pageWidth / 2, yPos, { align: 'center' });
        yPos += 6;
      }

      // ========== PIE DE PÁGINA ==========
      const textoPie = config.factura.textoPie || 'Gracias por confiar en nosotros';
      doc.setFontSize(10);
      doc.setFont('helvetica', 'italic');
      doc.setTextColor(primaryRgb.r, primaryRgb.g, primaryRgb.b);
      doc.text(textoPie, pageWidth / 2, pageHeight - 15, { align: 'center' });

      // ========== DESCARGAR ==========
      doc.save(`factura_${factura.numero.replace(/\//g, '_')}.pdf`);

    } catch (err) {
      console.error('Error generando PDF:', err);
      alert('Error al generar PDF: ' + err.message);
    }
  };

  // Función auxiliar para formatear fecha
  const formatearFecha = (fecha) => {
    if (!fecha) return '—';

    try {
      let day, month, year;

      if (typeof fecha === 'string') {
        if (fecha.includes('/')) {
          const partes = fecha.split('/');
          day = parseInt(partes[0]);
          month = parseInt(partes[1]);
          year = parseInt(partes[2]);
        } else if (fecha.includes('-')) {
          const partes = fecha.split('-');
          year = parseInt(partes[0]);
          month = parseInt(partes[1]);
          day = parseInt(partes[2]);
        }
      }

      if (day && month && year) {
        return `${String(day).padStart(2, '0')}/${String(month).padStart(2, '0')}/${year}`;
      }
      return fecha;
    } catch (err) {
      console.error('Error formateando fecha:', err);
      return fecha;
    }
  };

  // Formatear moneda
  const formatearMoneda = (cantidad) => {
    if (!cantidad) return '€0,00';
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
    }).format(parseFloat(cantidad));
  };

  // Obtener color de estado
  const getColorEstado = (estado) => {
    switch (estado) {
      case 'PAGADA':
        return 'bg-green-100 text-green-800';
      case 'PENDIENTE':
        return 'bg-yellow-100 text-yellow-800';
      case 'CANCELADA':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const totalVentas = facturas.reduce((sum, factura) => sum + (parseFloat(factura.total) || 0), 0);

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-6">
        <div className="flex items-center gap-3">
          <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <img src="/assets/icons/invoice.png" alt="Facturas Emitidas" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
          </div>
          <div>
            <h2 className="text-2xl font-bold">Facturas Emitidas</h2>
            <p className="text-gray-500 text-sm mt-1">
              Total de ventas: {formatearMoneda(totalVentas)}
            </p>
          </div>
        </div>
        <button
          onClick={abrirModalNuevo}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded font-semibold"
        >
          + Nueva Factura
        </button>
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-8">
          <p className="text-gray-500">Cargando facturas...</p>
        </div>
      ) : facturas.length === 0 ? (
        <div className="text-center py-8">
          <p className="text-gray-500">No hay facturas registradas</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-gray-100 border-b">
                <th className="px-4 py-2 text-left font-semibold">Nº Factura</th>
                <th className="px-4 py-2 text-left font-semibold">Fecha</th>
                <th className="px-4 py-2 text-left font-semibold">Cliente</th>
                <th className="px-4 py-2 text-left font-semibold">Concepto</th>
                <th className="px-4 py-2 text-left font-semibold">Tipo</th>
                <th className="px-4 py-2 text-right font-semibold">Total</th>
                <th className="px-4 py-2 text-center font-semibold">Estado</th>
                <th className="px-4 py-2 text-center font-semibold">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {facturas.map(factura => (
                <tr key={factura.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-3 font-semibold text-blue-600">{factura.numero}</td>
                  <td className="px-4 py-3">{formatearFecha(factura.fecha)}</td>
                  <td className="px-4 py-3">{factura.clienteNombre}</td>
                  <td className="px-4 py-3 max-w-xs truncate text-sm text-gray-600">
                    {factura.lineas && factura.lineas.length > 0 
                      ? factura.lineas.map(l => l.concepto).join(', ')
                      : '-'}
                  </td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-1 bg-gray-200 rounded text-xs font-semibold">
                      {factura.tipo}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right font-semibold text-green-600">
                    {formatearMoneda(factura.total)}
                  </td>
                  <td className="px-4 py-3 text-center">
                    <span className={`px-2 py-1 rounded text-xs font-semibold ${getColorEstado(factura.estado)}`}>
                      {factura.estado}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-center space-x-2">
                    <button
                      onClick={() => abrirModalEditar(factura)}
                      className="text-blue-600 hover:text-blue-800 font-semibold text-sm"
                    >
                      Editar
                    </button>
                    {factura.estado === 'PENDIENTE' && (
                      <button
                        onClick={() => abrirModalCobrar(factura)}
                        className="text-green-600 hover:text-green-800 font-semibold text-sm"
                        title="Cobrar factura"
                      >
                        💰 Cobrar
                      </button>
                    )}
                    <button
                      onClick={() => generarPdf(factura.id)}
                      className="text-orange-600 hover:text-orange-800 font-semibold text-sm"
                    >
                      PDF
                    </button>
                    <button
                      onClick={() => eliminarFactura(factura.id)}
                      className="text-red-600 hover:text-red-800 font-semibold text-sm"
                    >
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Controles de paginación */}
          {totalPaginas > 1 && (
            <div className="flex items-center justify-between mt-6 px-4 py-3 bg-gray-50 rounded-lg border border-gray-200">
              <div className="text-sm text-gray-600">
                Mostrando <span className="font-semibold">{paginaActual * 20 + 1}</span> a <span className="font-semibold">{Math.min((paginaActual + 1) * 20, totalFacturas)}</span> de <span className="font-semibold">{totalFacturas}</span> facturas
              </div>
              
              <div className="flex gap-2">
                <button
                  onClick={() => irAlaPagina(paginaActual - 1)}
                  disabled={paginaActual === 0}
                  className="px-3 py-2 bg-blue-600 text-white rounded-lg font-semibold text-sm disabled:bg-gray-400 disabled:cursor-not-allowed hover:bg-blue-700 transition-colors"
                >
                  ← Anterior
                </button>

                <div className="flex items-center gap-1">
                  {Array.from({ length: totalPaginas }, (_, i) => (
                    <button
                      key={i}
                      onClick={() => irAlaPagina(i)}
                      className={`px-3 py-2 rounded-lg font-semibold text-sm transition-colors ${
                        paginaActual === i
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                      }`}
                    >
                      {i + 1}
                    </button>
                  ))}
                </div>

                <button
                  onClick={() => irAlaPagina(paginaActual + 1)}
                  disabled={paginaActual >= totalPaginas - 1}
                  className="px-3 py-2 bg-blue-600 text-white rounded-lg font-semibold text-sm disabled:bg-gray-400 disabled:cursor-not-allowed hover:bg-blue-700 transition-colors"
                >
                  Siguiente →
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Modal para crear/editar factura */}
      {modalAbierto && (
        <div className="fixed inset-0 bg-blue-900 bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-7xl w-full max-h-[95vh] flex flex-col">
            {/* Header del modal */}
            <div className="px-10 py-6 border-b border-gray-200 bg-gradient-to-r from-blue-50 to-blue-100">
              <h3 className="text-3xl font-bold text-gray-900">
                {editandoFactura ? 'Editar Factura Emitida' : 'Nueva Factura Emitida'}
              </h3>
            </div>

            {/* Contenido con scroll */}
            <div className="px-10 py-8 overflow-y-auto flex-1">
              <form onSubmit={guardarFactura} className="space-y-8">
                {/* Sección: Datos de la Factura */}
                <div>
                  <h4 className="text-2xl font-bold text-gray-900 mb-4 pb-3 border-b-2 border-blue-300">
                    Datos de la Factura
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                    <div>
                      <label className="block text-base font-semibold text-gray-700 mb-2">
                        Nº Factura*
                      </label>
                      <input
                        type="text"
                        name="numero"
                        value={formData.numero}
                        onChange={handleInputChange}
                        className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 text-base focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:border-transparent"
                        placeholder="Ej: 2026/001"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Fecha*
                      </label>
                      <input
                        type="date"
                        name="fecha"
                        value={formData.fecha}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Tipo de Factura*
                      </label>
                      <select
                        name="tipo"
                        value={formData.tipo}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      >
                        <option value="SIMPLIFICADA">Simplificada</option>
                        <option value="COMPLETA">Completa</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Estado
                      </label>
                      <select
                        name="estado"
                        value={formData.estado}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      >
                        <option value="PENDIENTE">Pendiente</option>
                        <option value="PAGADA">Pagada</option>
                        <option value="CANCELADA">Cancelada</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Método de Pago
                      </label>
                      <select
                        name="metodoPago"
                        value={formData.metodoPago}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      >
                        <option value="">-- Selecciona un método --</option>
                        {metodosPago.map((metodo) => (
                          <option key={metodo.valor} value={metodo.valor}>
                            {metodo.descripcion}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>

                {/* Sección: Datos del Cliente */}
                <div>
                  <h4 className="text-lg font-semibold text-gray-800 mb-3 pb-2 border-b border-gray-200">
                    Datos del Cliente
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="md:col-span-2">
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Seleccionar Cliente*
                      </label>
                      <select
                        name="clienteId"
                        value={formData.clienteId}
                        onChange={handleClienteChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      >
                        <option value="">-- Selecciona un cliente --</option>
                        {clientes.map(cliente => (
                          <option key={cliente.id} value={cliente.id}>
                            {cliente.nombre} {cliente.apellidos} {cliente.nif && `(${cliente.nif})`}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Nombre (autocompletado)
                      </label>
                      <input
                        type="text"
                        value={formData.clienteNombre}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-gray-50"
                        readOnly
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        NIF/CIF (autocompletado)
                      </label>
                      <input
                        type="text"
                        value={formData.clienteNif}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-gray-50"
                        readOnly
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Teléfono (autocompletado)
                      </label>
                      <input
                        type="text"
                        value={formData.clienteTelefono}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-gray-50"
                        readOnly
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Email (autocompletado)
                      </label>
                      <input
                        type="text"
                        value={formData.clienteEmail}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-gray-50"
                        readOnly
                      />
                    </div>
                    <div className="md:col-span-2">
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Dirección (autocompletada)
                      </label>
                      <input
                        type="text"
                        value={formData.clienteDireccion}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-gray-50"
                        readOnly
                      />
                    </div>
                  </div>
                </div>

                {/* Sección: Líneas de Factura */}
                <div>
                  <h4 className="text-lg font-semibold text-gray-800 mb-3 pb-2 border-b border-gray-200">
                    Líneas de Factura
                  </h4>

                  <div className="bg-blue-50 p-4 rounded-lg mb-4">
                    <div className="grid grid-cols-1 md:grid-cols-12 gap-3 items-end">
                      <div className="md:col-span-5">
                        <label className="block text-sm font-semibold text-gray-700 mb-1">
                          Concepto
                        </label>
                        <select
                          name="concepto"
                          value={nuevaLinea.concepto}
                          onChange={handleNuevaLineaChange}
                          className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                        >
                          <option value="">-- Selecciona un servicio --</option>
                          {tiposLavado.map((tipo) => (
                            <option key={tipo.valor} value={tipo.valor}>
                              {tipo.descripcion}
                            </option>
                          ))}
                        </select>
                      </div>
                      <div className="md:col-span-2">
                        <label className="block text-sm font-semibold text-gray-700 mb-1">
                          Cantidad
                        </label>
                        <input
                          type="number"
                          name="cantidad"
                          value={nuevaLinea.cantidad}
                          onChange={handleNuevaLineaChange}
                          step="1"
                          min="1"
                          className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                        />
                      </div>
                      <div className="md:col-span-3">
                        <label className="block text-sm font-semibold text-gray-700 mb-1">
                          Precio Unitario (€)
                        </label>
                        <input
                          type="number"
                          name="precioUnitario"
                          value={nuevaLinea.precioUnitario}
                          onChange={handleNuevaLineaChange}
                          step="0.01"
                          className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                          placeholder="0,00"
                        />
                      </div>
                      <div className="md:col-span-2">
                        <button
                          type="button"
                          onClick={agregarLinea}
                          className="w-full bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-semibold transition-colors"
                        >
                          + Agregar
                        </button>
                      </div>
                    </div>
                  </div>

                  {formData.lineas.length > 0 ? (
                    <div className="overflow-x-auto border border-gray-200 rounded-lg">
                      <table className="w-full">
                        <thead className="bg-gray-100">
                          <tr>
                            <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">Concepto</th>
                            <th className="px-4 py-2 text-center text-sm font-semibold text-gray-700">Cantidad</th>
                            <th className="px-4 py-2 text-right text-sm font-semibold text-gray-700">Precio Unit.</th>
                            <th className="px-4 py-2 text-right text-sm font-semibold text-gray-700">Subtotal</th>
                            <th className="px-4 py-2 text-center text-sm font-semibold text-gray-700">Acción</th>
                          </tr>
                        </thead>
                        <tbody>
                          {formData.lineas.map((linea) => (
                            <tr key={linea.id} className="border-t border-gray-200 hover:bg-gray-50">
                              <td className="px-4 py-3 text-sm">{linea.concepto}</td>
                              <td className="px-4 py-3 text-sm text-center">{linea.cantidad}</td>
                              <td className="px-4 py-3 text-sm text-right">{linea.precioUnitario.toFixed(2)} €</td>
                              <td className="px-4 py-3 text-sm text-right font-semibold">{linea.subtotal.toFixed(2)} €</td>
                              <td className="px-4 py-3 text-center">
                                <button
                                  type="button"
                                  onClick={() => eliminarLinea(linea.id)}
                                  className="text-red-600 hover:text-red-800 font-semibold text-sm"
                                >
                                  Eliminar
                                </button>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : (
                    <div className="text-center py-8 bg-gray-50 rounded-lg border-2 border-dashed border-gray-300">
                      <p className="text-gray-500">No hay líneas agregadas. Usa el formulario de arriba para agregar líneas.</p>
                    </div>
                  )}
                </div>

                {/* Sección: Importes */}
                <div>
                  <h4 className="text-lg font-semibold text-gray-800 mb-3 pb-2 border-b border-gray-200">
                    Importes (Calculados Automáticamente)
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Base Imponible
                      </label>
                      <input
                        type="text"
                        value={formData.baseImponible ? `${formData.baseImponible} €` : '0,00 €'}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-gray-50 font-semibold"
                        readOnly
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        IVA (%)
                      </label>
                      <select
                        name="tipoIva"
                        value={formData.tipoIva}
                        onChange={handleInputChange}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      >
                        <option value="0">0%</option>
                        <option value="4">4%</option>
                        <option value="10">10%</option>
                        <option value="21">21%</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Importe IVA
                      </label>
                      <input
                        type="text"
                        value={formData.importeIva ? `${formData.importeIva} €` : '0,00 €'}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-gray-50 font-semibold"
                        readOnly
                      />
                    </div>
                  </div>
                  <div className="mt-4">
                    <label className="block text-sm font-semibold text-gray-700 mb-1">
                      Total Factura
                    </label>
                    <input
                      type="text"
                      value={formData.total ? `${formData.total} €` : '0,00 €'}
                      className="w-full border-2 border-green-500 rounded-lg px-4 py-3 bg-green-50 text-xl font-bold text-green-700"
                      readOnly
                    />
                  </div>
                </div>

                {/* Sección: Observaciones */}
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">
                    Observaciones
                  </label>
                  <textarea
                    name="observaciones"
                    value={formData.observaciones}
                    onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    rows="3"
                    placeholder="Notas adicionales sobre la factura"
                  />
                </div>
              </form>
            </div>

            {/* Footer con botones */}
            <div className="px-8 py-5 border-t border-gray-200 bg-gray-50 flex justify-end gap-3 rounded-b-xl">
              <button
                type="button"
                onClick={cerrarModal}
                className="px-6 py-2.5 border-2 border-gray-300 rounded-lg font-semibold text-gray-700 hover:bg-gray-100 transition-colors"
              >
                Cancelar
              </button>
              <button
                type="submit"
                onClick={guardarFactura}
                className="px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-semibold transition-colors shadow-md hover:shadow-lg"
              >
                {editandoFactura ? 'Actualizar Factura' : 'Crear Factura'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal para cobrar factura */}
      {modalCobrarAbierto && facturaParaCobrar && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-2xl p-8 max-w-md w-full">
            <h3 className="text-2xl font-bold mb-4 text-gray-800">💰 Registrar Pago</h3>
            
            <div className="mb-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
              <p className="text-sm text-gray-600">Factura Nº</p>
              <p className="text-xl font-bold text-blue-600 mb-2">{facturaParaCobrar.numero}</p>
              <p className="text-sm text-gray-600">Cliente</p>
              <p className="text-lg font-semibold text-gray-800 mb-2">{facturaParaCobrar.clienteNombre}</p>
              <p className="text-sm text-gray-600">Importe a Cobrar</p>
              <p className="text-2xl font-bold text-green-600">
                {new Intl.NumberFormat('es-ES', {
                  style: 'currency',
                  currency: 'EUR',
                }).format(facturaParaCobrar.total)}
              </p>
            </div>

            <div className="mb-6">
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Método de Pago
              </label>
              <select
                value={metodoPagoCobro}
                onChange={(e) => setMetodoPagoCobro(e.target.value)}
                className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-green-500 focus:border-green-500"
              >
                <option value="">-- Selecciona un método --</option>
                {metodosPago.map((metodo) => (
                  <option key={metodo.valor} value={metodo.valor}>
                    {metodo.descripcion}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex gap-3">
              <button
                onClick={cerrarModalCobrar}
                className="flex-1 px-4 py-2.5 border-2 border-gray-300 rounded-lg font-semibold text-gray-700 hover:bg-gray-100 transition-colors"
              >
                Cancelar
              </button>
              <button
                onClick={procesarPago}
                disabled={loading}
                className="flex-1 px-4 py-2.5 bg-green-600 hover:bg-green-700 disabled:bg-gray-400 text-white rounded-lg font-semibold transition-colors shadow-md"
              >
                {loading ? 'Procesando...' : '✓ Confirmar Pago'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default FacturasEmitidas;