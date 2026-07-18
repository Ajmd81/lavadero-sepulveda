package com.lavaderosepulveda.app.fiscal.generators;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generador del fichero BOE para el Modelo 130 (IRPF — Pago fraccionado, actividades económicas).
 *
 * Aplicable a autónomos en estimación directa (normal o simplificada).
 * Régimen general: ingreso el 20% del rendimiento neto positivo del trimestre
 * menos retenciones soportadas en el período, con mínimo de 0.
 *
 * Formato: ISO-8859-1, texto posicional.
 * Nombre del fichero: {NIF}{EJERCICIO}{PERIODO}.130
 *
 * Referencia normativa: Orden EHA/672/2007, Modelo 130 IRPF.
 *
 * Ejemplo de uso:
 *   Modelo130BoeGenerator gen = new Modelo130BoeGenerator.Builder()
 *       .nif("12345678A")
 *       .nombre("SEPULVEDA GARCIA FRANCISCO")
 *       .ejercicio(2026)
 *       .periodo(Modelo130BoeGenerator.Periodo.PRIMER_TRIMESTRE)
 *       .ingresosTrimestre(new BigDecimal("5000.00"))
*        .gastosTrimestre(new BigDecimal("1800.00"))
 *       .retencionesComputadas(BigDecimal.ZERO)
 *       .pagosAnteriorese(BigDecimal.ZERO)
 *       .build();
 */
public class Modelo130BoeGenerator {

    private static final Charset ISO = Charset.forName("ISO-8859-1");
    private static final int LEN_REGISTRO = 500;

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

    // ─────────────────────────────────────────────────────────────────────────
    // Campos
    // ─────────────────────────────────────────────────────────────────────────
    private final String   nif;
    private final String   nombre;
    private final int      ejercicio;
    private final Periodo  periodo;

    /** Casilla 01: Ingresos computables acumulados en el año hasta el trimestre. */
    private final BigDecimal ingresosTrimestre;

    /** Casilla 02: Gastos fiscalmente deducibles acumulados hasta el trimestre. */
    private final BigDecimal gastosTrimestre;

    /**
     * Casilla 11: Retenciones e ingresos a cuenta soportados acumulados.
     * Para Lavadero Sepúlveda habitualmente = 0 (servicio final a consumidor).
     */
    private final BigDecimal retencionesComputadas;

    /**
     * Casilla 14: Suma de pagos fraccionados ingresados en trimestres anteriores del mismo año.
     * Se rellena en 2T, 3T y 4T.
     */
    private final BigDecimal pagosAnteriores;

