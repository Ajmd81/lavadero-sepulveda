import { useState, useCallback } from "react";
import {
  TrendingUp, TrendingDown, Minus, FileDown, Printer,
  ChevronDown, ChevronRight, Calendar, BarChart3, PieChart,
  BookOpen, ArrowUpRight, ArrowDownRight
} from "lucide-react";
import {
  PieChart as RechartsPie, Pie, Cell, Tooltip, Legend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer
} from "recharts";

// ─────────────────────────────────────────────
// DATOS MOCK — reemplazar por llamadas a la API
// ─────────────────────────────────────────────
const MOCK_DATA = {
  ejercicio: 2025,
  fechaDesde: "01/01/2025",
  fechaHasta: "19/02/2025",
  numFacturasEmitidas: 142,
  numFacturasRecibidas: 38,
  numGastos: 17,
  totalIngresos: 28450.0,
  totalGastos: 19830.5,
  ventasNetas: 27900.0,
  otrosIngresosExplotacion: 550.0,
  aprovisionamientos: 4200.0,
  gastosPersonal: 8500.0,
  gastosPersonalDetalle: { sueldos: 7000.0, seguridadSocial: 1500.0 },
  otrosGastosExplotacion: 5130.5,
  serviciosExteriores: 2800.0,
  tributos: 630.5,
  otrosGastosGestion: 1700.0,
  amortizacion: 2000.0,
  resultadoExplotacion: 8619.5,
  ingresosFinancieros: 120.0,
  gastosFinancieros: 89.0,
  resultadoFinanciero: 31.0,
  resultadoAntesImpuestos: 8650.5,
  impuestoBeneficios: 2162.62,
  resultadoEjercicio: 6487.88,
  margenBeneficio: 22.8,
  gastosPorCategoria: [
    { categoria: "PERSONAL", label: "Personal", importe: 7000.0 },
    { categoria: "SEGURIDAD_SOCIAL", label: "Seguridad Social", importe: 1500.0 },
    { categoria: "ALQUILER", label: "Alquiler", importe: 1800.0 },
    { categoria: "PRODUCTOS", label: "Productos limpieza", importe: 1200.0 },
    { categoria: "LUZ", label: "Electricidad", importe: 890.5 },
    { categoria: "AGUA", label: "Agua", importe: 420.0 },
    { categoria: "GESTORIA", label: "Gestoría", importe: 400.0 },
    { categoria: "TELEFONIA", label: "Telefonía/Internet", importe: 180.0 },
    { categoria: "PUBLICIDAD", label: "Publicidad", importe: 280.0 },
    { categoria: "MANTENIMIENTO", label: "Mantenimiento", importe: 660.0 },
    { categoria: "BANCARIOS", label: "Gastos bancarios", importe: 89.0 },
    { categoria: "SEGUROS", label: "Seguros", importe: 311.0 },
    { categoria: "OTROS", label: "Otros gastos", importe: 100.0 },
  ],
};

const PIE_COLORS = [
  "#1565C0", "#2E7D32", "#E65100", "#6A1B9A",
  "#00838F", "#AD1457", "#37474F"
];

// ─────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────
const fmt = (n) =>
  new Intl.NumberFormat("es-ES", { style: "currency", currency: "EUR" }).format(n ?? 0);

const fmtGasto = (n) => {
  if (!n || n === 0) return "0,00 €";
  return `(${fmt(n)})`;
};

// ─────────────────────────────────────────────
// COMPONENTES AUXILIARES
// ─────────────────────────────────────────────
function KpiCard({ title, value, sub, color, icon }) {
  const colors = {
    green: { bg: "#E8F5E9", border: "#4CAF50", text: "#2E7D32", iconBg: "#C8E6C9" },
    red:   { bg: "#FFEBEE", border: "#EF5350", text: "#C62828", iconBg: "#FFCDD2" },
    blue:  { bg: "#E3F2FD", border: "#1E88E5", text: "#1565C0", iconBg: "#BBDEFB" },
    grey:  { bg: "#F5F5F5", border: "#9E9E9E", text: "#424242", iconBg: "#E0E0E0" },
  };
  const c = colors[color] || colors.blue;
  const IconComp = icon;
  return (
    <div style={{
      background: c.bg, border: `2px solid ${c.border}`,
      borderRadius: 12, padding: "20px 24px",
      display: "flex", alignItems: "center", gap: 16, flex: 1, minWidth: 180
    }}>
      <div style={{
        background: c.iconBg, borderRadius: 10,
        width: 48, height: 48, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0
      }}>
        <IconComp size={22} color={c.text} />
      </div>
      <div>
        <div style={{ fontSize: 11, fontWeight: 600, color: c.text, textTransform: "uppercase", letterSpacing: 1, marginBottom: 4 }}>{title}</div>
        <div style={{ fontSize: 22, fontWeight: 800, color: c.text }}>{value}</div>
        {sub && <div style={{ fontSize: 11, color: c.text, opacity: 0.75, marginTop: 2 }}>{sub}</div>}
      </div>
    </div>
  );
}

