import React, { useState, useEffect } from 'react';
import { FileText, Download, AlertCircle, CheckCircle, ChevronDown, ChevronUp, Info } from 'lucide-react';
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// ─────────────────────────────────────────────────────────────────────────────
// Datos fiscales del emisor — obtenidos del backend en tiempo de ejecución.
// NUNCA se hardcodean aquí: el NIF/nombre viven en las variables de entorno
// de Railway (APP_FISCAL_EMISOR_NIF, etc.) y solo el backend los conoce.
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// Helpers de fecha
// ─────────────────────────────────────────────────────────────────────────────
const fmt = (v) =>
    new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(v || 0);

const currentYear = new Date().getFullYear();

const TRIMESTRES = [
    { value: 1, label: '1T (Ene–Mar)', meses: [0, 1, 2] },
    { value: 2, label: '2T (Abr–Jun)', meses: [3, 4, 5] },
    { value: 3, label: '3T (Jul–Sep)', meses: [6, 7, 8] },
    { value: 4, label: '4T (Oct–Dic)', meses: [9, 10, 11] },
];

/**
 * Parsea fechas en cualquiera de los formatos que devuelve el backend:
 *   - "dd/MM/yyyy"  (FacturaDTO, GastoDTO con @JsonFormat)
 *   - "yyyy-MM-dd"  (LocalDate sin anotación)
 */
function parsearFecha(str) {
    if (!str) return null;
    if (typeof str !== 'string') return null;
    // dd/MM/yyyy
    if (/^\d{2}\/\d{2}\/\d{4}$/.test(str)) {
        const [d, m, y] = str.split('/');
        return new Date(Number(y), Number(m) - 1, Number(d));
    }
    // yyyy-MM-dd
    if (/^\d{4}-\d{2}-\d{2}$/.test(str)) {
        const [y, m, d] = str.split('-');
        return new Date(Number(y), Number(m) - 1, Number(d));
    }
    return new Date(str);
}

function enTrimestre(fechaStr, trimestre, anio) {
    const d = parsearFecha(fechaStr);
    if (!d || isNaN(d)) return false;
    const meses = TRIMESTRES.find(t => t.value === trimestre)?.meses || [];
    return d.getFullYear() === anio && meses.includes(d.getMonth());
}

// ─────────────────────────────────────────────────────────────────────────────
// Cálculos de IVA
// Los DTOs ya traen baseImponible, tipoIva, importeIva/cuotaIva calculados.
// Los usamos directamente; solo recalculamos si faltan.
// ─────────────────────────────────────────────────────────────────────────────

/** Extrae base e IVA de una FacturaDTO emitida. */
function ivaFacturaEmitida(f) {
    // El DTO devuelve baseImponible, tipoIva (%), importeIva y total
    const base = parseFloat(f.baseImponible || 0);
    const cuota = parseFloat(f.importeIva || 0);
    // Fallback si el backend no calculó importeIva
    if (!base && f.total) {
        const total = parseFloat(f.total);
        const tipo = parseFloat(f.tipoIva || 21);
        const baseCalc = total / (1 + tipo / 100);
        return { base: baseCalc, cuota: total - baseCalc };
    }
    return { base, cuota };
}

/** Extrae base e IVA de una FacturaRecibidaDTO. */
function ivaFacturaRecibida(f) {
    // FacturaRecibidaDTO tiene baseImponible, tipoIva (%), cuotaIva y total
    const base = parseFloat(f.baseImponible || 0);
    const cuota = parseFloat(f.cuotaIva || 0);
    if (!base && f.total) {
        const total = parseFloat(f.total);
        const tipo = parseFloat(f.tipoIva || 21);
        const baseCalc = total / (1 + tipo / 100);
        return { base: baseCalc, cuota: total - baseCalc };
    }
    return { base, cuota };
}

/** Extrae base e IVA de un GastoDTO.
 *  GastoDTO puede tener baseImponible + cuotaIva ya calculados,
 *  o solo importe + ivaIncluido. */
