import React, { useState, useEffect, useCallback } from 'react';
import {
    FileText, Download, AlertCircle, CheckCircle,
    ChevronDown, ChevronUp, Info, History,
    BadgeCheck, Clock, Trash2, RotateCcw
} from 'lucide-react';
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
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

function parsearFecha(str) {
    if (!str) return null;
    if (typeof str !== 'string') return null;
    if (/^\d{2}\/\d{2}\/\d{4}$/.test(str)) {
        const [d, m, y] = str.split('/');
        return new Date(Number(y), Number(m) - 1, Number(d));
    }
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

function ivaFacturaEmitida(f) {
    const base = parseFloat(f.baseImponible || 0);
    const cuota = parseFloat(f.importeIva || 0);
    if (!base && f.total) {
        const total = parseFloat(f.total);
        const tipo = parseFloat(f.tipoIva || 21);
        const b = total / (1 + tipo / 100);
        return { base: b, cuota: total - b };
    }
    return { base, cuota };
}

function ivaFacturaRecibida(f) {
    const base = parseFloat(f.baseImponible || 0);
    const cuota = parseFloat(f.cuotaIva || 0);
    if (!base && f.total) {
        const total = parseFloat(f.total);
        const tipo = parseFloat(f.tipoIva || 21);
        const b = total / (1 + tipo / 100);
        return { base: b, cuota: total - b };
    }
    return { base, cuota };
}

function ivaGasto(g) {
    if (g.baseImponible != null && g.cuotaIva != null) {
        return { base: parseFloat(g.baseImponible || 0), cuota: parseFloat(g.cuotaIva || 0) };
    }
    const importe = parseFloat(g.importe || 0);
    if (!g.ivaIncluido) return { base: importe, cuota: 0 };
    const b = importe / 1.21;
    return { base: b, cuota: importe - b };
}

// ─────────────────────────────────────────────────────────────────────────────
// Componente principal
// ─────────────────────────────────────────────────────────────────────────────
const ModelosFiscales = () => {
    // ── Filtros
    const [anio, setAnio] = useState(currentYear);
    const [trimestre, setTrimestre] = useState(1);
    const [modelo, setModelo] = useState('303');

    // ── Datos contables
    const [emisor, setEmisor] = useState(null);
    const [facturas, setFacturas] = useState([]);
    const [facturasRec, setFacturasRec] = useState([]);
    const [gastos, setGastos] = useState([]);

    // ── Historial
    const [historial, setHistorial] = useState([]);
    const [tabActiva, setTabActiva] = useState('declarar'); // 'declarar' | 'historial'
    const [filtroHistorial, setFiltroHistorial] = useState('TODOS'); // 'TODOS' | '303' | '130' | 'PENDIENTES'

    // ── UI
    const [cargando, setCargando] = useState(false);
    const [descargando, setDescargando] = useState(false);
    const [mensaje, setMensaje] = useState(null);
    const [detalleVisible, setDetalleVisible] = useState(false);
    const [confirmEliminar, setConfirmEliminar] = useState(null); // id a eliminar

    // ── Carga inicial ────────────────────────────────────────────────────────
    const cargarDatos = useCallback(async () => {
        setCargando(true);
        try {
            const [respEmisor, respF, respFR, respG, respH] = await Promise.allSettled([
                axios.get(`${API_URL}/facturas/emisor`),
                axios.get(`${API_URL}/facturas/todas`),
                axios.get(`${API_URL}/facturas-recibidas`),
                axios.get(`${API_URL}/gastos`),
                axios.get(`${API_URL}/fiscal/declaraciones`),
            ]);

            if (respEmisor.status === 'fulfilled') setEmisor(respEmisor.value.data);
            setFacturas(respF.status === 'fulfilled' && Array.isArray(respF.value.data) ? respF.value.data : []);
            setFacturasRec(respFR.status === 'fulfilled' && Array.isArray(respFR.value.data) ? respFR.value.data : []);
            setGastos(respG.status === 'fulfilled' && Array.isArray(respG.value.data) ? respG.value.data : []);
            setHistorial(respH.status === 'fulfilled' && Array.isArray(respH.value.data) ? respH.value.data : []);
        } catch {
            mostrarMensaje('error', 'No se pudieron cargar los datos contables.');
        } finally {
            setCargando(false);
        }
    }, []);

    useEffect(() => { cargarDatos(); }, [cargarDatos]);

    // ── Cálculos del período ─────────────────────────────────────────────────
    const facturasPeriodo = facturas.filter(f => enTrimestre(f.fecha, trimestre, anio));
    const facturasRecPeriodo = facturasRec.filter(f => enTrimestre(f.fechaFactura, trimestre, anio));
    const gastosPeriodo = gastos.filter(g => enTrimestre(g.fecha, trimestre, anio));

    const repercutido = facturasPeriodo.reduce(
        (acc, f) => { const { base, cuota } = ivaFacturaEmitida(f); return { base: acc.base + base, cuota: acc.cuota + cuota }; },
        { base: 0, cuota: 0 }
    );
    const soportadoFR = facturasRecPeriodo.reduce(
        (acc, f) => { const { base, cuota } = ivaFacturaRecibida(f); return { base: acc.base + base, cuota: acc.cuota + cuota }; },
        { base: 0, cuota: 0 }
    );
    const soportadoG = gastosPeriodo.reduce(
        (acc, g) => { const { base, cuota } = ivaGasto(g); return { base: acc.base + base, cuota: acc.cuota + cuota }; },
        { base: 0, cuota: 0 }
    );
    const soportado = { base: soportadoFR.base + soportadoG.base, cuota: soportadoFR.cuota + soportadoG.cuota };
    const liquidacionIva = repercutido.cuota - soportado.cuota;

    const ingresosBrutos = facturasPeriodo.reduce((acc, f) => acc + parseFloat(f.total || 0), 0);
    const gastosDeducibles = facturasRecPeriodo.reduce((acc, f) => acc + parseFloat(f.total || 0), 0)
        + gastosPeriodo.reduce((acc, g) => acc + parseFloat(g.importe || 0), 0);
    const rendimientoNeto = Math.max(0, ingresosBrutos - gastosDeducibles);
    const pagoFraccionado130 = parseFloat((rendimientoNeto * 0.20).toFixed(2));

    // ── Descarga BOE + registro automático ───────────────────────────────────
    const descargarBoe = async () => {
        if (!emisor) return;
        setDescargando(true);
        try {
            const periodoStr = `${trimestre}T`;
            let url, nombreFichero;

            if (modelo === '303') {
                const params = new URLSearchParams({
                    nif: emisor.nif, nombre: emisor.nombre,
                    baseRep21: repercutido.base.toFixed(2), cuotaRep21: repercutido.cuota.toFixed(2),
                    baseSop21: soportado.base.toFixed(2), cuotaSop21: soportado.cuota.toFixed(2),
                });
                url = `${API_URL}/fiscal/modelo303/${anio}/${periodoStr}/exportar-boe?${params}`;
                nombreFichero = `${emisor.nif}${anio}${periodoStr}.303`;

            } else if (modelo === '130') {
                const params = new URLSearchParams({
                    nif: emisor.nif, nombre: emisor.nombre,
                    ingresosTrimestre: ingresosBrutos.toFixed(2),
                    gastosTrimestre: gastosDeducibles.toFixed(2),
                    retencionesComputadas: '0',
                    pagosAnteriores: '0',
                });
                url = `${API_URL}/fiscal/modelo130/${anio}/${periodoStr}/exportar-boe?${params}`;
                nombreFichero = `${emisor.nif}${anio}${periodoStr}.130`;

            } else {
                mostrarMensaje('error', 'El Modelo 111 aún no está disponible.');
                setDescargando(false);
                return;
            }

            const resp = await fetch(url, { credentials: 'include' });
            if (!resp.ok) throw new Error(await resp.text() || `HTTP ${resp.status}`);

            const blob = await resp.blob();
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob); link.download = nombreFichero; link.click();
            URL.revokeObjectURL(link.href);

            mostrarMensaje('exito', `Fichero ${nombreFichero} descargado y registrado en el historial.`);
            // Recargar historial para mostrar el nuevo registro
            const respH = await axios.get(`${API_URL}/fiscal/declaraciones`);
            if (Array.isArray(respH.data)) setHistorial(respH.data);

        } catch (err) {
            mostrarMensaje('error', `Error al generar el fichero: ${err.message}`);
        } finally {
            setDescargando(false);
        }
    };

    // ── Acciones del historial ───────────────────────────────────────────────
    const marcarPresentado = async (id) => {
        try {
            await axios.put(`${API_URL}/fiscal/declaraciones/${id}/presentado`);
            setHistorial(h => h.map(d => d.id === id ? { ...d, estado: 'PRESENTADO', fechaPresentacion: new Date().toLocaleDateString('es-ES') } : d));
            mostrarMensaje('exito', 'Declaración marcada como presentada ante la AEAT.');
        } catch {
            mostrarMensaje('error', 'No se pudo actualizar el estado.');
        }
    };

    const revertirPresentacion = async (id) => {
        try {
            await axios.put(`${API_URL}/fiscal/declaraciones/${id}/revertir`);
            setHistorial(h => h.map(d => d.id === id ? { ...d, estado: 'GENERADO', fechaPresentacion: null } : d));
            mostrarMensaje('exito', 'Estado revertido a "Generado".');
        } catch {
            mostrarMensaje('error', 'No se pudo revertir el estado.');
        }
    };

    const eliminarDeclaracion = async (id) => {
        try {
            await axios.delete(`${API_URL}/fiscal/declaraciones/${id}`);
            setHistorial(h => h.filter(d => d.id !== id));
            setConfirmEliminar(null);
            mostrarMensaje('exito', 'Registro eliminado del historial.');
        } catch {
            mostrarMensaje('error', 'No se pudo eliminar el registro.');
        }
    };

    const mostrarMensaje = (tipo, texto) => {
        setMensaje({ tipo, texto });
        setTimeout(() => setMensaje(null), 7000);
    };

    // ── Historial filtrado ───────────────────────────────────────────────────
    const historialFiltrado = historial.filter(d => {
        if (filtroHistorial === 'TODOS') return true;
        if (filtroHistorial === 'PENDIENTES') return d.estado === 'GENERADO';
        return d.modelo === filtroHistorial;
    });

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
                    <p className="text-gray-500">Genera, descarga y controla tus declaraciones tributarias ante la AEAT</p>
                </div>
            </div>

            {/* Mensaje */}
            {mensaje && (
                <div className={`rounded-lg p-4 flex items-center gap-3 border ${mensaje.tipo === 'exito'
                        ? 'bg-green-50 border-green-200 text-green-800'
                        : 'bg-red-50 border-red-200 text-red-800'
                    }`}>
                    {mensaje.tipo === 'exito' ? <CheckCircle size={20} /> : <AlertCircle size={20} />}
                    <span>{mensaje.texto}</span>
                </div>
            )}

            {/* Tabs */}
            <div className="bg-white rounded-lg shadow">
                <div className="flex border-b">
                    {[
                        { id: 'declarar', label: 'Nueva declaración', icon: <FileText size={16} /> },
                        { id: 'historial', label: `Historial (${historial.length})`, icon: <History size={16} /> },
                    ].map(tab => (
                        <button
                            key={tab.id}
                            onClick={() => setTabActiva(tab.id)}
                            className={`flex items-center gap-2 px-6 py-4 text-sm font-medium border-b-2 transition-colors ${tabActiva === tab.id
                                    ? 'border-blue-600 text-blue-600'
                                    : 'border-transparent text-gray-500 hover:text-gray-700'
                                }`}
                        >
                            {tab.icon}{tab.label}
                        </button>
                    ))}
                </div>

                {/* ── Tab: Nueva declaración ── */}
                {tabActiva === 'declarar' && (
                    <div className="p-6 space-y-6">

                        {/* Filtros */}
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Modelo</label>
                                <select value={modelo} onChange={e => { setModelo(e.target.value); setDetalleVisible(false); }}
                                    className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                                    <option value="303">Modelo 303 — IVA Autoliquidación</option>
                                    <option value="130">Modelo 130 — IRPF Pago Fraccionado</option>
                                    <option value="111" disabled>Modelo 111 — Retenciones (próximamente)</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Trimestre</label>
                                <select value={trimestre} onChange={e => setTrimestre(Number(e.target.value))}
                                    className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                                    {TRIMESTRES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Ejercicio</label>
                                <select value={anio} onChange={e => setAnio(Number(e.target.value))}
                                    className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                                    {Array.from({ length: 5 }, (_, i) => currentYear - 2 + i).map(y =>
                                        <option key={y} value={y}>{y}</option>)}
                                </select>
                            </div>
                        </div>

                        {cargando ? (
                            <div className="flex justify-center py-8">
                                <div className="flex flex-col items-center gap-3 text-gray-500">
                                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
                                    <span className="text-sm">Cargando datos contables…</span>
                                </div>
                            </div>
                        ) : (
                            <>
                                {/* ── Modelo 303 ── */}
                                {modelo === '303' && (
                                    <div className="space-y-4">
                                        <div className="flex items-center justify-between">
                                            <h3 className="font-semibold text-gray-700">Modelo 303 — IVA · {trimestre}T {anio}</h3>
                                            <span className="text-xs text-gray-400">{facturasPeriodo.length} fact. emitidas · {facturasRecPeriodo.length} recibidas · {gastosPeriodo.length} gastos</span>
                                        </div>
                                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                            <KpiCard titulo="IVA Repercutido" valor={fmt(repercutido.cuota)} subtitulo={`Base: ${fmt(repercutido.base)}`} color="blue" />
                                            <KpiCard titulo="IVA Soportado deducible" valor={fmt(soportado.cuota)} subtitulo={`Base: ${fmt(soportado.base)}`} color="orange" />
                                            <KpiCard titulo="Liquidación" valor={fmt(liquidacionIva)}
                                                subtitulo={liquidacionIva >= 0 ? 'A ingresar en AEAT' : 'A compensar'}
                                                color={liquidacionIva >= 0 ? 'red' : 'green'} />
                                        </div>
                                        <DetalleCasillas visible={detalleVisible} onToggle={() => setDetalleVisible(v => !v)}>
                                            <FilaCasilla c="07" desc="Base IVA repercutido 21%" val={fmt(repercutido.base)} />
                                            <FilaCasilla c="09" desc="Cuota IVA repercutido 21%" val={fmt(repercutido.cuota)} />
                                            <FilaCasilla c="27" desc="Total IVA devengado" val={fmt(repercutido.cuota)} bold />
                                            <FilaCasilla c="28" desc="Base IVA soportado corrientes" val={fmt(soportado.base)} />
                                            <FilaCasilla c="29" desc="Cuota IVA soportado deducible" val={fmt(soportado.cuota)} />
                                            <FilaCasilla c="45" desc="Total IVA deducible" val={fmt(soportado.cuota)} bold />
                                            <FilaCasilla c="46/64" desc="Resultado (27 − 45)" val={fmt(liquidacionIva)} bold />
                                            <FilaCasilla c="70" desc="A ingresar" val={fmt(Math.max(0, liquidacionIva))} bold />
                                        </DetalleCasillas>
                                        <InfoBox texto={`IVA soportado: facturas recibidas ${fmt(soportadoFR.cuota)} + gastos ${fmt(soportadoG.cuota)}.`} />
                                    </div>
                                )}

                                {/* ── Modelo 130 ── */}
                                {modelo === '130' && (
                                    <div className="space-y-4">
                                        <div className="flex items-center justify-between">
                                            <h3 className="font-semibold text-gray-700">Modelo 130 — IRPF · {trimestre}T {anio}</h3>
                                            <span className="text-xs text-gray-400">Estimación directa · 20% rendimiento neto</span>
                                        </div>
                                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                            <KpiCard titulo="Ingresos (Cas. 01)" valor={fmt(ingresosBrutos)} subtitulo={`${facturasPeriodo.length} facturas emitidas`} color="blue" />
                                            <KpiCard titulo="Gastos deducibles (Cas. 02)" valor={fmt(gastosDeducibles)} subtitulo={`${facturasRecPeriodo.length} fact. + ${gastosPeriodo.length} gastos`} color="orange" />
                                            <KpiCard titulo="Pago fraccionado 20% (Cas. 14)" valor={fmt(pagoFraccionado130)} subtitulo={`Rend. neto: ${fmt(rendimientoNeto)}`} color={pagoFraccionado130 > 0 ? 'red' : 'green'} />
                                        </div>
                                        <DetalleCasillas visible={detalleVisible} onToggle={() => setDetalleVisible(v => !v)}>
                                            <FilaCasilla c="01" desc="Ingresos computables" val={fmt(ingresosBrutos)} />
                                            <FilaCasilla c="02" desc="Gastos fiscalmente deducibles" val={fmt(gastosDeducibles)} />
                                            <FilaCasilla c="03" desc="Rendimiento neto (01 − 02)" val={fmt(ingresosBrutos - gastosDeducibles)} />
                                            <FilaCasilla c="05" desc="Base de cálculo (máx. 0)" val={fmt(rendimientoNeto)} bold />
                                            <FilaCasilla c="07" desc="20% × casilla 05" val={fmt(pagoFraccionado130)} bold />
                                            <FilaCasilla c="11" desc="Retenciones soportadas" val={fmt(0)} />
                                            <FilaCasilla c="13" desc="Pagos fraccionados anteriores" val={fmt(0)} />
                                            <FilaCasilla c="14" desc="Resultado a ingresar" val={fmt(pagoFraccionado130)} bold />
                                        </DetalleCasillas>
                                        <InfoBox texto="Retenciones y pagos anteriores en 0. Si corresponde ajustarlos, hazlo al importar el fichero en la AEAT." />
                                    </div>
                                )}

                                {/* Botón descarga */}
                                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pt-2 border-t">
                                    <div>
                                        <p className="font-semibold text-gray-800">Descargar fichero BOE</p>
                                        <p className="text-sm text-gray-500 mt-0.5">
                                            Fichero: <span className="font-mono text-gray-700">{emisor?.nif || '…'}{anio}{trimestre}T.{modelo}</span>
                                        </p>
                                        <p className="text-xs text-gray-400 mt-0.5">La descarga queda registrada automáticamente en el historial.</p>
                                    </div>
                                    <button onClick={descargarBoe} disabled={descargando || cargando || !emisor}
                                        className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300
                                                   text-white font-semibold px-6 py-3 rounded-lg transition-colors whitespace-nowrap">
                                        {descargando
                                            ? <><div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white" />Generando…</>
                                            : !emisor
                                                ? <>Cargando datos del emisor…</>
                                                : <><Download size={18} />Generar fichero .{modelo}</>}
                                    </button>
                                </div>
                            </>
                        )}
                    </div>
                )}

                {/* ── Tab: Historial ── */}
                {tabActiva === 'historial' && (
                    <div className="p-6 space-y-4">

                        {/* Filtros historial */}
                        <div className="flex flex-wrap gap-2">
                            {[
                                { id: 'TODOS', label: 'Todos' },
                                { id: '303', label: 'Modelo 303' },
                                { id: '130', label: 'Modelo 130' },
                                { id: 'PENDIENTES', label: 'Pendientes' },
                            ].map(f => (
                                <button key={f.id} onClick={() => setFiltroHistorial(f.id)}
                                    className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${filtroHistorial === f.id
                                            ? 'bg-blue-600 text-white'
                                            : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                                        }`}>
                                    {f.label}
                                </button>
                            ))}
                        </div>

                        {historialFiltrado.length === 0 ? (
                            <div className="text-center py-12 text-gray-400">
                                <History size={40} className="mx-auto mb-3 opacity-40" />
                                <p className="font-medium">No hay declaraciones registradas</p>
                                <p className="text-sm mt-1">Genera tu primer fichero BOE para que aparezca aquí.</p>
                            </div>
                        ) : (
                            <div className="space-y-3">
                                {historialFiltrado.map(d => (
                                    <FilaHistorial
                                        key={d.id}
                                        d={d}
                                        onPresentado={() => marcarPresentado(d.id)}
                                        onRevertir={() => revertirPresentacion(d.id)}
                                        onEliminar={() => setConfirmEliminar(d.id)}
                                    />
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* Modal confirmar eliminación */}
            {confirmEliminar && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
                    <div className="bg-white rounded-xl shadow-xl p-6 max-w-sm w-full mx-4">
                        <h3 className="font-semibold text-gray-900 mb-2">¿Eliminar registro?</h3>
                        <p className="text-sm text-gray-500 mb-6">
                            Se borrará este registro del historial. El fichero BOE ya descargado no se verá afectado.
                        </p>
                        <div className="flex gap-3 justify-end">
                            <button onClick={() => setConfirmEliminar(null)}
                                className="px-4 py-2 text-sm rounded-lg border text-gray-600 hover:bg-gray-50">
                                Cancelar
                            </button>
                            <button onClick={() => eliminarDeclaracion(confirmEliminar)}
                                className="px-4 py-2 text-sm rounded-lg bg-red-600 text-white hover:bg-red-700">
                                Eliminar
                            </button>
                        </div>
                    </div>
                </div>
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

const DetalleCasillas = ({ visible, onToggle, children }) => (
    <div>
        <button onClick={onToggle} className="flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800">
            {visible ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
            {visible ? 'Ocultar casillas' : 'Ver desglose por casillas'}
        </button>
        {visible && (
            <div className="mt-3 border rounded-lg overflow-hidden text-sm">
                <table className="w-full">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="text-left px-4 py-2 text-gray-600 w-16">Cas.</th>
                            <th className="text-left px-4 py-2 text-gray-600">Concepto</th>
                            <th className="text-right px-4 py-2 text-gray-600">Importe</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y">{children}</tbody>
                </table>
            </div>
        )}
    </div>
);

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

const FilaHistorial = ({ d, onPresentado, onRevertir, onEliminar }) => {
    const presentado = d.estado === 'PRESENTADO';

    return (
        <div className={`border rounded-lg p-4 ${presentado ? 'border-green-200 bg-green-50/30' : 'border-gray-200 bg-white'}`}>
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">

                {/* Identificación */}
                <div className="flex items-center gap-3">
                    <div className={`w-10 h-10 rounded-lg flex items-center justify-center font-bold text-sm
                        ${d.modelo === '303' ? 'bg-blue-100 text-blue-700' : 'bg-purple-100 text-purple-700'}`}>
                        {d.modelo}
                    </div>
                    <div>
                        <p className="font-semibold text-gray-800">
                            Modelo {d.modelo} · {d.periodo} {d.ejercicio}
                        </p>
                        <p className="text-xs text-gray-400">
                            Generado: {d.fechaGeneracion}
                            {d.nombreFichero && <> · <span className="font-mono">{d.nombreFichero}</span></>}
                        </p>
                    </div>
                </div>

                {/* Estado */}
                <div className="flex items-center gap-2">
                    {presentado ? (
                        <span className="flex items-center gap-1 px-3 py-1 rounded-full bg-green-100 text-green-700 text-xs font-medium">
                            <BadgeCheck size={13} /> Presentado {d.fechaPresentacion && `· ${d.fechaPresentacion}`}
                        </span>
                    ) : (
                        <span className="flex items-center gap-1 px-3 py-1 rounded-full bg-amber-100 text-amber-700 text-xs font-medium">
                            <Clock size={13} /> Pendiente de presentar
                        </span>
                    )}
                </div>
            </div>

            {/* Importes snapshot */}
            <div className="mt-3 pt-3 border-t border-gray-100 grid grid-cols-2 sm:grid-cols-4 gap-3 text-sm">
                {d.modelo === '303' ? (
                    <>
                        <ImporteSnap label="IVA repercutido" valor={fmt(d.cuotaRepercutida)} />
                        <ImporteSnap label="IVA soportado" valor={fmt(d.cuotaSoportada)} />
                        <ImporteSnap label="Resultado IVA" valor={fmt(d.resultadoIva)}
                            color={parseFloat(d.resultadoIva) >= 0 ? 'red' : 'green'} />
                        <ImporteSnap label="Base repercutida" valor={fmt(d.baseRepercutida)} />
                    </>
                ) : (
                    <>
                        <ImporteSnap label="Ingresos" valor={fmt(d.ingresosTrimestre)} />
                        <ImporteSnap label="Gastos" valor={fmt(d.gastosTrimestre)} />
                        <ImporteSnap label="Rend. neto" valor={fmt(d.rendimientoNeto)} />
                        <ImporteSnap label="Pago fraccionado" valor={fmt(d.pagoFraccionado)} color="red" />
                    </>
                )}
            </div>

            {/* Acciones */}
            <div className="mt-3 flex gap-2 justify-end">
                {!presentado ? (
                    <button onClick={onPresentado}
                        className="flex items-center gap-1 text-xs px-3 py-1.5 rounded-lg bg-green-600 text-white hover:bg-green-700 transition-colors">
                        <BadgeCheck size={13} /> Marcar como presentado
                    </button>
                ) : (
                    <button onClick={onRevertir}
                        className="flex items-center gap-1 text-xs px-3 py-1.5 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 transition-colors">
                        <RotateCcw size={13} /> Revertir
                    </button>
                )}
                <button onClick={onEliminar}
                    className="flex items-center gap-1 text-xs px-3 py-1.5 rounded-lg border border-red-200 text-red-600 hover:bg-red-50 transition-colors">
                    <Trash2 size={13} /> Eliminar
                </button>
            </div>
        </div>
    );
};

const ImporteSnap = ({ label, valor, color }) => (
    <div>
        <p className="text-xs text-gray-400">{label}</p>
        <p className={`font-semibold tabular-nums ${color === 'red' ? 'text-red-600' : color === 'green' ? 'text-green-600' : 'text-gray-800'
            }`}>{valor}</p>
    </div>
);

export default ModelosFiscales;