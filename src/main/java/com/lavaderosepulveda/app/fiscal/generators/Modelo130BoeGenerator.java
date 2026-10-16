package com.lavaderosepulveda.app.fiscal.generators;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Generador del fichero de importación del Modelo 130
 * (IRPF – Pago fraccionado. Empresarios y profesionales en estimación directa).
 *
 * Diseño de registro oficial AEAT: Orden HAP/258/2015 (DR130e15v12, ejercicios 2019 y siguientes).
 *
 * Estructura del fichero (una sola línea, ISO-8859-1, sin saltos de línea, 946 bytes):
 *   <T1300{AAAA}{PP}0000>                    17  cabecera
 *   <AUX> ... </AUX>                        311  datos auxiliares (versión programa, NIF desarrollador)
 *   <T13001000> ... </T13001000>            600  página 01 con las casillas
 *   </T1300{AAAA}{PP}0000>                   18  cierre
 *
 * Nombre del fichero: {NIF}{EJERCICIO}{PERIODO}.130   (p. ej. 12345678Z20263T.130)
 *
 * Casillas (todas ACUMULADAS desde el 1 de enero hasta el final del trimestre):
 *   [01] Ingresos computables
 *   [02] Gastos fiscalmente deducibles (en simplificada incluye el 5% de difícil justificación, máx. 2.000 €/año)
 *   [03] Rendimiento neto = 01 - 02
 *   [04] 20% de [03] si es positivo (truncado a céntimos, como hace la AEAT)
 *   [05] Pagos fraccionados de trimestres anteriores del ejercicio (suma de [07] positivas anteriores)
 *   [06] Retenciones e ingresos a cuenta soportados
 *   [07] = 04 - 05 - 06
 *   [08]-[11] Actividades agrícolas/ganaderas/forestales/pesqueras (aquí siempre 0)
 *   [12] = [07] si es positiva + [11]
 *   [13] Minoración por deducción art. 110.3.c) Reglamento IRPF
 *   [14] = 12 - 13
 *   [15] Resultados negativos de trimestres anteriores pendientes de deducir
 *   [16] Deducción por préstamos para vivienda habitual
 *   [17] = 14 - 15 - 16
 *   [18] Resultado de la declaración anterior (solo complementarias)
 *   [19] Resultado de la declaración = 17 - 18
 */
public class Modelo130BoeGenerator {

    private static final int LEN_CABECERA = 17;
    private static final int LEN_AUX      = 311;
    private static final int LEN_PAGINA   = 600;
    private static final int LEN_CIERRE   = 18;
    public  static final int LEN_TOTAL    = LEN_CABECERA + LEN_AUX + LEN_PAGINA + LEN_CIERRE; // 946

    private static final BigDecimal VEINTE_POR_CIENTO = new BigDecimal("0.20");

    // ─────────────────────────────────────────────────────────────────────────
    // Enum Período
    // ─────────────────────────────────────────────────────────────────────────
    public enum Periodo {
        PRIMER_TRIMESTRE("1T"),
        SEGUNDO_TRIMESTRE("2T"),
        TERCER_TRIMESTRE("3T"),
        CUARTO_TRIMESTRE("4T");

        private final String codigo;
        Periodo(String codigo) { this.codigo = codigo; }
        public String getCodigo() { return codigo; }
    }

    /** Tipo de declaración según diseño de registro. */
    public enum TipoDeclaracion {
        INGRESO('I'),
        DOMICILIACION('U'),
        NEGATIVA('N'),
        A_DEDUCIR('B');

