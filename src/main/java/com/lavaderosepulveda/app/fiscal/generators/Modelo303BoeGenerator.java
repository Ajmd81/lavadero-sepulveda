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
 * Generador del fichero BOE para el Modelo 303 (IVA Autoliquidación).
 *
 * Sigue el diseño de registro publicado por la AEAT según Orden HAC/646/2021.
 * Formato: ISO-8859-1, texto posicional, longitud fija 500 chars por registro.
 * Nombre del fichero generado: {NIF}{EJERCICIO}{PERIODO}.303
 *
 * Ejemplo de uso:
 *   Modelo303BoeGenerator gen = new Modelo303BoeGenerator.Builder()
 *       .nif("12345678A")
 *       .nombre("SEPULVEDA GARCIA FRANCISCO")
 *       .ejercicio(2026)
 *       .periodo(Modelo303BoeGenerator.Periodo.PRIMER_TRIMESTRE)
 *       .baseIvaRepercutido21(new BigDecimal("3813.06"))
 *       .cuotaIvaRepercutido21(new BigDecimal("800.86"))
 *       .baseIvaSoportado21(new BigDecimal("2513.07"))
 *       .cuotaIvaSoportado21(new BigDecimal("240.71"))
 *       .build();
 *   String contenido = gen.generarContenido();
 */
public class Modelo303BoeGenerator {

    private static final Charset ISO = Charset.forName("ISO-8859-1");
    private static final int LEN_CABECERA = 500;
    private static final int LEN_DETALLE  = 500;

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

    private final BigDecimal baseIvaRepercutido4,  cuotaIvaRepercutido4;
    private final BigDecimal baseIvaRepercutido10, cuotaIvaRepercutido10;
    private final BigDecimal baseIvaRepercutido21, cuotaIvaRepercutido21;

    private final BigDecimal baseIvaSoportado4,  cuotaIvaSoportado4;
    private final BigDecimal baseIvaSoportado10, cuotaIvaSoportado10;
    private final BigDecimal baseIvaSoportado21, cuotaIvaSoportado21;

    private final BigDecimal baseInversion21, cuotaInversion21;