function PyGRow({ label, value, indent = 0, bold = false, isTotal = false, colored = false, isGasto = false }) {
  const numVal = value ?? 0;
  const color = colored
    ? numVal > 0 ? "#2E7D32" : numVal < 0 ? "#C62828" : "#1565C0"
    : "inherit";

  return (
    <div style={{
      display: "flex", justifyContent: "space-between", alignItems: "center",
      padding: `${isTotal ? 10 : 6}px ${isTotal ? 16 : 8}px`,
      paddingLeft: 8 + indent * 20,
      background: isTotal ? "#EFF3FA" : "transparent",
      borderRadius: isTotal ? 6 : 0,
      borderBottom: isTotal ? "none" : "1px solid #F0F0F0",
      marginBottom: isTotal ? 6 : 0,
    }}>
      <span style={{ fontSize: isTotal ? 13 : 12, fontWeight: bold || isTotal ? 700 : 400, color: "#2C3E50" }}>
        {label}
      </span>
      <span style={{
        fontSize: isTotal ? 13 : 12, fontWeight: bold || isTotal ? 700 : 400,
        color, fontFamily: "monospace", minWidth: 110, textAlign: "right"
      }}>
        {isGasto ? fmtGasto(numVal) : fmt(numVal)}
      </span>
    </div>
  );
}

function Section({ title, children, defaultOpen = true }) {
  const [open, setOpen] = useState(defaultOpen);
  return (
    <div style={{ marginBottom: 8 }}>
      <button
        onClick={() => setOpen(o => !o)}
        style={{
          display: "flex", alignItems: "center", gap: 8, width: "100%",
          background: "#1565C0", color: "#fff", border: "none",
          borderRadius: open ? "8px 8px 0 0" : 8, padding: "10px 16px",
          cursor: "pointer", fontSize: 12, fontWeight: 700, letterSpacing: 0.5,
          textTransform: "uppercase"
        }}
      >
        {open ? <ChevronDown size={15} /> : <ChevronRight size={15} />}
        {title}
      </button>
      {open && (
        <div style={{
          background: "#fff", border: "1px solid #DBEAFE",
          borderTop: "none", borderRadius: "0 0 8px 8px", padding: "8px 12px"
        }}>
          {children}
        </div>
      )}
    </div>
  );
}

