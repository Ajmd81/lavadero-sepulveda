import { useState, useEffect, useCallback } from 'react';
import {
    Download, FileText, Calendar, AlertCircle, CheckCircle,
    Calculator, TrendingUp, TrendingDown, Info
} from 'lucide-react';
import { format, startOfQuarter, endOfQuarter, parseISO, isWithinInterval } from 'date-fns';
import { jsPDF } from 'jspdf';
import 'jspdf-autotable';
import facturaService from '../../services/facturaService';
import facturaRecibidaService from '../../services/facturaRecibidaService';
import gastoService from '../../services/gastoService';

const ModelosFiscales = () => {
    const currentYear = new Date().getFullYear();
    const currentQuarter = Math.floor((new Date().getMonth() / 3)) + 1;

    const [modeloSeleccionado, setModeloSeleccionado] = useState('303');
    const [trimestre, setTrimestre] = useState(currentQuarter);
    const [año, setAño] = useState(currentYear);
    const [mensaje, setMensaje] = useState(null);
    const [facturas, setFacturas] = useState([]);
    const [facturasRecibidas, setFacturasRecibidas] = useState([]);
    const [gastos, setGastos] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [datosModelo, setDatosModelo] = useState(null);

    useEffect(() => {
        cargarTodosDatos();
    }, []);

    useEffect(() => {
        if (facturas.length > 0) {
            calcularModelo();
        }
    }, [facturas, facturasRecibidas, gastos, modeloSeleccionado, trimestre, año, calcularModelo]);

    const cargarTodosDatos = async () => {
        try {
            setIsLoading(true);
            console.log('📊 Cargando datos para modelos fiscales...');
            
            const [facturasResp, facturasRecibidasResp, gastosResp] = await Promise.all([
                facturaService.getAll(0, 1000),
                facturaRecibidaService.getAll(),
                gastoService.getAll()
            ]);
            
            // Procesar facturas emitidas
            let facturasData = [];
            if (Array.isArray(facturasResp)) {
                facturasData = facturasResp;
            } else if (facturasResp?.content && Array.isArray(facturasResp.content)) {
                facturasData = facturasResp.content;
            } else if (facturasResp?.data) {
                facturasData = Array.isArray(facturasResp.data) ? facturasResp.data : facturasResp.data.content || [];
            }

            // Procesar facturas recibidas
            let facturasRecibidasData = facturasRecibidasResp?.data || [];
            if (facturasRecibidasData?.content && Array.isArray(facturasRecibidasData.content)) {
                facturasRecibidasData = facturasRecibidasData.content;
            }
            if (!Array.isArray(facturasRecibidasData)) {
                facturasRecibidasData = [];
            }

            // Procesar gastos
            let gastosData = gastosResp?.data || [];
            if (gastosData?.content && Array.isArray(gastosData.content)) {
                gastosData = gastosData.content;
            }
            if (!Array.isArray(gastosData)) {
                gastosData = [];
            }

            console.log(`✅ Cargados: ${facturasData.length} facturas, ${facturasRecibidasData.length} facturas recibidas, ${gastosData.length} gastos`);
            
            setFacturas(facturasData);
            setFacturasRecibidas(facturasRecibidasData);
            setGastos(gastosData);
        } catch (error) {
            console.error('❌ Error cargando datos:', error);
            setMensaje({ tipo: 'error', texto: 'Error al cargar los datos' });
            setTimeout(() => setMensaje(null), 5000);
        } finally {
            setIsLoading(false);
        }
    };

    const parsearFecha = (fechaStr) => {
        if (!fechaStr) return null;
        try {
            if (fechaStr.includes('/')) {
                const [dia, mes, anio] = fechaStr.split('/');
                return new Date(parseInt(anio), parseInt(mes) - 1, parseInt(dia));
            }
            if (fechaStr.includes('-')) {
                return parseISO(fechaStr);
            }
            return null;
        } catch {
            return null;
        }
    };

    const calcularModelo = useCallback(() => {
        const date = new Date(año, (trimestre - 1) * 3, 1);
        const fechas = {
            desde: startOfQuarter(date),
            hasta: endOfQuarter(date)
        };
        
        // Filtrar por período
        const filtrarPorFecha = (items) => items.filter(item => {
            const fechaItem = parsearFecha(item.fecha || item.fechaFactura);
            if (!fechaItem) return false;
            return isWithinInterval(fechaItem, { start: fechas.desde, end: fechas.hasta });
        });

        const facturasPeriodo = filtrarPorFecha(facturas);
        const facturasRecibidasPeriodo = filtrarPorFecha(facturasRecibidas);
        const gastosPeriodo = filtrarPorFecha(gastos);

        console.log(`Calculando modelo ${modeloSeleccionado} para ${trimestre}T ${año}`);
        console.log(`Facturas: ${facturasPeriodo.length}, Facturas recibidas: ${facturasRecibidasPeriodo.length}, Gastos: ${gastosPeriodo.length}`);

        // CALCULAR MODELO 303 - IVA
        if (modeloSeleccionado === '303') {
            // INGRESOS
            const baseIngresos = facturasPeriodo.reduce((sum, f) => {
                const base = typeof f.baseImponible === 'number' ? f.baseImponible : parseFloat(f.baseImponible || 0);
                return sum + base;
            }, 0);

            const totalIngresos = facturasPeriodo.reduce((sum, f) => {
                const total = typeof f.total === 'number' ? f.total : parseFloat(f.total || 0);
                return sum + total;
            }, 0);

            const ivaRepercutido = totalIngresos - baseIngresos;

            // GASTOS - Facturas Recibidas + Gastos Simples
            const baseFacturasRecibidas = facturasRecibidasPeriodo.reduce((sum, f) => {
                const base = typeof f.baseImponible === 'number' ? f.baseImponible : parseFloat(f.baseImponible || 0);
                return sum + base;
            }, 0);

            const totalFacturasRecibidas = facturasRecibidasPeriodo.reduce((sum, f) => {
                const total = typeof f.total === 'number' ? f.total : parseFloat(f.total || 0);
                return sum + total;
            }, 0);

            const gastosSimples = gastosPeriodo.reduce((sum, g) => {
                const importe = typeof g.importe === 'number' ? g.importe : parseFloat(g.importe || 0);
                return sum + importe;
            }, 0);

            // Base Gastos = Base Facturas Recibidas + Gastos Simples (sin IVA)
            const baseGastos = baseFacturasRecibidas + gastosSimples;
            
            // IVA Soportado = Solo de facturas recibidas (gastos simples no tienen IVA)
            const ivaSoportado = totalFacturasRecibidas - baseFacturasRecibidas;
            const liquidacion = ivaRepercutido - ivaSoportado;

            setDatosModelo({
                baseIngresos,
                ivaRepercutido,
                baseGastos,
                ivaSoportado,
                liquidacion
            });
        }

        // CALCULAR MODELO 130 - IRPF
        else if (modeloSeleccionado === '130') {
            const ingresosTrimestre = facturasPeriodo.reduce((sum, f) => {
                const total = typeof f.total === 'number' ? f.total : parseFloat(f.total || 0);
                return sum + total;
            }, 0);

            // Gastos = Facturas recibidas + Gastos simples
            const gastosFacturasRecibidas = facturasRecibidasPeriodo.reduce((sum, f) => {
                const total = typeof f.total === 'number' ? f.total : parseFloat(f.total || 0);
                return sum + total;
            }, 0);

            const gastosSimples = gastosPeriodo.reduce((sum, g) => {
                const importe = typeof g.importe === 'number' ? g.importe : parseFloat(g.importe || 0);
                return sum + importe;
            }, 0);

            const gastosDeducibles = gastosFacturasRecibidas + gastosSimples;
            const beneficio = ingresosTrimestre - gastosDeducibles;
            const pagoACuenta = beneficio > 0 ? beneficio * 0.20 : 0; // 20% del beneficio

            setDatosModelo({
                ingresosTrimestre,
                gastosDeducibles,
                beneficio,
                pagoACuenta
            });
        }

        // CALCULAR MODELO 111 - Retenciones
        else if (modeloSeleccionado === '111') {
            setDatosModelo({
                totalRetenciones: 0,
                numeroPerceptores: 0
            });
        }
    }, [facturas, facturasRecibidas, gastos, modeloSeleccionado, trimestre, año]);

    const generarPDF = async () => {
        try {
            if (!datosModelo) {
                setMensaje({ tipo: 'error', texto: 'No hay datos para generar el PDF' });
                return;
            }

            const doc = new jsPDF();
            const pageWidth = doc.internal.pageSize.getWidth();
            const pageHeight = doc.internal.pageSize.getHeight();
            let yPosition = 15;

            // Título
            doc.setFontSize(18);
            doc.setTextColor(40, 40, 40);
            doc.text(`Modelo ${modeloSeleccionado}`, pageWidth / 2, yPosition, { align: 'center' });

            yPosition += 8;
            doc.setFontSize(12);
            doc.setTextColor(100, 100, 100);
            const modeloInfo = modelos.find(m => m.id === modeloSeleccionado);
            doc.text(modeloInfo?.descripcion || '', pageWidth / 2, yPosition, { align: 'center' });

            yPosition += 12;
            doc.setFontSize(10);
            doc.setTextColor(0, 0, 0);
            doc.text(`Período: ${trimestre}T ${año}`, 20, yPosition);
            doc.text(`Generado: ${format(new Date(), 'dd/MM/yyyy HH:mm')}`, pageWidth - 20, yPosition, { align: 'right' });

            yPosition += 15;

            // Contenido según modelo
            if (modeloSeleccionado === '303') {
                doc.setFontSize(12);
                doc.setTextColor(40, 40, 40);
                doc.text('Declaración de IVA', 20, yPosition);

                yPosition += 10;
                doc.setFontSize(10);
                doc.setTextColor(0, 0, 0);

                const datos303 = [
                    ['Concepto', 'Importe'],
                    ['IVA Repercutido (Ventas)', formatCurrency(datosModelo.ivaRepercutido)],
                    ['Base Ingresos', formatCurrency(datosModelo.baseIngresos)],
                    ['', ''],
                    ['IVA Soportado (Compras)', formatCurrency(datosModelo.ivaSoportado)],
                    ['Base Gastos', formatCurrency(datosModelo.baseGastos)],
                    ['', ''],
                    ['RESULTADO', formatCurrency(datosModelo.liquidacion)]
                ];

                doc.autoTable({
                    head: [datos303[0]],
                    body: datos303.slice(1),
                    startY: yPosition,
                    theme: 'grid',
                    headStyles: { fillColor: [66, 139, 202], textColor: 255, fontStyle: 'bold' },
                    bodyStyles: { textColor: 0 },
                    alternateRowStyles: { fillColor: [245, 245, 245] },
                    columnStyles: { 1: { halign: 'right' } },
                    margin: { left: 20, right: 20 }
                });

            } else if (modeloSeleccionado === '130') {
                doc.setFontSize(12);
                doc.setTextColor(40, 40, 40);
                doc.text('Pago Fraccionado IRPF', 20, yPosition);

                yPosition += 10;
                doc.setFontSize(10);
                doc.setTextColor(0, 0, 0);

                const datos130 = [
                    ['Concepto', 'Importe'],
                    ['Ingresos del Trimestre', formatCurrency(datosModelo.ingresosTrimestre)],
                    ['Gastos Deducibles', formatCurrency(datosModelo.gastosDeducibles)],
                    ['Beneficio Neto', formatCurrency(datosModelo.beneficio)],
                    ['Pago a Cuenta (20%)', formatCurrency(datosModelo.pagoACuenta)]
                ];

                doc.autoTable({
                    head: [datos130[0]],
                    body: datos130.slice(1),
                    startY: yPosition,
                    theme: 'grid',
                    headStyles: { fillColor: [66, 139, 202], textColor: 255, fontStyle: 'bold' },
                    bodyStyles: { textColor: 0 },
                    alternateRowStyles: { fillColor: [245, 245, 245] },
                    columnStyles: { 1: { halign: 'right' } },
                    margin: { left: 20, right: 20 }
                });

            } else if (modeloSeleccionado === '111') {
                doc.setFontSize(12);
                doc.setTextColor(40, 40, 40);
                doc.text('Retenciones IRPF', 20, yPosition);

                yPosition += 10;
                doc.setFontSize(10);
                doc.setTextColor(0, 0, 0);

                const datos111 = [
                    ['Concepto', 'Importe'],
                    ['Total Retenciones', formatCurrency(datosModelo.totalRetenciones)],
                    ['Número de Perceptores', (datosModelo.numeroPerceptores || 0).toString()]
                ];

                doc.autoTable({
                    head: [datos111[0]],
                    body: datos111.slice(1),
                    startY: yPosition,
                    theme: 'grid',
                    headStyles: { fillColor: [66, 139, 202], textColor: 255, fontStyle: 'bold' },
                    bodyStyles: { textColor: 0 },
                    alternateRowStyles: { fillColor: [245, 245, 245] },
                    columnStyles: { 1: { halign: 'right' } },
                    margin: { left: 20, right: 20 }
                });
            }

            // Pie de página
            const pageCount = doc.internal.getPages().length;
            for (let i = 1; i <= pageCount; i++) {
                doc.setPage(i);
                doc.setFontSize(8);
                doc.setTextColor(150, 150, 150);
                doc.text(
                    `Página ${i} de ${pageCount}`,
                    pageWidth / 2,
                    pageHeight - 10,
                    { align: 'center' }
                );
            }

            // Descargar
            const nombreArchivo = `Modelo_${modeloSeleccionado}_${trimestre}T_${año}.pdf`;
            doc.save(nombreArchivo);

            setMensaje({ tipo: 'exito', texto: `PDF descargado: ${nombreArchivo}` });
            setTimeout(() => setMensaje(null), 5000);

        } catch (error) {
            console.error('Error generando PDF:', error);
            setMensaje({ tipo: 'error', texto: 'Error al generar el PDF' });
            setTimeout(() => setMensaje(null), 5000);
        }
    };

    const generarTXT = () => {
        try {
            if (!datosModelo) {
                setMensaje({ tipo: 'error', texto: 'No hay datos para generar el TXT' });
                return;
            }

            let contenido = '';
            const ahora = new Date();
            const nif = 'XXXXXXXXX'; // Placeholder, idealmente desde config/usuario

            // Encabezado común
            contenido += `MODELO FISCAL ${modeloSeleccionado}\n`;
            contenido += `====================================\n\n`;
            contenido += `Período: ${trimestre}T ${año}\n`;
            contenido += `NIF: ${nif}\n`;
            contenido += `Generado: ${format(ahora, 'dd/MM/yyyy HH:mm')}\n`;
            contenido += `====================================\n\n`;

            if (modeloSeleccionado === '303') {
                contenido += `DECLARACIÓN DE IVA (MODELO 303)\n`;
                contenido += `--------------------------------\n\n`;
                contenido += `OPERACIONES INTERIORES CON BIENES\n`;
                contenido += `Base Ingresos (21%)          : ${formatCurrency(datosModelo.baseIngresos)}\n`;
                contenido += `IVA Repercutido              : ${formatCurrency(datosModelo.ivaRepercutido)}\n\n`;
                contenido += `CUOTA IMPUESTO SOPORTADO\n`;
                contenido += `Base Gastos (21%)            : ${formatCurrency(datosModelo.baseGastos)}\n`;
                contenido += `IVA Soportado                : ${formatCurrency(datosModelo.ivaSoportado)}\n\n`;
                contenido += `RESULTADO\n`;
                contenido += `====================================\n`;
                if (datosModelo.liquidacion >= 0) {
                    contenido += `CANTIDAD A INGRESAR          : ${formatCurrency(datosModelo.liquidacion)}\n`;
                } else {
                    contenido += `CANTIDAD A COMPENSAR         : ${formatCurrency(Math.abs(datosModelo.liquidacion))}\n`;
                }
                contenido += `====================================\n\n`;
                contenido += `NOTAS:\n`;
                contenido += `- Declaración generada automáticamente desde sistema de facturación\n`;
                contenido += `- Revise que todas las facturas del período estén registradas\n`;
                contenido += `- Incluye IVA soportado de facturas recibidas\n`;

            } else if (modeloSeleccionado === '130') {
                contenido += `PAGO FRACCIONADO IRPF (MODELO 130)\n`;
                contenido += `--------------------------------\n\n`;
                contenido += `INGRESOS\n`;
                contenido += `Ingresos del Trimestre       : ${formatCurrency(datosModelo.ingresosTrimestre)}\n\n`;
                contenido += `GASTOS DEDUCIBLES\n`;
                contenido += `Gastos Totales               : ${formatCurrency(datosModelo.gastosDeducibles)}\n\n`;
                contenido += `RESULTADO\n`;
                contenido += `Beneficio Neto               : ${formatCurrency(datosModelo.beneficio)}\n`;
                contenido += `Porcentaje Retención (20%)   : 20%\n`;
                contenido += `Pago a Cuenta                : ${formatCurrency(datosModelo.pagoACuenta)}\n`;
                contenido += `====================================\n\n`;
                contenido += `NOTAS:\n`;
                contenido += `- Incluye gastos de facturas recibidas y gastos registrados\n`;
                contenido += `- Obligatorio para autónomos en estimación directa\n`;

            } else if (modeloSeleccionado === '111') {
                contenido += `RETENCIONES IRPF (MODELO 111)\n`;
                contenido += `--------------------------------\n\n`;
                contenido += `Total Retenciones            : ${formatCurrency(datosModelo.totalRetenciones)}\n`;
                contenido += `Número de Perceptores       : ${datosModelo.numeroPerceptores || 0}\n`;
                contenido += `====================================\n\n`;
                contenido += `NOTAS:\n`;
                contenido += `- Declara retenciones practicadas a trabajadores y profesionales\n`;
            }

            contenido += `\nJustificación: Generado automáticamente por sistema de gestión fiscal\n`;

            // Crear blob y descargar
            const blob = new Blob([contenido], { type: 'text/plain;charset=utf-8' });
            const enlace = document.createElement('a');
            const url = URL.createObjectURL(blob);
            
            enlace.setAttribute('href', url);
            enlace.setAttribute('download', `Modelo_${modeloSeleccionado}_${trimestre}T_${año}.txt`);
            enlace.style.visibility = 'hidden';
            
            document.body.appendChild(enlace);
            enlace.click();
            document.body.removeChild(enlace);

            setMensaje({ tipo: 'exito', texto: `TXT descargado: Modelo_${modeloSeleccionado}_${trimestre}T_${año}.txt` });
            setTimeout(() => setMensaje(null), 5000);

        } catch (error) {
            console.error('Error generando TXT:', error);
            setMensaje({ tipo: 'error', texto: 'Error al generar el TXT' });
            setTimeout(() => setMensaje(null), 5000);
        }
    };

    const formatCurrency = (value) => {
        return new Intl.NumberFormat('es-ES', {
            style: 'currency',
            currency: 'EUR'
        }).format(value || 0);
    };

    const modelos = [
        { id: '303', nombre: 'Modelo 303 - IVA', descripcion: 'Declaración trimestral de IVA' },
        { id: '130', nombre: 'Modelo 130 - IRPF', descripcion: 'Pago fraccionado IRPF (autónomos)' },
        { id: '111', nombre: 'Modelo 111 - Retenciones', descripcion: 'Retenciones e ingresos a cuenta' }
    ];

    const trimestres = [
        { value: 1, label: '1T (Ene-Mar)', meses: 'Enero - Marzo' },
        { value: 2, label: '2T (Abr-Jun)', meses: 'Abril - Junio' },
        { value: 3, label: '3T (Jul-Sep)', meses: 'Julio - Septiembre' },
        { value: 4, label: '4T (Oct-Dic)', meses: 'Octubre - Diciembre' }
    ];

    const años = Array.from({ length: 5 }, (_, i) => currentYear - 2 + i);

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center gap-3">
                    <div style={{ width: 48, height: 48, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <img src="/assets/icons/modeloFiscal.png" alt="Modelos Fiscales" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                    </div>
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">Modelos Fiscales</h1>
                        <p className="text-gray-600">Genera y consulta tus declaraciones tributarias</p>
                    </div>
                </div>
            </div>

            {/* Mensajes */}
            {mensaje && (
                <div className={`rounded-lg p-4 flex items-center justify-between ${mensaje.tipo === 'exito' ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'
                    }`}>
                    <div className="flex items-center">
                        {mensaje.tipo === 'exito' ? (
                            <CheckCircle className="text-green-600 mr-2" size={20} />
                        ) : (
                            <AlertCircle className="text-red-600 mr-2" size={20} />
                        )}
                        <span className={mensaje.tipo === 'exito' ? 'text-green-800' : 'text-red-800'}>
                            {mensaje.texto}
                        </span>
                    </div>
                </div>
            )}

            {/* Selección de Modelo y Período */}
            <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-4">Seleccionar Modelo y Período</h2>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                    {/* Modelo */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Tipo de Modelo
                        </label>
                        <select
                            value={modeloSeleccionado}
                            onChange={(e) => setModeloSeleccionado(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        >
                            {modelos.map((modelo) => (
                                <option key={modelo.id} value={modelo.id}>
                                    {modelo.nombre}
                                </option>
                            ))}
                        </select>
                        <p className="text-xs text-gray-500 mt-1">
                            {modelos.find(m => m.id === modeloSeleccionado)?.descripcion}
                        </p>
                    </div>

                    {/* Trimestre */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Trimestre
                        </label>
                        <select
                            value={trimestre}
                            onChange={(e) => setTrimestre(Number(e.target.value))}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        >
                            {trimestres.map((t) => (
                                <option key={t.value} value={t.value}>
                                    {t.label}
                                </option>
                            ))}
                        </select>
                        <p className="text-xs text-gray-500 mt-1">
                            {trimestres.find(t => t.value === trimestre)?.meses}
                        </p>
                    </div>

                    {/* Año */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Año Fiscal
                        </label>
                        <select
                            value={año}
                            onChange={(e) => setAño(Number(e.target.value))}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        >
                            {años.map((y) => (
                                <option key={y} value={y}>
                                    {y}
                                </option>
                            ))}
                        </select>
                        <p className="text-xs text-gray-500 mt-1">
                            Ejercicio fiscal
                        </p>
                    </div>
                </div>

                <button
                    onClick={calcularModelo}
                    disabled={isLoading}
                    className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                >
                    <Calculator size={20} />
                    {isLoading ? 'Calculando...' : 'Recalcular Modelo'}
                </button>
            </div>

            {/* Información del Período */}
            {!isLoading && datosModelo && (
                <>
                    {/* Modelo 303 - IVA */}
                    {modeloSeleccionado === '303' && (
                        <div className="bg-white rounded-lg shadow p-6">
                            <div className="flex items-center justify-between mb-6">
                                <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                                    <FileText size={24} />
                                    Modelo 303 - Declaración de IVA
                                </h2>
                                <div className="flex gap-2">
                                    <button
                                        onClick={generarPDF}
                                        className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                                    >
                                        <Download size={20} />
                                        Descargar PDF
                                    </button>
                                    <button
                                        onClick={generarTXT}
                                        className="flex items-center gap-2 px-4 py-2 bg-orange-600 text-white rounded-lg hover:bg-orange-700 transition-colors"
                                    >
                                        <FileText size={20} />
                                        Descargar TXT
                                    </button>
                                </div>
                            </div>

                            {/* Resumen de Importes */}
                            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
                                <div className="p-4 bg-green-50 rounded-lg border border-green-200">
                                    <p className="text-sm text-gray-600 mb-1">IVA Repercutido</p>
                                    <p className="text-2xl font-bold text-red-700">
                                        {formatCurrency(datosModelo.ivaSoportado)}
                                    </p>
                                    <p className="text-xs text-gray-500 mt-1">
                                        Base: {formatCurrency(datosModelo.baseGastos)}
                                    </p>
                                </div>

                                <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
                                    <p className="text-sm text-gray-600 mb-1">Resultado</p>
                                    <p className={`text-2xl font-bold ${datosModelo.liquidacion >= 0 ? 'text-blue-700' : 'text-red-700'
                                        }`}>
                                        {formatCurrency(datosModelo.liquidacion)}
                                    </p>
                                    <p className="text-xs text-gray-500 mt-1">
                                        {datosModelo.liquidacion >= 0 ? 'A ingresar' : 'A compensar'}
                                    </p>
                                </div>

                                <div className="p-4 bg-purple-50 rounded-lg border border-purple-200">
                                    <p className="text-sm text-gray-600 mb-1">Período</p>
                                    <p className="text-2xl font-bold text-purple-700">
                                        {trimestre}T {año}
                                    </p>
                                    <p className="text-xs text-gray-500 mt-1">
                                        {trimestres.find(t => t.value === trimestre)?.meses}
                                    </p>
                                </div>
                            </div>

                            {/* Detalle de Operaciones */}
                            <div className="border-t border-gray-200 pt-6">
                                <h3 className="text-lg font-bold text-gray-900 mb-4">Detalle de Operaciones</h3>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    {/* IVA Devengado */}
                                    <div>
                                        <h4 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                                            <TrendingUp size={20} className="text-green-600" />
                                            IVA Devengado (Ventas)
                                        </h4>
                                        <table className="min-w-full text-sm">
                                            <tbody className="divide-y divide-gray-200">
                                                <tr>
                                                    <td className="py-2 text-gray-600">Régimen General (21%)</td>
                                                    <td className="py-2 text-right font-medium">
                                                        {formatCurrency(datosModelo.baseIngresos)}
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td className="py-2 text-gray-600">IVA repercutido (21%)</td>
                                                    <td className="py-2 text-right font-bold text-green-700">
                                                        {formatCurrency(datosModelo.ivaRepercutido)}
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>

                                    {/* IVA Deducible */}
                                    <div>
                                        <h4 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                                            <TrendingDown size={20} className="text-red-600" />
                                            IVA Deducible (Compras)
                                        </h4>
                                        <table className="min-w-full text-sm">
                                            <tbody className="divide-y divide-gray-200">
                                                <tr>
                                                    <td className="py-2 text-gray-600">Bienes corrientes</td>
                                                    <td className="py-2 text-right font-medium">
                                                        {formatCurrency(datosModelo.baseGastos)}
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td className="py-2 text-gray-600">IVA soportado</td>
                                                    <td className="py-2 text-right font-bold text-red-700">
                                                        {formatCurrency(datosModelo.ivaSoportado)}
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>

                            {/* Liquidación Final */}
                            <div className="border-t border-gray-200 pt-6 mt-6">
                                <div className="bg-gray-50 rounded-lg p-4">
                                    <div className="flex justify-between items-center mb-4">
                                        <span className="text-lg font-bold text-gray-900">RESULTADO DE LA LIQUIDACIÓN</span>
                                        <span className={`text-3xl font-bold ${datosModelo.liquidacion >= 0 ? 'text-blue-700' : 'text-green-700'
                                            }`}>
                                            {formatCurrency(datosModelo.liquidacion)}
                                        </span>
                                    </div>
                                    {datosModelo.liquidacion >= 0 ? (
                                        <p className="text-sm text-gray-600 mt-2">
                                            <Info size={16} className="inline mr-1" />
                                            Cantidad a ingresar en la Agencia Tributaria
                                        </p>
                                    ) : (
                                        <p className="text-sm text-gray-600 mt-2">
                                            <Info size={16} className="inline mr-1" />
                                            Cantidad a compensar en futuras declaraciones
                                        </p>
                                    )}
                                </div>
                            </div>

                            {/* Información Adicional */}
                            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                                <h3 className="font-bold text-blue-900 mb-2">📋 Información Importante</h3>
                                <ul className="text-sm text-blue-800 space-y-1">
                                    <li>• La declaración del Modelo 303 debe presentarse dentro de los 20 primeros días naturales del mes siguiente al trimestre</li>
                                    <li>• Los datos mostrados son calculados automáticamente desde las facturas y gastos registrados</li>
                                    <li>• Revisa que todas las facturas del período estén correctamente registradas</li>
                                    <li>• ✅ Incluye IVA soportado de facturas recibidas</li>
                                </ul>
                            </div>
                        </div>
                    )}

                    {/* Modelo 130 - IRPF */}
                    {modeloSeleccionado === '130' && (
                        <div className="bg-white rounded-lg shadow p-6">
                            <div className="flex items-center justify-between mb-6">
                                <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                                    <FileText size={24} />
                                    Modelo 130 - Pago Fraccionado IRPF
                                </h2>
                                <div className="flex gap-2">
                                    <button
                                        onClick={generarPDF}
                                        className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                                    >
                                        <Download size={20} />
                                        Descargar PDF
                                    </button>
                                    <button
                                        onClick={generarTXT}
                                        className="flex items-center gap-2 px-4 py-2 bg-orange-600 text-white rounded-lg hover:bg-orange-700 transition-colors"
                                    >
                                        <FileText size={20} />
                                        Descargar TXT
                                    </button>
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                                <div className="p-4 bg-green-50 rounded-lg border border-green-200">
                                    <p className="text-sm text-gray-600 mb-1">Ingresos del Trimestre</p>
                                    <p className="text-2xl font-bold text-green-700">
                                        {formatCurrency(datosModelo.ingresosTrimestre)}
                                    </p>
                                </div>

                                <div className="p-4 bg-red-50 rounded-lg border border-red-200">
                                    <p className="text-sm text-gray-600 mb-1">Gastos Deducibles</p>
                                    <p className="text-2xl font-bold text-red-700">
                                        {formatCurrency(datosModelo.gastosDeducibles)}
                                    </p>
                                </div>

                                <div className="p-4 bg-purple-50 rounded-lg border border-purple-200">
                                    <p className="text-sm text-gray-600 mb-1">Beneficio Neto</p>
                                    <p className="text-2xl font-bold text-purple-700">
                                        {formatCurrency(datosModelo.beneficio)}
                                    </p>
                                </div>

                                <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
                                    <p className="text-sm text-gray-600 mb-1">Pago a Cuenta (20%)</p>
                                    <p className="text-2xl font-bold text-blue-700">
                                        {formatCurrency(datosModelo.pagoACuenta)}
                                    </p>
                                </div>
                            </div>

                            <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
                                <p className="text-sm text-blue-800">
                                    <Info size={16} className="inline mr-1" />
                                    El Modelo 130 es obligatorio para autónomos en estimación directa. Se aplica un 20% sobre el beneficio neto.
                                    <br />
                                    ✅ Incluye gastos de facturas recibidas y gastos registrados
                                </p>
                            </div>
                        </div>
                    )}

                    {/* Modelo 111 - Retenciones */}
                    {modeloSeleccionado === '111' && (
                        <div className="bg-white rounded-lg shadow p-6">
                            <div className="flex items-center justify-between mb-6">
                                <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                                    <FileText size={24} />
                                    Modelo 111 - Retenciones IRPF
                                </h2>
                                <div className="flex gap-2">
                                    <button
                                        onClick={generarPDF}
                                        className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                                    >
                                        <Download size={20} />
                                        Descargar PDF
                                    </button>
                                    <button
                                        onClick={generarTXT}
                                        className="flex items-center gap-2 px-4 py-2 bg-orange-600 text-white rounded-lg hover:bg-orange-700 transition-colors"
                                    >
                                        <FileText size={20} />
                                        Descargar TXT
                                    </button>
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
                                    <p className="text-sm text-gray-600 mb-1">Total Retenciones</p>
                                    <p className="text-2xl font-bold text-blue-700">
                                        {formatCurrency(datosModelo.totalRetenciones)}
                                    </p>
                                </div>

                                <div className="p-4 bg-purple-50 rounded-lg border border-purple-200">
                                    <p className="text-sm text-gray-600 mb-1">Número de Perceptores</p>
                                    <p className="text-2xl font-bold text-purple-700">
                                        {datosModelo.numeroPerceptores || 0}
                                    </p>
                                </div>
                            </div>

                            <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
                                <p className="text-sm text-blue-800">
                                    <Info size={16} className="inline mr-1" />
                                    Declara las retenciones practicadas a trabajadores y profesionales durante el trimestre.
                                    <br />
                                    ⚠️ Nota: No hay datos de retenciones disponibles en el sistema actual.
                                </p>
                            </div>
                        </div>
                    )}
                </>
            )}

            {/* Loading State */}
            {isLoading && (
                <div className="bg-white rounded-lg shadow p-12">
                    <div className="flex flex-col items-center justify-center">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
                        <p className="text-gray-600">Cargando datos...</p>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ModelosFiscales;