        private final char codigo;
        TipoDeclaracion(char codigo) { this.codigo = codigo; }
        public char getCodigo() { return codigo; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Datos de entrada
    // ─────────────────────────────────────────────────────────────────────────
    private final String  nif;
    private final String  apellidos;
    private final String  nombre;
    private final int     ejercicio;
    private final Periodo periodo;

    private final BigDecimal ingresos;                       // [01]
    private final BigDecimal gastos;                         // [02]
    private final BigDecimal pagosFraccionadosAnteriores;    // [05]
    private final BigDecimal retenciones;                    // [06]
    private final BigDecimal minoracionArt110;               // [13]
    private final BigDecimal resultadosNegativosAnteriores;  // [15]
    private final BigDecimal deduccionVivienda;              // [16]
    private final BigDecimal resultadoDeclaracionAnterior;   // [18]

    private final boolean complementaria;
    private final String  justificanteAnterior;
    private final String  ibanDomiciliacion;
    private final String  versionPrograma;
    private final String  nifDesarrollador;

    // ─────────────────────────────────────────────────────────────────────────
    // Casillas calculadas
    // ─────────────────────────────────────────────────────────────────────────
    private final BigDecimal c01, c02, c03, c04, c05, c06, c07;
    private final BigDecimal c08, c09, c10, c11;
    private final BigDecimal c12, c13, c14, c15, c16, c17, c18, c19;
    private final TipoDeclaracion tipoDeclaracion;

    private Modelo130BoeGenerator(Builder b) {
        this.nif                           = b.nif;
        this.apellidos                     = b.apellidos;
        this.nombre                        = b.nombre;
        this.ejercicio                     = b.ejercicio;
        this.periodo                       = b.periodo;
        this.ingresos                      = nz(b.ingresos);
        this.gastos                        = nz(b.gastos);
        this.pagosFraccionadosAnteriores   = nz(b.pagosFraccionadosAnteriores);
        this.retenciones                   = nz(b.retenciones);
        this.minoracionArt110              = nz(b.minoracionArt110);
        this.resultadosNegativosAnteriores = nz(b.resultadosNegativosAnteriores);
        this.deduccionVivienda             = nz(b.deduccionVivienda);
        this.resultadoDeclaracionAnterior  = nz(b.resultadoDeclaracionAnterior);
        this.complementaria                = b.complementaria;
        this.justificanteAnterior          = b.justificanteAnterior;
        this.ibanDomiciliacion             = b.ibanDomiciliacion;
        this.versionPrograma               = b.versionPrograma;
        this.nifDesarrollador              = b.nifDesarrollador;

        // ── Sección I: actividades económicas (no agrícolas) ─────────────────
        c01 = esc(ingresos);
        c02 = esc(gastos);
        c03 = c01.subtract(c02);
        c04 = c03.signum() > 0
                ? c03.multiply(VEINTE_POR_CIENTO).setScale(2, RoundingMode.DOWN)
                : BigDecimal.ZERO.setScale(2);
        c05 = esc(pagosFraccionadosAnteriores);
        c06 = esc(retenciones);
        c07 = c04.subtract(c05).subtract(c06);

        // ── Sección II: agrícolas/ganaderas (no aplica) ──────────────────────
        c08 = BigDecimal.ZERO.setScale(2);
        c09 = BigDecimal.ZERO.setScale(2);
        c10 = BigDecimal.ZERO.setScale(2);
        c11 = BigDecimal.ZERO.setScale(2);

        // ── Sección III: total liquidación ───────────────────────────────────
        c12 = max0(c07).add(max0(c11));
        c13 = min(esc(minoracionArt110), c12);
        c14 = c12.subtract(c13);
        c15 = min(esc(resultadosNegativosAnteriores), max0(c14));
        c16 = min(esc(deduccionVivienda), max0(c14.subtract(c15)));
        c17 = c14.subtract(c15).subtract(c16);
        c18 = complementaria ? esc(resultadoDeclaracionAnterior) : BigDecimal.ZERO.setScale(2);
        c19 = c17.subtract(c18);

        if (c19.signum() > 0) {
            tipoDeclaracion = (ibanDomiciliacion != null && !ibanDomiciliacion.isBlank())
                    ? TipoDeclaracion.DOMICILIACION : TipoDeclaracion.INGRESO;
        } else if (c19.signum() < 0 && periodo != Periodo.CUARTO_TRIMESTRE) {
            tipoDeclaracion = TipoDeclaracion.A_DEDUCIR;
        } else {
            tipoDeclaracion = TipoDeclaracion.NEGATIVA;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────────

    public String getNombreFichero() {
        return nif + ejercicio + periodo.getCodigo() + ".130";
    }

    public Path generarFichero(String directorioSalida) throws IOException {
        Path ruta = Paths.get(directorioSalida, getNombreFichero());
        Files.write(ruta, generarBytes());
        return ruta;
    }

    /** Bytes listos para descargar/subir (ISO-8859-1). Úsalo también en el endpoint de Spring. */
    public byte[] generarBytes() {
        String contenido = generarContenido();
        CharsetEncoder enc = StandardCharsets.ISO_8859_1.newEncoder();
        if (!enc.canEncode(contenido)) {
            throw new IllegalStateException("El contenido tiene caracteres no representables en ISO-8859-1");
        }
        byte[] bytes = contenido.getBytes(StandardCharsets.ISO_8859_1);
        if (bytes.length != LEN_TOTAL) {
            throw new IllegalStateException("Longitud incorrecta: " + bytes.length + " (esperado " + LEN_TOTAL + ")");
        }
        return bytes;
    }

    public String generarContenido() {
        return cabecera() + aux() + pagina01() + cierre();
    }

    public BigDecimal getResultado()               { return c19; }
    public TipoDeclaracion getTipoDeclaracion()    { return tipoDeclaracion; }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloques del fichero
    // ─────────────────────────────────────────────────────────────────────────

    private String cabecera() {
        String s = "<T130" + "0" + ejercicio + periodo.getCodigo() + "0000>";
        return comprobar(s, LEN_CABECERA, "cabecera");
    }

    private String aux() {
        StringBuilder r = new StringBuilder();
        r.append("<AUX>");                                // 5
        r.append(blancos(70));                            // Reservado AEAT
        r.append(alfa(versionPrograma, 4));               // Versión del programa
        r.append(blancos(4));                             // Reservado AEAT
        r.append(alfa(nifDesarrollador, 9));              // NIF empresa desarrollo
        r.append(blancos(213));                           // Reservado AEAT
        r.append("</AUX>");                               // 6
        return comprobar(r.toString(), LEN_AUX, "AUX");
    }

    private String pagina01() {
        StringBuilder r = new StringBuilder();
        r.append("<T13001000>");                          // Inicio página 01 (11)
        r.append(" ");                                    // Indicador página complementaria
        r.append(tipoDeclaracion.getCodigo());            // Tipo de declaración
        r.append(alfa(nif, 9));                           // NIF
        r.append(alfa(apellidos, 60));                    // Apellidos o razón social
        r.append(alfa(nombre, 20));                       // Nombre
        r.append(ejercicio);                              // Ejercicio (4)
        r.append(periodo.getCodigo());                    // Período (2)

        r.append(importe(c01)).append(importe(c02)).append(importe(c03))
         .append(importe(c04)).append(importe(c05)).append(importe(c06))
         .append(importe(c07)).append(importe(c08)).append(importe(c09))
         .append(importe(c10)).append(importe(c11)).append(importe(c12))
         .append(importe(c13)).append(importe(c14)).append(importe(c15))
         .append(importe(c16)).append(importe(c17)).append(importe(c18))
         .append(importe(c19));

        r.append(complementaria ? "X" : " ");             // Declaración complementaria
        r.append(alfa(complementaria ? justificanteAnterior : "", 13)); // Nº justificante anterior
        r.append(alfa(ibanDomiciliacion == null ? "" : ibanDomiciliacion.replace(" ", ""), 34)); // IBAN
        r.append(blancos(96));                            // Reservado AEAT
        r.append(blancos(13));                            // Sello electrónico
        r.append("</T13001000>");                         // Fin página (12)
        return comprobar(r.toString(), LEN_PAGINA, "página 01");
    }

    private String cierre() {
        String s = "</T130" + "0" + ejercicio + periodo.getCodigo() + "0000>";
        return comprobar(s, LEN_CIERRE, "cierre");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Formateo según normas AEAT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Importe de 17 posiciones: 15 enteros + 2 decimales, sin coma, ceros a la izquierda.
     * Si es negativo, "N" en la primera posición y 16 dígitos.
     */
    private static String importe(BigDecimal valor) {
        BigDecimal v = esc(valor);
        long centimos = v.abs().movePointRight(2).longValueExact();
        if (v.signum() < 0) {
            return "N" + String.format("%016d", centimos);
        }
        return String.format("%017d", centimos);
    }

    /**
     * Alfanumérico: mayúsculas, sin tildes, conserva Ñ y Ç, solo caracteres permitidos,
     * alineado a la izquierda y relleno con blancos.
     */
    private static String alfa(String texto, int longitud) {
        String t = limpiarTexto(texto);
        if (t.length() >= longitud) return t.substring(0, longitud);
        return t + " ".repeat(longitud - t.length());
    }

    private static String limpiarTexto(String texto) {
        if (texto == null) return "";
        String t = texto.trim().toUpperCase(Locale.ROOT)
                .replace("Ñ", "\u0001")
                .replace("Ç", "\u0002");
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        t = t.replace("\u0001", "Ñ").replace("\u0002", "Ç");
        t = t.replaceAll("[^A-Z0-9ÑÇ .,\\-_&'/():;]", "");
        return t.replaceAll("\\s+", " ");
    }

    private static String blancos(int n) {
        return " ".repeat(n);
    }

    private static String comprobar(String bloque, int esperado, String nombreBloque) {
        if (bloque.length() != esperado) {
            throw new IllegalStateException("Bloque " + nombreBloque + " con longitud "
                    + bloque.length() + " (esperado " + esperado + ")");
        }
        return bloque;
    }

    private static BigDecimal nz(BigDecimal v)  { return v == null ? BigDecimal.ZERO : v; }
    private static BigDecimal esc(BigDecimal v) { return nz(v).setScale(2, RoundingMode.HALF_UP); }
    private static BigDecimal max0(BigDecimal v){ return v.signum() > 0 ? v : BigDecimal.ZERO.setScale(2); }
    private static BigDecimal min(BigDecimal a, BigDecimal b) { return a.compareTo(b) <= 0 ? a : b; }

    // ─────────────────────────────────────────────────────────────────────────
    // Builder
    // ─────────────────────────────────────────────────────────────────────────
    public static class Builder {
        private String  nif        = "";
        private String  apellidos  = "";
        private String  nombre     = "";
        private int     ejercicio  = java.time.Year.now().getValue();
        private Periodo periodo    = Periodo.PRIMER_TRIMESTRE;

        private BigDecimal ingresos                      = BigDecimal.ZERO;
        private BigDecimal gastos                        = BigDecimal.ZERO;
        private BigDecimal pagosFraccionadosAnteriores   = BigDecimal.ZERO;
        private BigDecimal retenciones                   = BigDecimal.ZERO;
        private BigDecimal minoracionArt110              = BigDecimal.ZERO;
        private BigDecimal resultadosNegativosAnteriores = BigDecimal.ZERO;
        private BigDecimal deduccionVivienda             = BigDecimal.ZERO;
        private BigDecimal resultadoDeclaracionAnterior  = BigDecimal.ZERO;

        private boolean complementaria       = false;
        private String  justificanteAnterior = "";
        private String  ibanDomiciliacion    = "";
        private String  versionPrograma      = "LS01";
        private String  nifDesarrollador     = "";

        public Builder nif(String v)                            { this.nif = v == null ? "" : v.toUpperCase(Locale.ROOT).replaceAll("[\\s-]", ""); return this; }
        /** Apellidos (persona física) o razón social. */
        public Builder apellidos(String v)                      { this.apellidos = v; return this; }
        /** Nombre de pila (persona física). */
        public Builder nombre(String v)                         { this.nombre = v; return this; }
        public Builder ejercicio(int v)                         { this.ejercicio = v; return this; }
        public Builder periodo(Periodo v)                       { this.periodo = v; return this; }
        /** [01] Ingresos acumulados desde el 1 de enero. */
        public Builder ingresos(BigDecimal v)                   { this.ingresos = v; return this; }
        /** [02] Gastos deducibles acumulados desde el 1 de enero. */
        public Builder gastos(BigDecimal v)                     { this.gastos = v; return this; }
        /** [05] Suma de las casillas [07] positivas de los trimestres anteriores del ejercicio. */
        public Builder pagosFraccionadosAnteriores(BigDecimal v){ this.pagosFraccionadosAnteriores = v; return this; }
        /** [06] Retenciones e ingresos a cuenta acumulados. */
        public Builder retenciones(BigDecimal v)                { this.retenciones = v; return this; }
        /** [13] Minoración art. 110.3.c) RIRPF. */
        public Builder minoracionArt110(BigDecimal v)           { this.minoracionArt110 = v; return this; }
        /** [15] Resultados negativos de trimestres anteriores (en positivo). */
        public Builder resultadosNegativosAnteriores(BigDecimal v){ this.resultadosNegativosAnteriores = v; return this; }
        /** [16] Deducción por préstamo de vivienda habitual. */
        public Builder deduccionVivienda(BigDecimal v)          { this.deduccionVivienda = v; return this; }
        /** Declaración complementaria: [18] e indicador + justificante anterior. */
        public Builder complementaria(String justificanteAnterior, BigDecimal resultadoAnterior) {
            this.complementaria = true;
            this.justificanteAnterior = justificanteAnterior;
            this.resultadoDeclaracionAnterior = resultadoAnterior;
            return this;
        }
        /** IBAN para domiciliar el pago (tipo de declaración "U"). */
        public Builder ibanDomiciliacion(String v)              { this.ibanDomiciliacion = v; return this; }
        public Builder versionPrograma(String v)                { this.versionPrograma = v; return this; }
        public Builder nifDesarrollador(String v)               { this.nifDesarrollador = v; return this; }

        public Modelo130BoeGenerator build() {
            if (nif.length() != 9)
                throw new IllegalStateException("NIF obligatorio de 9 caracteres: '" + nif + "'");
            if (apellidos == null || apellidos.isBlank())
                throw new IllegalStateException("Apellidos o razón social obligatorios");
            if (ejercicio < 2015 || ejercicio > 9999)
                throw new IllegalStateException("Ejercicio no válido: " + ejercicio);
            if (periodo == null)
                throw new IllegalStateException("Período obligatorio");
            if (complementaria && (justificanteAnterior == null || justificanteAnterior.length() != 13))
                throw new IllegalStateException("La complementaria requiere el justificante anterior de 13 dígitos");
            return new Modelo130BoeGenerator(this);
        }
    }
}