    private Modelo303BoeGenerator(Builder b) {
        this.nif                   = b.nif;
        this.nombre                = b.nombre;
        this.ejercicio             = b.ejercicio;
        this.periodo               = b.periodo;
        this.baseIvaRepercutido4   = b.baseIvaRepercutido4;
        this.cuotaIvaRepercutido4  = b.cuotaIvaRepercutido4;
        this.baseIvaRepercutido10  = b.baseIvaRepercutido10;
        this.cuotaIvaRepercutido10 = b.cuotaIvaRepercutido10;
        this.baseIvaRepercutido21  = b.baseIvaRepercutido21;
        this.cuotaIvaRepercutido21 = b.cuotaIvaRepercutido21;
        this.baseIvaSoportado4     = b.baseIvaSoportado4;
        this.cuotaIvaSoportado4    = b.cuotaIvaSoportado4;
        this.baseIvaSoportado10    = b.baseIvaSoportado10;
        this.cuotaIvaSoportado10   = b.cuotaIvaSoportado10;
        this.baseIvaSoportado21    = b.baseIvaSoportado21;
        this.cuotaIvaSoportado21   = b.cuotaIvaSoportado21;
        this.baseInversion21       = b.baseInversion21;
        this.cuotaInversion21      = b.cuotaInversion21;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────────

    public Path generarFichero(String directorioSalida) throws IOException {
        String nombreFichero = nif + ejercicio + periodo.getCodigo() + ".303";
        Path ruta = Paths.get(directorioSalida, nombreFichero);
        try (OutputStreamWriter w = new OutputStreamWriter(
                new FileOutputStream(ruta.toFile()), ISO)) {
            w.write(generarContenido());
        }
        return ruta;
    }

    /** Devuelve el contenido completo del fichero como String ISO-8859-1. */
    public String generarContenido() {
        return generarRegistro1() + generarRegistro2();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Registro 1 – Cabecera presentador
    // ─────────────────────────────────────────────────────────────────────────
    private String generarRegistro1() {
        StringBuilder r = new StringBuilder();

        r.append("1");                                              // Pos 1   Tipo registro
        r.append("303");                                            // Pos 2-4 Modelo
        r.append(padLeft(String.valueOf(ejercicio), 4, '0'));       // Pos 5-8 Ejercicio
        r.append(padRight(periodo.getCodigo(), 2));                 // Pos 9-10 Período
        r.append(" ");                                              // Pos 11  Soporte telemático
        r.append(padRight(nif, 9));                                 // Pos 12-20 NIF presentador
        r.append(padRight(limpiarTexto(nombre), 39));               // Pos 21-59 Nombre presentador
        r.append(padRight(nif, 9));                                 // Pos 60-68 NIF declarante
        r.append(padRight(limpiarTexto(nombre), 39));               // Pos 69-107 Nombre declarante
        r.append(padRight("", 7));                                  // Pos 108-114 Teléfono
        r.append(padRight("", 13));                                 // Pos 115-127 Fax
        r.append(padRight("", 50));                                 // Pos 128-177 Email
        r.append("  ");                                             // Pos 178-179 Indicador sustitutiva
        r.append(padRight("", 13));                                 // Pos 180-192 Número justificante anterior

        // Relleno hasta 500 chars
        int restantes = LEN_CABECERA - r.length();
        if (restantes > 0) r.append(" ".repeat(restantes));

        return r.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Registro 2 – Datos de la declaración
    // ─────────────────────────────────────────────────────────────────────────
    private String generarRegistro2() {
        StringBuilder r = new StringBuilder();

        r.append("2");                                              // Tipo registro
        r.append("303");                                            // Modelo
        r.append(padLeft(String.valueOf(ejercicio), 4, '0'));       // Ejercicio
        r.append(padRight(periodo.getCodigo(), 2));                 // Período
        r.append(padRight(nif, 9));                                 // NIF declarante
        r.append(padRight(limpiarTexto(nombre), 39));               // Nombre declarante

        // ── PÁGINA 1: IVA DEVENGADO ──────────────────────────────────────────

        // Casilla 01: Base imponible 4%
        r.append(casilla("001", baseIvaRepercutido4));
        // Casilla 02: Cuota 4%
        r.append(casilla("002", cuotaIvaRepercutido4));
        // Casilla 03: Base imponible 10%
        r.append(casilla("003", baseIvaRepercutido10));
        // Casilla 04: Cuota 10%
        r.append(casilla("004", cuotaIvaRepercutido10));
        // Casilla 07: Base imponible 21%
        r.append(casilla("007", baseIvaRepercutido21));
        // Casilla 08: Cuota 21%  (etiqueta AEAT = "009" en este diseño)
        r.append(casilla("009", cuotaIvaRepercutido21));

        // Casilla 27: Total IVA devengado
        BigDecimal totalDevengado = cuotaIvaRepercutido4
                .add(cuotaIvaRepercutido10)
                .add(cuotaIvaRepercutido21);
        r.append(casilla("027", totalDevengado));

        // ── PÁGINA 2: IVA SOPORTADO ──────────────────────────────────────────

        // Casilla 28: Base soportado corrientes (todos tipos agrupados)
        r.append(casilla("028", baseIvaSoportado4.add(baseIvaSoportado10).add(baseIvaSoportado21)));
        // Casilla 29: Cuota soportado corrientes deducible
        BigDecimal totalSoportadoCorriente = cuotaIvaSoportado4.add(cuotaIvaSoportado10).add(cuotaIvaSoportado21);
        r.append(casilla("029", totalSoportadoCorriente));

        // Casilla 30: Base bienes de inversión
        r.append(casilla("030", baseInversion21));
        // Casilla 31: Cuota bienes de inversión deducible
        r.append(casilla("031", cuotaInversion21));

        // Casilla 45: Total IVA deducible
        BigDecimal totalDeducible = totalSoportadoCorriente.add(cuotaInversion21);
        r.append(casilla("045", totalDeducible));

        // Casilla 46: Resultado régimen general (27-45)
        BigDecimal resultado46 = totalDevengado.subtract(totalDeducible);
        r.append(casilla("046", resultado46));

        // Casilla 64: Resultado (= 46)
        r.append(casilla("064", resultado46));

        // Casilla 65: % Atribuible Admon. Estado = 100%
        r.append(casilla("065", new BigDecimal("100.00")));

        // Casilla 66: Resultado atribuible Admon. Estado
        r.append(casilla("066", resultado46));

        // Casilla 69: Tipo declaración  I=ingresar  N=compensar  D=devolver
        String tipoDeclaracion = resultado46.compareTo(BigDecimal.ZERO) > 0 ? "I" : "N";
        r.append("069");
        r.append(" ");
        r.append(padRight(tipoDeclaracion, 12));

        // Casilla 70: Resultado a ingresar
        BigDecimal aIngresar = resultado46.compareTo(BigDecimal.ZERO) > 0 ? resultado46 : BigDecimal.ZERO;
        r.append(casilla("070", aIngresar));

        // Relleno hasta 500 chars
        int restantes = LEN_DETALLE - r.length();
        if (restantes > 0) r.append(" ".repeat(restantes));

        return r.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers de formato
    // ─────────────────────────────────────────────────────────────────────────

    /** Bloque de casilla: 3 chars clave + 1 char signo + 12 chars importe (sin punto decimal). */
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

    /** Elimina tildes y caracteres problemáticos para ISO-8859-1/AEAT. */
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
        private String  nif     = "";
        private String  nombre  = "";
        private int     ejercicio = 2026;
        private Periodo periodo = Periodo.PRIMER_TRIMESTRE;

        private BigDecimal baseIvaRepercutido4   = BigDecimal.ZERO;
        private BigDecimal cuotaIvaRepercutido4  = BigDecimal.ZERO;
        private BigDecimal baseIvaRepercutido10  = BigDecimal.ZERO;
        private BigDecimal cuotaIvaRepercutido10 = BigDecimal.ZERO;
        private BigDecimal baseIvaRepercutido21  = BigDecimal.ZERO;
        private BigDecimal cuotaIvaRepercutido21 = BigDecimal.ZERO;

        private BigDecimal baseIvaSoportado4   = BigDecimal.ZERO;
        private BigDecimal cuotaIvaSoportado4  = BigDecimal.ZERO;
        private BigDecimal baseIvaSoportado10  = BigDecimal.ZERO;
        private BigDecimal cuotaIvaSoportado10 = BigDecimal.ZERO;
        private BigDecimal baseIvaSoportado21  = BigDecimal.ZERO;
        private BigDecimal cuotaIvaSoportado21 = BigDecimal.ZERO;

        private BigDecimal baseInversion21  = BigDecimal.ZERO;
        private BigDecimal cuotaInversion21 = BigDecimal.ZERO;

        public Builder nif(String nif)         { this.nif     = nif.toUpperCase().trim(); return this; }
        public Builder nombre(String nombre)   { this.nombre  = nombre; return this; }
        public Builder ejercicio(int e)        { this.ejercicio = e; return this; }
        public Builder periodo(Periodo p)      { this.periodo = p; return this; }

        public Builder baseIvaRepercutido4(BigDecimal v)   { this.baseIvaRepercutido4   = v; return this; }
        public Builder cuotaIvaRepercutido4(BigDecimal v)  { this.cuotaIvaRepercutido4  = v; return this; }
        public Builder baseIvaRepercutido10(BigDecimal v)  { this.baseIvaRepercutido10  = v; return this; }
        public Builder cuotaIvaRepercutido10(BigDecimal v) { this.cuotaIvaRepercutido10 = v; return this; }
        public Builder baseIvaRepercutido21(BigDecimal v)  { this.baseIvaRepercutido21  = v; return this; }
        public Builder cuotaIvaRepercutido21(BigDecimal v) { this.cuotaIvaRepercutido21 = v; return this; }

        public Builder baseIvaSoportado4(BigDecimal v)   { this.baseIvaSoportado4   = v; return this; }
        public Builder cuotaIvaSoportado4(BigDecimal v)  { this.cuotaIvaSoportado4  = v; return this; }
        public Builder baseIvaSoportado10(BigDecimal v)  { this.baseIvaSoportado10  = v; return this; }
        public Builder cuotaIvaSoportado10(BigDecimal v) { this.cuotaIvaSoportado10 = v; return this; }
        public Builder baseIvaSoportado21(BigDecimal v)  { this.baseIvaSoportado21  = v; return this; }
        public Builder cuotaIvaSoportado21(BigDecimal v) { this.cuotaIvaSoportado21 = v; return this; }

        public Builder baseInversion21(BigDecimal v)  { this.baseInversion21  = v; return this; }
        public Builder cuotaInversion21(BigDecimal v) { this.cuotaInversion21 = v; return this; }

        public Modelo303BoeGenerator build() {
            if (nif.isEmpty())    throw new IllegalStateException("NIF es obligatorio");
            if (nombre.isEmpty()) throw new IllegalStateException("Nombre es obligatorio");
            return new Modelo303BoeGenerator(this);
        }
    }
}