// ─────────────────────────────────────────────
// COMPONENTE PRINCIPAL
// ─────────────────────────────────────────────
export default function LibrosContables() {
  const today = new Date();
  const [desde, setDesde] = useState(`${today.getFullYear()}-01-01`);
  const [hasta, setHasta] = useState(today.toISOString().split("T")[0]);
  const [activeTab, setActiveTab] = useState("pyg"); // pyg | grafico
  const [data, setData] = useState(MOCK_DATA);
  const [loading, setLoading] = useState(false);

  // Quick filters
  const setQuickFilter = (type) => {
    const y = today.getFullYear();
    const quarters = { T1: ["01-01","03-31"], T2: ["04-01","06-30"], T3: ["07-01","09-30"], T4: ["10-01","12-31"] };
    if (type === "mes") {
      setDesde(`${y}-${String(today.getMonth()+1).padStart(2,"0")}-01`);
      setHasta(today.toISOString().split("T")[0]);
    } else if (quarters[type]) {
      setDesde(`${y}-${quarters[type][0]}`);
      setHasta(`${y}-${quarters[type][1]}`);
    } else if (type === "anio") {
      setDesde(`${y}-01-01`);
      setHasta(`${y}-12-31`);
    }
  };

  const generarInforme = useCallback(() => {
    setLoading(true);
    // TODO: llamar a la API → GET /api/contabilidad/pyg?desde={desde}&hasta={hasta}
    setTimeout(() => { setData(MOCK_DATA); setLoading(false); }, 600);
  }, []);

  const resultado = data.resultadoEjercicio;
  const resultadoColor = resultado > 0 ? "green" : resultado < 0 ? "red" : "blue";
  const ResultIcon = resultado > 0 ? TrendingUp : resultado < 0 ? TrendingDown : Minus;

  // Top 6 para pie chart
  const topGastos = [...data.gastosPorCategoria].sort((a,b) => b.importe - a.importe).slice(0,6);
  const otrosImporte = data.gastosPorCategoria.slice(6).reduce((s,g) => s + g.importe, 0);
  const pieData = otrosImporte > 0
    ? [...topGastos, { label: "Otros", importe: otrosImporte }]
    : topGastos;

  const barData = data.gastosPorCategoria
    .sort((a,b) => b.importe - a.importe)
    .slice(0, 10)
    .map(g => ({ name: g.label.split("/")[0], importe: g.importe }));

  return (
    <div style={{ fontFamily: "'Segoe UI', system-ui, sans-serif", background: "#F0F4F8", minHeight: "100vh" }}>
      {/* ── HEADER ── */}
      <div style={{
        background: "linear-gradient(135deg, #0D47A1 0%, #1565C0 50%, #1976D2 100%)",
        padding: "24px 32px 20px", color: "#fff"
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 4 }}>
          <BookOpen size={26} />
          <h1 style={{ margin: 0, fontSize: 22, fontWeight: 800, letterSpacing: -0.5 }}>
            Libros Contables
          </h1>
        </div>
        <p style={{ margin: 0, fontSize: 13, opacity: 0.8 }}>
          Lavadero Sepúlveda · Ejercicio {data.ejercicio}
        </p>
      </div>

      <div style={{ padding: "24px 32px", maxWidth: 1200, margin: "0 auto" }}>

        {/* ── TABS ── */}
        <div style={{ display: "flex", gap: 4, marginBottom: 20 }}>
          {[
            { id: "pyg", label: "Pérdidas y Ganancias", icon: BookOpen },
            { id: "grafico", label: "Análisis Gráfico", icon: BarChart3 },
          ].map(({ id, label, icon }) => {
            const TabIcon = icon;
            return (
            <button key={id} onClick={() => setActiveTab(id)} style={{
              display: "flex", alignItems: "center", gap: 7,
              padding: "9px 18px", borderRadius: 8, border: "none",
              background: activeTab === id ? "#1565C0" : "#fff",
              color: activeTab === id ? "#fff" : "#555",
              fontWeight: 600, fontSize: 13, cursor: "pointer",
              boxShadow: activeTab === id ? "0 2px 8px rgba(21,101,192,0.3)" : "0 1px 3px rgba(0,0,0,0.1)",
              transition: "all 0.15s"
            }}>
              <TabIcon size={15} />{label}
            </button>
            );
          })}
        </div>

        {/* ── FILTROS ── */}
        <div style={{
          background: "#fff", borderRadius: 12, padding: "16px 20px",
          boxShadow: "0 1px 4px rgba(0,0,0,0.08)", marginBottom: 20,
          display: "flex", flexWrap: "wrap", gap: 12, alignItems: "flex-end"
        }}>
          <div style={{ display: "flex", gap: 8, flexWrap: "wrap", alignItems: "center" }}>
            <Calendar size={16} color="#1565C0" />
            <span style={{ fontSize: 12, fontWeight: 600, color: "#555" }}>Período:</span>
            <input type="date" value={desde} onChange={e => setDesde(e.target.value)}
              style={{ padding: "6px 10px", borderRadius: 6, border: "1px solid #CBD5E0", fontSize: 13, color: "#2C3E50" }} />
            <span style={{ fontSize: 12, color: "#888" }}>—</span>
            <input type="date" value={hasta} onChange={e => setHasta(e.target.value)}
              style={{ padding: "6px 10px", borderRadius: 6, border: "1px solid #CBD5E0", fontSize: 13, color: "#2C3E50" }} />
            <button onClick={generarInforme} disabled={loading} style={{
              padding: "7px 16px", borderRadius: 6, border: "none",
              background: "#1565C0", color: "#fff", fontWeight: 700, fontSize: 12,
              cursor: "pointer", opacity: loading ? 0.7 : 1
            }}>
              {loading ? "Cargando…" : "Generar"}
            </button>
          </div>

          <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
            {["mes","T1","T2","T3","T4","anio"].map(f => (
              <button key={f} onClick={() => setQuickFilter(f)} style={{
                padding: "5px 11px", borderRadius: 6, fontSize: 11, fontWeight: 600,
                border: "1px solid #CBD5E0", background: "#F7FAFC", cursor: "pointer",
                color: "#555", transition: "all 0.15s"
              }}>
                {f === "mes" ? "Este mes" : f === "anio" ? "Año" : f}
              </button>
            ))}
          </div>

          <div style={{ marginLeft: "auto", display: "flex", gap: 8 }}>
            <button onClick={() => alert("Exportar a Excel: conectar con API")} style={{
              display: "flex", alignItems: "center", gap: 6,
              padding: "7px 14px", borderRadius: 6, border: "1px solid #2E7D32",
              background: "#E8F5E9", color: "#2E7D32", fontWeight: 600, fontSize: 12, cursor: "pointer"
            }}>
              <FileDown size={14} />Excel
            </button>
            <button onClick={() => window.print()} style={{
              display: "flex", alignItems: "center", gap: 6,
              padding: "7px 14px", borderRadius: 6, border: "1px solid #1565C0",
              background: "#E3F2FD", color: "#1565C0", fontWeight: 600, fontSize: 12, cursor: "pointer"
            }}>
              <Printer size={14} />Imprimir
            </button>
          </div>
        </div>

        {/* ── KPIs ── */}
        <div style={{ display: "flex", gap: 12, marginBottom: 20, flexWrap: "wrap" }}>
          <KpiCard title="Total Ingresos" value={fmt(data.totalIngresos)}
            sub={`${data.numFacturasEmitidas} facturas emitidas`} color="green" icon={ArrowUpRight} />
          <KpiCard title="Total Gastos" value={fmt(data.totalGastos)}
            sub={`${data.numFacturasRecibidas} facturas + ${data.numGastos} gastos`} color="grey" icon={ArrowDownRight} />
          <KpiCard
            title={resultado > 0 ? "Beneficio" : resultado < 0 ? "Pérdida" : "Resultado"}
            value={fmt(Math.abs(resultado))}
            sub={`Margen: ${data.margenBeneficio}%`}
            color={resultadoColor}
            icon={ResultIcon}
          />
        </div>

        {/* ═══════════════════════════════════════ */}
        {/* TAB: Pérdidas y Ganancias               */}
        {/* ═══════════════════════════════════════ */}
        {activeTab === "pyg" && (
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>

            {/* COLUMNA IZQUIERDA */}
            <div>
              {/* INGRESOS */}
              <Section title="A) Ingresos de Explotación">
                <PyGRow label="1. Importe neto cifra de negocios" value={data.ventasNetas} bold />
                <PyGRow label="Servicios de lavado" value={data.ventasNetas} indent={1} />
                <PyGRow label="5. Otros ingresos de explotación" value={data.otrosIngresosExplotacion} />
                <div style={{ height: 8 }} />
                <PyGRow label="TOTAL INGRESOS DE EXPLOTACIÓN" value={data.totalIngresos} isTotal colored />
              </Section>

              {/* GASTOS */}
              <Section title="B) Gastos de Explotación">
                <PyGRow label="4. Aprovisionamientos" value={data.aprovisionamientos} isGasto />
                <PyGRow label="6. Gastos de personal" value={data.gastosPersonal} isGasto bold />
                <PyGRow label="Sueldos y salarios" value={data.gastosPersonalDetalle.sueldos} indent={1} isGasto />
                <PyGRow label="Cargas sociales" value={data.gastosPersonalDetalle.seguridadSocial} indent={1} isGasto />
                <PyGRow label="7. Otros gastos de explotación" value={data.otrosGastosExplotacion} isGasto bold />
                <PyGRow label="Servicios exteriores" value={data.serviciosExteriores} indent={1} isGasto />
                <PyGRow label="Tributos" value={data.tributos} indent={1} isGasto />
                <PyGRow label="Otros gastos de gestión" value={data.otrosGastosGestion} indent={1} isGasto />
                <PyGRow label="8. Amortización del inmovilizado" value={data.amortizacion} isGasto />
                <div style={{ height: 8 }} />
                <PyGRow label="TOTAL GASTOS DE EXPLOTACIÓN" value={data.totalGastos} isTotal colored />
              </Section>

              {/* DESGLOSE POR CATEGORÍA */}
              <Section title="Desglose Gastos por Categoría" defaultOpen={false}>
                <div style={{ overflowX: "auto" }}>
                  <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 12 }}>
                    <thead>
                      <tr style={{ background: "#EFF3FA" }}>
                        <th style={{ textAlign: "left", padding: "8px 10px", fontWeight: 700, color: "#1565C0" }}>Categoría</th>
                        <th style={{ textAlign: "right", padding: "8px 10px", fontWeight: 700, color: "#1565C0" }}>Importe</th>
                        <th style={{ textAlign: "right", padding: "8px 10px", fontWeight: 700, color: "#1565C0" }}>% s/Total</th>
                      </tr>
                    </thead>
                    <tbody>
                      {[...data.gastosPorCategoria]
                        .sort((a, b) => b.importe - a.importe)
                        .map((g, i) => (
                          <tr key={g.categoria} style={{ background: i % 2 === 0 ? "#fff" : "#F7FAFC" }}>
                            <td style={{ padding: "7px 10px", color: "#2C3E50" }}>{g.label}</td>
                            <td style={{ padding: "7px 10px", textAlign: "right", fontFamily: "monospace", color: "#C62828" }}>
                              {fmtGasto(g.importe)}
                            </td>
                            <td style={{ padding: "7px 10px", textAlign: "right", color: "#666" }}>
                              {((g.importe / data.totalGastos) * 100).toFixed(1)}%
                            </td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              </Section>
            </div>

            {/* COLUMNA DERECHA */}
            <div>
              {/* RESULTADOS */}
              <Section title="Cuenta de Resultados">
                {/* Resultado explotación */}
                <PyGRow
                  label="A.1) Resultado de Explotación"
                  value={data.resultadoExplotacion}
                  isTotal colored
                />

                {/* Resultado financiero */}
                <div style={{ margin: "8px 0 4px", paddingLeft: 8 }}>
                  <span style={{ fontSize: 11, fontWeight: 700, color: "#888", textTransform: "uppercase" }}>Resultado Financiero</span>
                </div>
                <PyGRow label="12. Ingresos financieros" value={data.ingresosFinancieros} />
                <PyGRow label="13. Gastos financieros" value={data.gastosFinancieros} isGasto />
                <PyGRow label="A.2) Resultado Financiero" value={data.resultadoFinanciero} isTotal colored />

                {/* Antes de impuestos */}
                <div style={{ height: 4 }} />
                <PyGRow label="A.3) Resultado Antes de Impuestos" value={data.resultadoAntesImpuestos} isTotal colored />
                <PyGRow label="17. Impuesto sobre beneficios (25%)" value={data.impuestoBeneficios} isGasto />

                {/* RESULTADO FINAL */}
                <div style={{
                  marginTop: 12, padding: 16,
                  background: resultado >= 0 ? "#E8F5E9" : "#FFEBEE",
                  border: `2px solid ${resultado >= 0 ? "#4CAF50" : "#EF5350"}`,
                  borderRadius: 8, textAlign: "center"
                }}>
                  <div style={{
                    fontSize: 11, fontWeight: 800, letterSpacing: 1,
                    color: resultado >= 0 ? "#2E7D32" : "#C62828",
                    textTransform: "uppercase", marginBottom: 6
                  }}>
                    A.4) {resultado >= 0 ? "Beneficio" : "Pérdida"} del Ejercicio
                  </div>
                  <div style={{
                    fontSize: 28, fontWeight: 900,
                    color: resultado >= 0 ? "#2E7D32" : "#C62828",
                    fontFamily: "monospace"
                  }}>
                    {fmt(Math.abs(resultado))}
                  </div>
                  <div style={{ fontSize: 12, color: resultado >= 0 ? "#388E3C" : "#E53935", marginTop: 4 }}>
                    Margen neto: {data.margenBeneficio}%
                  </div>
                </div>
              </Section>

              {/* PIE CHART */}
              <Section title="Distribución de Gastos">
                <div style={{ height: 260 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <RechartsPie>
                      <Pie data={pieData} dataKey="importe" nameKey="label"
                        cx="50%" cy="50%" outerRadius={90} labelLine={false}
                        label={({ percent }) => percent > 0.05 ? `${(percent*100).toFixed(0)}%` : ""}
                      >
                        {pieData.map((_, i) => (
                          <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip formatter={(v) => fmt(v)} />
                      <Legend iconType="circle" iconSize={10} wrapperStyle={{ fontSize: 11 }} />
                    </RechartsPie>
                  </ResponsiveContainer>
                </div>
              </Section>
            </div>
          </div>
        )}

        {/* ═══════════════════════════════════════ */}
        {/* TAB: Análisis Gráfico                   */}
        {/* ═══════════════════════════════════════ */}
        {activeTab === "grafico" && (
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
            {/* Barras: gastos por categoría */}
            <div style={{ background: "#fff", borderRadius: 12, padding: 20, boxShadow: "0 1px 4px rgba(0,0,0,0.08)" }}>
              <h3 style={{ margin: "0 0 16px", fontSize: 14, fontWeight: 700, color: "#1565C0" }}>
                Top Gastos por Categoría
              </h3>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={barData} layout="vertical" margin={{ left: 0, right: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                  <XAxis type="number" tickFormatter={v => `${(v/1000).toFixed(1)}k€`} fontSize={10} />
                  <YAxis type="category" dataKey="name" width={110} fontSize={11} />
                  <Tooltip formatter={v => fmt(v)} />
                  <Bar dataKey="importe" fill="#1565C0" radius={[0, 4, 4, 0]}>
                    {barData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>

            {/* Pie */}
            <div style={{ background: "#fff", borderRadius: 12, padding: 20, boxShadow: "0 1px 4px rgba(0,0,0,0.08)" }}>
              <h3 style={{ margin: "0 0 16px", fontSize: 14, fontWeight: 700, color: "#1565C0" }}>
                Distribución de Gastos
              </h3>
              <ResponsiveContainer width="100%" height={300}>
                <RechartsPie>
                  <Pie data={pieData} dataKey="importe" nameKey="label"
                    cx="50%" cy="50%" outerRadius={110} innerRadius={50}
                  >
                    {pieData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                  </Pie>
                  <Tooltip formatter={v => fmt(v)} />
                  <Legend iconType="circle" iconSize={10} wrapperStyle={{ fontSize: 11 }} />
                </RechartsPie>
              </ResponsiveContainer>
            </div>

            {/* Ingresos vs Gastos */}
            <div style={{ background: "#fff", borderRadius: 12, padding: 20, boxShadow: "0 1px 4px rgba(0,0,0,0.08)", gridColumn: "1 / -1" }}>
              <h3 style={{ margin: "0 0 16px", fontSize: 14, fontWeight: 700, color: "#1565C0" }}>
                Resumen Financiero
              </h3>
              <ResponsiveContainer width="100%" height={180}>
                <BarChart data={[
                  { name: "Ingresos", value: data.totalIngresos, fill: "#2E7D32" },
                  { name: "Gastos", value: data.totalGastos, fill: "#C62828" },
                  { name: "Resultado explotación", value: data.resultadoExplotacion, fill: "#1565C0" },
                  { name: "Resultado ejercicio", value: data.resultadoEjercicio, fill: resultado >= 0 ? "#43A047" : "#E53935" },
                ]} margin={{ left: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="name" fontSize={11} />
                  <YAxis tickFormatter={v => `${(v/1000).toFixed(0)}k€`} fontSize={10} />
                  <Tooltip formatter={v => fmt(v)} />
                  <Bar dataKey="value" radius={[6,6,0,0]}>
                    {[
                      "#2E7D32","#C62828","#1565C0",
                      resultado >= 0 ? "#43A047" : "#E53935"
                    ].map((c,i) => <Cell key={i} fill={c} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        )}

        {/* ── FOOTER ── */}
        <div style={{
          marginTop: 24, padding: "12px 16px",
          background: "#fff", borderRadius: 8, fontSize: 11, color: "#888",
          boxShadow: "0 1px 3px rgba(0,0,0,0.06)",
          display: "flex", justifyContent: "space-between", flexWrap: "wrap", gap: 8
        }}>
          <span>Lavadero Sepúlveda · CIF: B-XXXXXXXX · Córdoba</span>
          <span>Período: {data.fechaDesde} — {data.fechaHasta} · Ejercicio {data.ejercicio}</span>
          <span>Generado: {new Date().toLocaleString("es-ES")}</span>
        </div>
      </div>
    </div>
  );
}