function ivaGasto(g) {
    if (g.baseImponible != null && g.cuotaIva != null) {
        return {
            base: parseFloat(g.baseImponible || 0),
            cuota: parseFloat(g.cuotaIva || 0),
        };
    }
    const importe = parseFloat(g.importe || 0);
    if (!g.ivaIncluido) return { base: importe, cuota: 0 };
    const tipo = 21; // tipo por defecto para gastos sin desglose
    const base = importe / (1 + tipo / 100);
    return { base, cuota: importe - base };
}

// ─────────────────────────────────────────────────────────────────────────────
// Componente principal
// ─────────────────────────────────────────────────────────────────────────────
const ModelosFiscales = () => {
    const [anio, setAnio] = useState(currentYear);
    const [trimestre, setTrimestre] = useState(1);
    const [modelo, setModelo] = useState('303');

    const [emisor, setEmisor] = useState(null);   // { nif, nombre } del backend
    const [facturas, setFacturas] = useState([]);
    const [facturasRec, setFacturasRec] = useState([]);
    const [gastos, setGastos] = useState([]);
    const [cargando, setCargando] = useState(false);
    const [descargando, setDescargando] = useState(false);
    const [mensaje, setMensaje] = useState(null);
    const [detalleVisible, setDetalleVisible] = useState(false);

    // ── Carga de datos ───────────────────────────────────────────────────────
    useEffect(() => {
        const cargar = async () => {
            setCargando(true);
            try {
                // Obtener datos del emisor desde el backend (NIF y nombre nunca viajan al cliente)
                const respEmisor = await axios.get(`${API_URL}/api/facturas/emisor`);
                setEmisor(respEmisor.data);   // { nif, nombre, direccion }

                const [respF, respFR, respG] = await Promise.allSettled([
                    // Facturas emitidas: /todas devuelve List<FacturaDTO> sin paginación
                    axios.get(`${API_URL}/api/facturas/todas`),
                    // Facturas recibidas: GET /api/facturas-recibidas → List<FacturaRecibidaDTO>
                    axios.get(`${API_URL}/api/facturas-recibidas`),
                    // Gastos: GET /api/gastos → List<GastoDTO>
                    axios.get(`${API_URL}/api/gastos`),
                ]);

                setFacturas(
                    respF.status === 'fulfilled'
                        ? (Array.isArray(respF.value.data) ? respF.value.data : [])
                        : []
                );
                setFacturasRec(
                    respFR.status === 'fulfilled'
                        ? (Array.isArray(respFR.value.data) ? respFR.value.data : [])
                        : []
                );
                setGastos(
                    respG.status === 'fulfilled'
                        ? (Array.isArray(respG.value.data) ? respG.value.data : [])
                        : []
                );
            } catch (e) {
                mostrarMensaje('error', 'No se pudieron cargar los datos contables.');
            } finally {
                setCargando(false);
            }
        };
        cargar();
    }, []);

    // ── Filtrado por período ─────────────────────────────────────────────────
    // FacturaDTO.fecha → "dd/MM/yyyy" (por @JsonFormat)
    const facturasPeriodo = facturas.filter(f =>
        enTrimestre(f.fecha, trimestre, anio)
    );
    // FacturaRecibidaDTO.fechaFactura → "dd/MM/yyyy" (formateado en mapper)
    const facturasRecPeriodo = facturasRec.filter(f =>
        enTrimestre(f.fechaFactura, trimestre, anio)
    );
    // GastoDTO.fecha → "dd/MM/yyyy"
    const gastosPeriodo = gastos.filter(g =>
        enTrimestre(g.fecha, trimestre, anio)
    );

    // ── Modelo 303 — IVA ─────────────────────────────────────────────────────
    const repercutido = facturasPeriodo.reduce(
        (acc, f) => {
            const { base, cuota } = ivaFacturaEmitida(f);
            return { base: acc.base + base, cuota: acc.cuota + cuota };
        },
        { base: 0, cuota: 0 }
    );

    const soportadoFactRec = facturasRecPeriodo.reduce(
        (acc, f) => {
            const { base, cuota } = ivaFacturaRecibida(f);
            return { base: acc.base + base, cuota: acc.cuota + cuota };
        },
        { base: 0, cuota: 0 }
    );

    const soportadoGastos = gastosPeriodo.reduce(
        (acc, g) => {
            const { base, cuota } = ivaGasto(g);
            return { base: acc.base + base, cuota: acc.cuota + cuota };
        },
        { base: 0, cuota: 0 }
    );

    const soportado = {
        base: soportadoFactRec.base + soportadoGastos.base,
        cuota: soportadoFactRec.cuota + soportadoGastos.cuota,
    };

    const liquidacionIva = repercutido.cuota - soportado.cuota;

    // ── Modelo 130 — IRPF ────────────────────────────────────────────────────
    // Ingresos brutos = suma de total de facturas emitidas del período
    const ingresosBrutos = facturasPeriodo.reduce(
        (acc, f) => acc + parseFloat(f.total || 0), 0
    );
    // Gastos deducibles = total facturas recibidas + importe gastos del período
    const gastosDeducibles =
        facturasRecPeriodo.reduce((acc, f) => acc + parseFloat(f.total || 0), 0) +
        gastosPeriodo.reduce((acc, g) => acc + parseFloat(g.importe || 0), 0);

    const rendimientoNeto = Math.max(0, ingresosBrutos - gastosDeducibles);
    const pagoFraccionado130 = parseFloat((rendimientoNeto * 0.20).toFixed(2));

    // ── Descarga BOE ─────────────────────────────────────────────────────────
    const descargarBoe = async () => {
        setDescargando(true);
        try {
            const periodoStr = `${trimestre}T`;
            let url, nombreFichero;

            if (modelo === '303') {
                const params = new URLSearchParams({
                    nif: emisor?.nif || '',
                    nombre: emisor?.nombre || '',
                    baseRep21: repercutido.base.toFixed(2),
                    cuotaRep21: repercutido.cuota.toFixed(2),
                    baseSop21: soportado.base.toFixed(2),
                    cuotaSop21: soportado.cuota.toFixed(2),
                });
                url = `${API_URL}/api/fiscal/modelo303/${anio}/${periodoStr}/exportar-boe?${params}`;
                nombreFichero = `${NIF_NEGOCIO}${anio}${periodoStr}.303`;

            } else if (modelo === '130') {
                const params = new URLSearchParams({
                    nif: emisor?.nif || '',
                    nombre: emisor?.nombre || '',
                    ingresosTrimestre: ingresosBrutos.toFixed(2),
                    gastosTrimestre: gastosDeducibles.toFixed(2),
                    retencionesComputadas: '0',
                    pagosAnteriores: '0',
                });
                url = `${API_URL}/api/fiscal/modelo130/${anio}/${periodoStr}/exportar-boe?${params}`;
                nombreFichero = `${NIF_NEGOCIO}${anio}${periodoStr}.130`;

            } else {
                mostrarMensaje('error', 'El Modelo 111 aún no está disponible.');
                setDescargando(false);
                return;
            }

            const resp = await fetch(url, { credentials: 'include' });
            if (!resp.ok) {
                const txt = await resp.text();
                throw new Error(txt || `HTTP ${resp.status}`);
            }

            const blob = await resp.blob();
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = nombreFichero;
            link.click();
            URL.revokeObjectURL(link.href);

            mostrarMensaje('exito', `Fichero ${nombreFichero} descargado. Importalo en la sede electrónica de la AEAT.`);

        } catch (err) {
            console.error('Error al descargar BOE:', err);
            mostrarMensaje('error', `Error al generar el fichero: ${err.message}`);
        } finally {
            setDescargando(false);
        }
    };

    const mostrarMensaje = (tipo, texto) => {
        setMensaje({ tipo, texto });
        setTimeout(() => setMensaje(null), 7000);
    };

    // ─────────────────────────────────────────────────────────────────────────
    // Render
    // ─────────────────────────────────────────────────────────────────────────
    return (
        <div className="space-y-6">

            {/* Cabecera */}
            <div className="bg-white rounded-lg shadow p-6 flex items-center gap-3">
                <div className="w-12 h-12 flex items-center justify-center bg-blue-100 rounded-lg">
                    <FileText className="text-blue-600" size={28} />
                </div>
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Modelos Fiscales</h1>
                    <p className="text-gray-500">Genera y descarga los ficheros BOE para importar en la AEAT</p>
                </div>
            </div>

            {/* Mensaje de estado */}
            {mensaje && (
                <div className={`rounded-lg p-4 flex items-center gap-3 border ${mensaje.tipo === 'exito'
                        ? 'bg-green-50 border-green-200 text-green-800'
                        : 'bg-red-50 border-red-200 text-red-800'
                    }`}>
                    {mensaje.tipo === 'exito' ? <CheckCircle size={20} /> : <AlertCircle size={20} />}
                    <span>{mensaje.texto}</span>
                </div>
            )}

            {/* Filtros */}
            <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-lg font-semibold text-gray-700 mb-4">Período de declaración</h2>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Modelo</label>
                        <select
                            value={modelo}
                            onChange={e => { setModelo(e.target.value); setDetalleVisible(false); }}
                            className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                            <option value="303">Modelo 303 — IVA Autoliquidación</option>
                            <option value="130">Modelo 130 — IRPF Pago Fraccionado</option>
                            <option value="111" disabled>Modelo 111 — Retenciones (próximamente)</option>
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Trimestre</label>
                        <select
                            value={trimestre}
                            onChange={e => setTrimestre(Number(e.target.value))}
                            className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                            {TRIMESTRES.map(t => (
                                <option key={t.value} value={t.value}>{t.label}</option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Ejercicio</label>
                        <select
                            value={anio}
                            onChange={e => setAnio(Number(e.target.value))}
                            className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                            {Array.from({ length: 5 }, (_, i) => currentYear - 2 + i).map(y => (
                                <option key={y} value={y}>{y}</option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>

            {cargando ? (
                <div className="bg-white rounded-lg shadow p-12 flex justify-center">
                    <div className="flex flex-col items-center gap-3 text-gray-500">
                        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
                        <span>Cargando datos contables…</span>
                    </div>
                </div>
            ) : (
                <>
                    {/* ── Modelo 303 ── */}
                    {modelo === '303' && (
                        <div className="bg-white rounded-lg shadow p-6 space-y-4">
                            <div className="flex items-center justify-between">
                                <h2 className="text-lg font-semibold text-gray-700">
                                    Modelo 303 — IVA · {trimestre}T {anio}
                                </h2>
                                <span className="text-xs text-gray-400">
                                    {facturasPeriodo.length} fact. emitidas · {facturasRecPeriodo.length} recibidas · {gastosPeriodo.length} gastos
                                </span>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <KpiCard
                                    titulo="IVA Repercutido (ventas)"
                                    valor={fmt(repercutido.cuota)}
                                    subtitulo={`Base: ${fmt(repercutido.base)}`}
                                    color="blue"
                                />
                                <KpiCard
                                    titulo="IVA Soportado deducible (gastos)"
                                    valor={fmt(soportado.cuota)}
                                    subtitulo={`Base: ${fmt(soportado.base)}`}
                                    color="orange"
                                />
                                <KpiCard
                                    titulo="Liquidación"
                                    valor={fmt(liquidacionIva)}
                                    subtitulo={liquidacionIva >= 0 ? 'A ingresar en AEAT' : 'A compensar / devolver'}
                                    color={liquidacionIva >= 0 ? 'red' : 'green'}
                                />
                            </div>

                            <button
                                onClick={() => setDetalleVisible(v => !v)}
                                className="flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800"
                            >
                                {detalleVisible ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                                {detalleVisible ? 'Ocultar casillas' : 'Ver desglose por casillas'}
                            </button>

                            {detalleVisible && (
                                <div className="border rounded-lg overflow-hidden text-sm">
                                    <table className="w-full">
                                        <thead className="bg-gray-50">
                                            <tr>
                                                <th className="text-left px-4 py-2 text-gray-600 w-16">Casilla</th>
                                                <th className="text-left px-4 py-2 text-gray-600">Concepto</th>
                                                <th className="text-right px-4 py-2 text-gray-600">Importe</th>
                                            </tr>
                                        </thead>
                                        <tbody className="divide-y">
                                            <FilaCasilla c="07" desc="Base imponible IVA repercutido 21%" val={fmt(repercutido.base)} />
                                            <FilaCasilla c="09" desc="Cuota IVA repercutido 21%" val={fmt(repercutido.cuota)} />
                                            <FilaCasilla c="27" desc="Total IVA devengado" val={fmt(repercutido.cuota)} bold />
                                            <FilaCasilla c="28" desc="Base IVA soportado corrientes" val={fmt(soportado.base)} />
                                            <FilaCasilla c="29" desc="Cuota IVA soportado deducible" val={fmt(soportado.cuota)} />
                                            <FilaCasilla c="45" desc="Total IVA deducible" val={fmt(soportado.cuota)} bold />
                                            <FilaCasilla c="46/64" desc="Resultado régimen general (27 − 45)" val={fmt(liquidacionIva)} bold />
                                            <FilaCasilla c="70" desc="A ingresar (si positivo)" val={fmt(Math.max(0, liquidacionIva))} bold />
                                        </tbody>
                                    </table>
                                </div>
                            )}

                            <InfoBox texto={
                                `IVA soportado incluye facturas recibidas (${fmt(soportadoFactRec.cuota)}) ` +
                                `y gastos con IVA (${fmt(soportadoGastos.cuota)}).`
                            } />
                        </div>
                    )}

                    {/* ── Modelo 130 ── */}
                    {modelo === '130' && (
                        <div className="bg-white rounded-lg shadow p-6 space-y-4">
                            <div className="flex items-center justify-between">
                                <h2 className="text-lg font-semibold text-gray-700">
                                    Modelo 130 — IRPF · {trimestre}T {anio}
                                </h2>
                                <span className="text-xs text-gray-400">
                                    Estimación directa · 20% rendimiento neto
                                </span>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <KpiCard
                                    titulo="Ingresos del período (Cas. 01)"
                                    valor={fmt(ingresosBrutos)}
                                    subtitulo={`${facturasPeriodo.length} facturas emitidas`}
                                    color="blue"
                                />
                                <KpiCard
                                    titulo="Gastos deducibles (Cas. 02)"
                                    valor={fmt(gastosDeducibles)}
                                    subtitulo={`${facturasRecPeriodo.length} fact. recibidas + ${gastosPeriodo.length} gastos`}
                                    color="orange"
                                />
                                <KpiCard
                                    titulo="Pago a cuenta 20% (Cas. 14)"
                                    valor={fmt(pagoFraccionado130)}
                                    subtitulo={`Rend. neto: ${fmt(rendimientoNeto)}`}
                                    color={pagoFraccionado130 > 0 ? 'red' : 'green'}
                                />
                            </div>

                            <button
                                onClick={() => setDetalleVisible(v => !v)}
                                className="flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800"
                            >
                                {detalleVisible ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                                {detalleVisible ? 'Ocultar casillas' : 'Ver desglose por casillas'}
                            </button>

                            {detalleVisible && (
                                <div className="border rounded-lg overflow-hidden text-sm">
                                    <table className="w-full">
                                        <thead className="bg-gray-50">
                                            <tr>
                                                <th className="text-left px-4 py-2 text-gray-600 w-16">Casilla</th>
                                                <th className="text-left px-4 py-2 text-gray-600">Concepto</th>
                                                <th className="text-right px-4 py-2 text-gray-600">Importe</th>
                                            </tr>
                                        </thead>
                                        <tbody className="divide-y">
                                            <FilaCasilla c="01" desc="Ingresos computables del período" val={fmt(ingresosBrutos)} />
                                            <FilaCasilla c="02" desc="Gastos fiscalmente deducibles" val={fmt(gastosDeducibles)} />
                                            <FilaCasilla c="03" desc="Rendimiento neto (01 − 02)" val={fmt(ingresosBrutos - gastosDeducibles)} />
                                            <FilaCasilla c="05" desc="Base de cálculo (máx. de 03 y 0)" val={fmt(rendimientoNeto)} bold />
                                            <FilaCasilla c="07" desc="20% × casilla 05" val={fmt(pagoFraccionado130)} bold />
                                            <FilaCasilla c="11" desc="Retenciones e ingresos a cuenta" val={fmt(0)} />
                                            <FilaCasilla c="13" desc="Pagos fraccionados anteriores del año" val={fmt(0)} />
                                            <FilaCasilla c="14" desc="Resultado a ingresar (07 − 11 − 13)" val={fmt(pagoFraccionado130)} bold />
                                        </tbody>
                                    </table>
                                </div>
                            )}

                            <InfoBox texto="Las retenciones y pagos anteriores se asumen en 0. Si en trimestres posteriores ya has ingresado pagos, indícalos manualmente al importar en la AEAT." />
                        </div>
                    )}

                    {/* ── Botón de descarga ── */}
                    <div className="bg-white rounded-lg shadow p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                        <div>
                            <p className="font-semibold text-gray-800">Descargar fichero BOE</p>
                            <p className="text-sm text-gray-500 mt-0.5">
                                Formato oficial AEAT · nombre:{' '}
                                <span className="font-mono text-gray-700">
                                    {emisor?.nif || 'NIF'}{anio}{trimestre}T.{modelo}
                                </span>
                            </p>
                            <p className="text-xs text-gray-400 mt-1">
                                Importa el fichero en Renta Web / Sede Electrónica AEAT → "Importar declaración"
                            </p>
                        </div>
                        <button
                            onClick={descargarBoe}
                            disabled={descargando || cargando || !emisor}
                            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300
                                       text-white font-semibold px-6 py-3 rounded-lg transition-colors whitespace-nowrap"
                        >
                            {descargando ? (
                                <>
                                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white" />
                                    Generando fichero…
                                </>
                            ) : !emisor ? (
                                <>Cargando datos del emisor…</>
                            ) : (
                                <>
                                    <Download size={18} />
                                    Generar fichero .{modelo}
                                </>
                            )}
                        </button>
                    </div>
                </>
            )}
        </div>
    );
};

// ─────────────────────────────────────────────────────────────────────────────
// Sub-componentes
// ─────────────────────────────────────────────────────────────────────────────
const COLORES = {
    blue: { bg: 'bg-blue-50', text: 'text-blue-700', sub: 'text-blue-500' },
    orange: { bg: 'bg-orange-50', text: 'text-orange-700', sub: 'text-orange-500' },
    red: { bg: 'bg-red-50', text: 'text-red-700', sub: 'text-red-500' },
    green: { bg: 'bg-green-50', text: 'text-green-700', sub: 'text-green-500' },
};

const KpiCard = ({ titulo, valor, subtitulo, color = 'blue' }) => {
    const c = COLORES[color] || COLORES.blue;
    return (
        <div className={`${c.bg} rounded-lg p-4`}>
            <p className="text-sm text-gray-600 mb-1">{titulo}</p>
            <p className={`text-2xl font-bold ${c.text}`}>{valor}</p>
            {subtitulo && <p className={`text-xs mt-1 ${c.sub}`}>{subtitulo}</p>}
        </div>
    );
};

const FilaCasilla = ({ c, desc, val, bold = false }) => (
    <tr className="hover:bg-gray-50">
        <td className="px-4 py-2 font-mono text-gray-400 text-xs">{c}</td>
        <td className={`px-4 py-2 ${bold ? 'font-semibold text-gray-800' : 'text-gray-600'}`}>{desc}</td>
        <td className={`px-4 py-2 text-right tabular-nums ${bold ? 'font-semibold text-gray-800' : 'text-gray-600'}`}>{val}</td>
    </tr>
);

const InfoBox = ({ texto }) => (
    <div className="flex items-start gap-2 text-sm text-gray-500 bg-gray-50 rounded-lg p-3">
        <Info size={15} className="mt-0.5 shrink-0 text-gray-400" />
        <span>{texto}</span>
    </div>
);

export default ModelosFiscales;