    private Modelo130BoeGenerator(Builder b) {
        this.nif                   = b.nif;
        this.nombre                = b.nombre;
        this.ejercicio             = b.ejercicio;
        this.periodo               = b.periodo;
        this.ingresosTrimestre     = b.ingresosTrimestre;
        this.gastosTrimestre       = b.gastosTrimestre;
        this.retencionesComputadas = b.retencionesComputadas;
        this.pagosAnteriores       = b.pagosAnteriores;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────────

    public Path generarFichero(String directorioSalida) throws IOException {
        String nombreFichero = nif + ejercicio + periodo.getCodigo() + ".130";
        Path ruta = Paths.get(directorioSalida, nombreFichero);
        try (OutputStreamWriter w = new OutputStreamWriter(
                new FileOutputStream(ruta.toFile()), ISO)) {
            w.write(generarContenido());
        }
        return ruta;
    }

    /** Devuelve el contenido completo del fichero como String (codificación ISO-8859-1). */
    public String generarContenido() {
        return generarRegistro1() + generarRegistro2();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Registro 1 – Cabecera presentador
    // ─────────────────────────────────────────────────────────────────────────
    private String generarRegistro1() {
        StringBuilder r = new StringBuilder();

        r.append("1");                                          // Tipo registro
        r.append("130");                                        // Modelo
        r.append(padLeft(String.valueOf(ejercicio), 4, '0'));   // Ejercicio
        r.append(padRight(periodo.getCodigo(), 2));             // Período
        r.append("T");                                          // Soporte telemático
        r.append(padRight(nif, 9));                             // NIF presentador
        r.append(padRight(limpiarTexto(nombre), 39));           // Nombre presentador
        r.append(padRight(nif, 9));                             // NIF declarante
        r.append(padRight(limpiarTexto(nombre), 39));           // Nombre declarante
        r.append(padRight("", 7));                              // Teléfono
        r.append(padRight("", 13));                             // Fax
        r.append(padRight("", 50));                             // Email
        r.append("  ");                                         // Indicador sustitutiva
        r.append(padRight("", 13));                             // Núm. justificante anterior

        int restantes = LEN_REGISTRO - r.length();
        if (restantes > 0) r.append(" ".repeat(restantes));
        return r.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Registro 2 – Datos de la declaración
    // ─────────────────────────────────────────────────────────────────────────
    private String generarRegistro2() {
        StringBuilder r = new StringBuilder();

        r.append("2");
        r.append("130");
        r.append(padLeft(String.valueOf(ejercicio), 4, '0'));
        r.append(padRight(periodo.getCodigo(), 2));
        r.append(padRight(nif, 9));
        r.append(padRight(limpiarTexto(nombre), 39));

        // ── Cálculo rendimiento neto ─────────────────────────────────────────
        // Casilla 01: Ingresos computables
        r.append(casilla("001", ingresosTrimestre));

        // Casilla 02: Gastos deducibles
        r.append(casilla("002", gastosTrimestre));

        // Casilla 03: Rendimiento neto (01 - 02)
        BigDecimal rendimientoNeto = ingresosTrimestre.subtract(gastosTrimestre);
        r.append(casilla("003", rendimientoNeto));

        // Casilla 05: Base de cálculo (= 03 si positivo, si no = 0)
        BigDecimal baseCalculo = rendimientoNeto.compareTo(BigDecimal.ZERO) > 0
                ? rendimientoNeto : BigDecimal.ZERO;
        r.append(casilla("005", baseCalculo));

        // Casilla 07: 20% de la base de cálculo (pago fraccionado bruto)
        BigDecimal pagoFraccionadoBruto = baseCalculo
                .multiply(new BigDecimal("0.20"))
                .setScale(2, RoundingMode.HALF_UP);
        r.append(casilla("007", pagoFraccionadoBruto));

        // Casilla 11: Retenciones e ingresos a cuenta acumulados
        r.append(casilla("011", retencionesComputadas));

        // Casilla 13: Pagos fraccionados anteriores del año
        r.append(casilla("013", pagosAnteriores));

        // Casilla 14: Resultado (07 - 11 - 13), mínimo 0
        BigDecimal resultado = pagoFraccionadoBruto
                .subtract(retencionesComputadas)
                .subtract(pagosAnteriores);
        if (resultado.compareTo(BigDecimal.ZERO) < 0) resultado = BigDecimal.ZERO;
        r.append(casilla("014", resultado));

        // Casilla 15: Tipo declaración  I=ingresar  N=negativa/cero
        String tipoDeclaracion = resultado.compareTo(BigDecimal.ZERO) > 0 ? "I" : "N";
        r.append("015");
        r.append(" ");
        r.append(padRight(tipoDeclaracion, 12));

        // Casilla 16: Importe a ingresar
        r.append(casilla("016", resultado));

        // Relleno hasta 500 chars
        int restantes = LEN_REGISTRO - r.length();
        if (restantes > 0) r.append(" ".repeat(restantes));
        return r.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String casilla(String clave, BigDecimal valor) {
        if (valor == null) valor = BigDecimal.ZERO;
        valor = valor.setScale(2, RoundingMode.HALF_UP);
        char signo = valor.compareTo(BigDecimal.ZERO) < 0 ? 'N' : ' ';
        long centimos = valor.abs()
                .multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
        return clave + signo + padLeft(String.valueOf(centimos), 12, '0');
    }

    private static String padLeft(String s, int length, char c) {
        if (s == null) s = "";
        if (s.length() >= length) return s.substring(0, length);
        return String.valueOf(c).repeat(length - s.length()) + s;
    }

    private static String padRight(String s, int length) {
        if (s == null) s = "";
        if (s.length() >= length) return s.substring(0, length);
        return s + " ".repeat(length - s.length());
    }

    private static String limpiarTexto(String texto) {
        if (texto == null) return "";
        return texto.toUpperCase()
                .replace("Á", "A").replace("É", "E").replace("Í", "I")
                .replace("Ó", "O").replace("Ú", "U").replace("Ü", "U")
                .replace("Ñ", "N");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Builder
    // ─────────────────────────────────────────────────────────────────────────
    public static class Builder {
        private String  nif       = "";
        private String  nombre    = "";
        private int     ejercicio = 2026;
        private Periodo periodo   = Periodo.PRIMER_TRIMESTRE;

        private BigDecimal ingresosTrimestre     = BigDecimal.ZERO;
        private BigDecimal gastosTrimestre       = BigDecimal.ZERO;
        private BigDecimal retencionesComputadas = BigDecimal.ZERO;
        private BigDecimal pagosAnteriores       = BigDecimal.ZERO;

        public Builder nif(String nif)                              { this.nif     = nif.toUpperCase().trim(); return this; }
        public Builder nombre(String nombre)                        { this.nombre  = nombre; return this; }
        public Builder ejercicio(int e)                             { this.ejercicio = e; return this; }
        public Builder periodo(Periodo p)                           { this.periodo = p; return this; }
        public Builder ingresosTrimestre(BigDecimal v)              { this.ingresosTrimestre     = v; return this; }
        public Builder gastosTrimestre(BigDecimal v)                { this.gastosTrimestre       = v; return this; }
        public Builder retencionesComputadas(BigDecimal v)          { this.retencionesComputadas = v; return this; }
        public Builder pagosAnteriores(BigDecimal v)                { this.pagosAnteriores       = v; return this; }

        public Modelo130BoeGenerator build() {
            if (nif.isEmpty())    throw new IllegalStateException("NIF es obligatorio");
            if (nombre.isEmpty()) throw new IllegalStateException("Nombre es obligatorio");
            return new Modelo130BoeGenerator(this);
        }
    }
}
