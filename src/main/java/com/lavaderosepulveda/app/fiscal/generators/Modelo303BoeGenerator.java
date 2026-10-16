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
 * Generador del fichero de importación del Modelo 303 (IVA – Autoliquidación).
 *
 * Diseño de registro AEAT "303 – Ejercicio 2026 y siguientes" (DR303e26v101).
 *
 * Estructura (una sola línea, ISO-8859-1, sin saltos de línea):
 *   <T3030{AAAA}{PP}0000>                 17   cabecera
 *   <AUX> ... </AUX>                     311   datos auxiliares
 *   <T30301000> ... </T30301000>        1581   página 01: régimen general (devengado / deducible)
 *   <T30303000> ... </T30303000>        1017   página 03: información adicional y resultado
 *   <T303DID00> ... </T303DID00>         823   página DID: domiciliación / devolución
 *   </T3030{AAAA}{PP}0000>                18   cierre
 *                                       ----
 *                                       3767   bytes en total
 *
 * Pensado para un autónomo en régimen general de IVA, territorio común, liquidación trimestral,
 * sin SII, sin REDEME, sin criterio de caja, sin prorrata y sin recargo de equivalencia.
 * No genera la página 04 (solo para exonerados del 390 en el 4T) ni la 02/05.
 *
 * Nombre del fichero: {NIF}{EJERCICIO}{PERIODO}.303   (p. ej. 12345678Z20263T.303)
 */
public class Modelo303BoeGenerator {

    private static final int LEN_CABECERA = 17;
    private static final int LEN_AUX      = 311;
    private static final int LEN_PAG01    = 1581;
    private static final int LEN_PAG03    = 1017;
    private static final int LEN_DID      = 823;
    private static final int LEN_CIERRE   = 18;
    public  static final int LEN_TOTAL    = LEN_CABECERA + LEN_AUX + LEN_PAG01 + LEN_PAG03 + LEN_DID + LEN_CIERRE;

    private static final BigDecimal CIEN = new BigDecimal("100");

    // ─────────────────────────────────────────────────────────────────────────
    // Enums
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

    /** Tipo de declaración (posición 13 de la página 01). */
    public enum TipoDeclaracion {
        INGRESO('I'),          // a ingresar sin domiciliación
        DOMICILIACION('U'),    // a ingresar con domiciliación en cuenta
        COMPENSAR('C'),        // negativo: a compensar en periodos siguientes
        DEVOLUCION('D'),       // negativo en 4T: devolución por transferencia (SEPA)
        SIN_ACTIVIDAD('N');    // sin actividad / resultado cero

        private final char codigo;
        TipoDeclaracion(char codigo) { this.codigo = codigo; }
        public char getCodigo() { return codigo; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Datos de entrada
    // ─────────────────────────────────────────────────────────────────────────
    private final String  nif;
    private final String  apellidosNombre;
    private final int     ejercicio;
    private final Periodo periodo;

    private final boolean rectificativa;
    private final String  justificanteAnterior;
    private final String  iban;
    private final boolean solicitarDevolucion;
    private final String  versionPrograma;
    private final String  nifDesarrollador;

    // ─────────────────────────────────────────────────────────────────────────
    // Casillas
    // ─────────────────────────────────────────────────────────────────────────
    // IVA devengado
    private final BigDecimal c01, c03;          // 4 %
    private final BigDecimal c04, c06;          // 10 %
    private final BigDecimal c07, c09;          // 21 %
    private final BigDecimal c10, c11;          // adquisiciones intracomunitarias
    private final BigDecimal c12, c13;          // inversión del sujeto pasivo
    private final BigDecimal c14, c15;          // modificación de bases y cuotas
    private final BigDecimal c27;               // total devengado
    // IVA deducible
    private final BigDecimal c28, c29;          // operaciones interiores corrientes
    private final BigDecimal c30, c31;          // bienes de inversión
    private final BigDecimal c36, c37;          // adq. intracomunitarias corrientes
    private final BigDecimal c40, c41;          // rectificación de deducciones
    private final BigDecimal c45;               // total a deducir
    private final BigDecimal c46;               // resultado régimen general
    // Información adicional
    private final BigDecimal c59, c60, c120, c122;
    // Resultado
    private final BigDecimal c64, c65, c66;
    private final BigDecimal c77;
    private final BigDecimal c110, c78, c87;
    private final BigDecimal c69, c70, c71;

    private final TipoDeclaracion tipoDeclaracion;

    private Modelo303BoeGenerator(Builder b) {
        this.nif                  = b.nif;
        this.apellidosNombre      = (nz(b.apellidos) + " " + nz(b.nombre)).trim();
        this.ejercicio            = b.ejercicio;
        this.periodo              = b.periodo;
        this.rectificativa        = b.rectificativa;
        this.justificanteAnterior = b.justificanteAnterior;
        this.iban                 = b.iban == null ? "" : b.iban.replace(" ", "").toUpperCase(Locale.ROOT);
        this.solicitarDevolucion  = b.solicitarDevolucion;
        this.versionPrograma      = b.versionPrograma;
        this.nifDesarrollador     = b.nifDesarrollador;

        // ── IVA devengado ────────────────────────────────────────────────────
        c01 = esc(b.baseRepercutido4);
        c03 = cuota(b.cuotaRepercutido4, c01, "4");
        c04 = esc(b.baseRepercutido10);
        c06 = cuota(b.cuotaRepercutido10, c04, "10");
        c07 = esc(b.baseRepercutido21);
        c09 = cuota(b.cuotaRepercutido21, c07, "21");
        c10 = esc(b.baseAdqIntracom);
        c11 = esc(b.cuotaAdqIntracom);
        c12 = esc(b.baseInversionSujetoPasivo);
        c13 = esc(b.cuotaInversionSujetoPasivo);
        c14 = esc(b.baseModificacion);
        c15 = esc(b.cuotaModificacion);
        c27 = c03.add(c06).add(c09).add(c11).add(c13).add(c15);

        // ── IVA deducible ────────────────────────────────────────────────────
        c28 = esc(b.baseSoportado4).add(esc(b.baseSoportado10)).add(esc(b.baseSoportado21));
        c29 = esc(b.cuotaSoportado4).add(esc(b.cuotaSoportado10)).add(esc(b.cuotaSoportado21));
        c30 = esc(b.baseInversion);
        c31 = esc(b.cuotaInversion);
        c36 = esc(b.baseAdqIntracomDeducible);
        c37 = esc(b.cuotaAdqIntracomDeducible);
        c40 = esc(b.baseRectificacionDeducciones);
        c41 = esc(b.cuotaRectificacionDeducciones);
        c45 = c29.add(c31).add(c37).add(c41);
        c46 = c27.subtract(c45);

        // ── Información adicional ────────────────────────────────────────────
        c59  = esc(b.entregasIntracomunitarias);
        c60  = esc(b.exportaciones);
        c120 = esc(b.noSujetasLocalizacion);
        c122 = esc(b.sujetasInversionSujetoPasivo);

        // ── Resultado ────────────────────────────────────────────────────────
        c64 = c46;
        c65 = new BigDecimal("100.00");
        c66 = c64.multiply(c65).divide(CIEN, 2, RoundingMode.HALF_UP);
        c77 = BigDecimal.ZERO.setScale(2);
        c110 = esc(b.cuotasCompensarPendientes);
        BigDecimal positivo = max0(c66.add(c77));
        c78 = min(c110, positivo);
        c87 = c110.subtract(c78);
        c69 = c66.add(c77).subtract(c78);
        c70 = rectificativa ? esc(b.resultadoAnteriorIngresado) : BigDecimal.ZERO.setScale(2);
        c71 = c69.subtract(c70);

        // ── Tipo de declaración ──────────────────────────────────────────────
        if (c71.signum() == 0) {
            tipoDeclaracion = TipoDeclaracion.SIN_ACTIVIDAD;
        } else if (c71.signum() > 0) {
            tipoDeclaracion = iban.isEmpty() ? TipoDeclaracion.INGRESO : TipoDeclaracion.DOMICILIACION;
        } else if (periodo == Periodo.CUARTO_TRIMESTRE && solicitarDevolucion) {
            if (iban.isEmpty()) throw new IllegalStateException("La devolución requiere IBAN");
            tipoDeclaracion = TipoDeclaracion.DEVOLUCION;
        } else {
            tipoDeclaracion = TipoDeclaracion.COMPENSAR;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────────

    public String getNombreFichero() {
        return nif + ejercicio + periodo.getCodigo() + ".303";
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
        return cabecera() + aux() + pagina01() + pagina03() + paginaDID() + cierre();
    }

    public BigDecimal getResultado()            { return c71; }
    public TipoDeclaracion getTipoDeclaracion() { return tipoDeclaracion; }
    public BigDecimal getCuotasPendientesCompensar() {
        // Si el resultado es "a compensar", lo que queda para el siguiente trimestre es [87] + |[71]|
        return tipoDeclaracion == TipoDeclaracion.COMPENSAR ? c87.add(c71.abs()) : c87;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloques
    // ─────────────────────────────────────────────────────────────────────────

    private String cabecera() {
        return comprobar("<T3030" + ejercicio + periodo.getCodigo() + "0000>", LEN_CABECERA, "cabecera");
    }

    private String aux() {
        Reg r = new Reg();
        r.txt("<AUX>");
        r.blancos(70);                        // Reservado AEAT
        r.alfa(versionPrograma, 4);           // Versión del programa
        r.blancos(4);                         // Reservado AEAT
        r.alfa(nifDesarrollador, 9);          // NIF empresa de desarrollo
        r.blancos(213);                       // Reservado AEAT
        r.txt("</AUX>");
        return comprobar(r.toString(), LEN_AUX, "AUX");
    }

    private String pagina01() {
        boolean cuartoTrimestre = periodo == Periodo.CUARTO_TRIMESTRE;
        Reg r = new Reg();
        r.txt("<T30301000>");                                  // 1-11
        r.blancos(1);                                          // 12  Indicador página complementaria
        r.txt(String.valueOf(tipoDeclaracion.getCodigo()));    // 13  Tipo de declaración
        r.alfa(nif, 9);                                        // 14  NIF
        r.alfa(apellidosNombre, 80);                           // 23  Apellidos y nombre / razón social
        r.txt(String.valueOf(ejercicio));                      // 103 Ejercicio
        r.txt(periodo.getCodigo());                            // 107 Período
        r.txt("2");                                            // 109 Tributación exclusivamente foral: NO
        r.txt("2");                                            // 110 Inscrito en REDEME: NO
        r.txt("3");                                            // 111 Tributa exclusivamente en territorio común
        r.txt("2");                                            // 112 Autoliquidación conjunta: NO
        r.txt("2");                                            // 113 Criterio de caja: NO
        r.txt("2");                                            // 114 Destinatario criterio de caja: NO
        r.txt("2");                                            // 115 Opción prorrata especial: NO
        r.txt("2");                                            // 116 Revocación prorrata especial: NO
        r.txt("2");                                            // 117 Declarado en concurso: NO
        r.blancos(8);                                          // 118 Fecha auto de concurso
        r.blancos(1);                                          // 126 Tipo autoliquidación si concurso
        r.txt("2");                                            // 127 Acogido voluntariamente al SII: NO
        r.txt(cuartoTrimestre ? "2" : "0");                    // 128 Exonerado del 390 (solo 4T): NO
        r.txt("0");                                            // 129 Volumen de operaciones (solo exonerados)
        r.txt("0");                                            // 130 Pago a cuenta gasolinas (solo mensual)

        r.num(BigDecimal.ZERO);  r.txt("00000");  r.num(BigDecimal.ZERO);   // 131 [150][151][152] 0 %
        r.num(BigDecimal.ZERO);  r.txt("00000");  r.num(BigDecimal.ZERO);   // 170 [165][166][167] 2 %
        r.num(c01);              r.txt("00400");  r.num(c03);               // 209 [01][02][03]    4 %
        r.num(BigDecimal.ZERO);  r.txt("00000");  r.num(BigDecimal.ZERO);   // 248 [153][154][155] 5 %
        r.num(c04);              r.txt("01000");  r.num(c06);               // 287 [04][05][06]    10 %
        r.num(c07);              r.txt("02100");  r.num(c09);               // 326 [07][08][09]    21 %
        r.num(c10);  r.num(c11);                                            // 365 [10][11] adq. intracom.
        r.num(c12);  r.num(c13);                                            // 399 [12][13] inversión s.p.
        r.signo(c14); r.signo(c15);                                         // 433 [14][15] modificaciones
        r.num(BigDecimal.ZERO);  r.txt("00175");  r.num(BigDecimal.ZERO);   // 467 [156][157][158] RE 1,75 %
        r.num(BigDecimal.ZERO);  r.txt("00050");  r.num(BigDecimal.ZERO);   // 506 [168][169][170] RE 0,5 %
        r.num(BigDecimal.ZERO);  r.txt("00000");  r.num(BigDecimal.ZERO);   // 545 [16][17][18]    RE otros
        r.num(BigDecimal.ZERO);  r.txt("00140");  r.num(BigDecimal.ZERO);   // 584 [19][20][21]    RE 1,4 %
        r.num(BigDecimal.ZERO);  r.txt("00520");  r.num(BigDecimal.ZERO);   // 623 [22][23][24]    RE 5,2 %
        r.signo(BigDecimal.ZERO); r.signo(BigDecimal.ZERO);                 // 662 [25][26] modif. RE
        r.signo(c27);                                                       // 696 [27] total devengado

        r.num(c28);  r.num(c29);                                            // 713 [28][29] interiores corrientes
        r.num(c30);  r.num(c31);                                            // 747 [30][31] bienes de inversión
        r.num(BigDecimal.ZERO);  r.num(BigDecimal.ZERO);                    // 781 [32][33] importaciones corrientes
        r.num(BigDecimal.ZERO);  r.num(BigDecimal.ZERO);                    // 815 [34][35] importaciones inversión
        r.num(c36);  r.num(c37);                                            // 849 [36][37] adq. intracom. corrientes
        r.num(BigDecimal.ZERO);  r.num(BigDecimal.ZERO);                    // 883 [38][39] adq. intracom. inversión
        r.signo(c40); r.signo(c41);                                         // 917 [40][41] rectificación deducciones
        r.signo(BigDecimal.ZERO);                                           // 951 [42] compensaciones REAGP
        r.signo(BigDecimal.ZERO);                                           // 968 [43] regularización inversiones
        r.signo(BigDecimal.ZERO);                                           // 985 [44] prorrata definitiva
        r.signo(c45);                                                       // 1002 [45] total a deducir
        r.signo(c46);                                                       // 1019 [46] resultado régimen general

        r.blancos(521);                                        // 1036 Reservado AEAT
        r.blancos(13);                                         // 1557 Sello electrónico
        r.txt("</T30301000>");                                 // 1570
        return comprobar(r.toString(), LEN_PAG01, "página 01");
    }

    private String pagina03() {
        Reg r = new Reg();
        r.txt("<T30303000>");                                  // 1-11
        r.signo(c59);                                          // 12  [59] entregas intracomunitarias
        r.signo(c60);                                          // 29  [60] exportaciones
        r.signo(c120);                                         // 46  [120] no sujetas por localización
        r.signo(c122);                                         // 63  [122] sujetas con inversión s.p.
        r.signo(BigDecimal.ZERO);                              // 80  [123] OSS no sujetas
        r.signo(BigDecimal.ZERO);                              // 97  [124] OSS sujetas
        r.signo(BigDecimal.ZERO);                              // 114 [62] criterio de caja – base
        r.signo(BigDecimal.ZERO);                              // 131 [63] criterio de caja – cuota
        r.signo(BigDecimal.ZERO);                              // 148 [74] adq. criterio de caja – base
        r.signo(BigDecimal.ZERO);                              // 165 [75] adq. criterio de caja – cuota
        r.signo(BigDecimal.ZERO);                              // 182 [76] regularización art. 80.cinco.5ª
        r.signo(c64);                                          // 199 [64] suma de resultados
        r.porcentaje(c65);                                     // 216 [65] % atribuible al Estado
        r.signo(c66);                                          // 221 [66] atribuible al Estado
        r.num(c77);                                            // 238 [77] IVA importación Aduana
        r.num(c110);                                           // 255 [110] cuotas a compensar pendientes
        r.num(c78);                                            // 272 [78] cuotas compensadas este periodo
        r.num(c87);                                            // 289 [87] pendientes para periodos posteriores
        r.signo(BigDecimal.ZERO);                              // 306 [68] regularización anual (forales)
        r.signo(BigDecimal.ZERO);                              // 323 [108] otros ajustes
        r.signo(c69);                                          // 340 [69] resultado autoliquidación
        r.signo(c70);                                          // 357 [70] resultados de anteriores autoliq.
        r.signo(BigDecimal.ZERO);                              // 374 [109] devoluciones acordadas
        r.signo(BigDecimal.ZERO);                              // 391 [112] pago a cuenta gasolinas
        r.signo(c71);                                          // 408 [71] resultado de la liquidación
        r.marca(tipoDeclaracion == TipoDeclaracion.SIN_ACTIVIDAD); // 425 Declaración sin actividad
        r.marca(rectificativa);                                // 426 Autoliquidación rectificativa
        r.alfa(rectificativa ? justificanteAnterior : "", 13); // 427 Nº justificante anterior
        r.marca(false);                                        // 440 Baja/modificación domiciliación
        r.signo(BigDecimal.ZERO);                              // 441 [111] rectificación – importe
        r.marca(rectificativa);                                // 458 Motivo: rectificaciones
        r.marca(false);                                        // 459 Motivo: discrepancia criterio adm.
        r.blancos(546);                                        // 460 Reservado AEAT
        r.txt("</T30303000>");                                 // 1006
        return comprobar(r.toString(), LEN_PAG03, "página 03");
    }

    private String paginaDID() {
        boolean conCuenta = tipoDeclaracion == TipoDeclaracion.DOMICILIACION
                         || tipoDeclaracion == TipoDeclaracion.DEVOLUCION;
        Reg r = new Reg();
        r.txt("<T303DID00>");                                  // 1-11
        r.blancos(11);                                         // 12  SWIFT-BIC (solo cuentas extranjeras)
        r.alfa(conCuenta ? iban : "", 34);                     // 23  IBAN
        r.blancos(70);                                         // 57  Nombre del banco (extranjero)
        r.blancos(35);                                         // 127 Dirección del banco (extranjero)
        r.blancos(30);                                         // 162 Ciudad (extranjero)
        r.blancos(2);                                          // 192 Código país (extranjero)
        r.txt(tipoDeclaracion == TipoDeclaracion.DEVOLUCION ? "1" : "0"); // 194 Marca SEPA
        r.blancos(617);                                        // 195 Reservado AEAT
        r.txt("</T303DID00>");                                 // 812
        return comprobar(r.toString(), LEN_DID, "página DID");
    }

    private String cierre() {
        return comprobar("</T3030" + ejercicio + periodo.getCodigo() + "0000>", LEN_CIERRE, "cierre");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Escritor de registro con las reglas de formato de la AEAT
    // ─────────────────────────────────────────────────────────────────────────
    private static final class Reg {
        private final StringBuilder sb = new StringBuilder();

        void txt(String s)         { sb.append(s); }
        void blancos(int n)        { sb.append(" ".repeat(n)); }
        void marca(boolean v)      { sb.append(v ? "X" : " "); }

        /** Alfanumérico: mayúsculas, sin tildes (conserva Ñ y Ç), a la izquierda con blancos. */
        void alfa(String texto, int longitud) {
            String t = limpiarTexto(texto);
            sb.append(t.length() >= longitud ? t.substring(0, longitud) : t + " ".repeat(longitud - t.length()));
        }

        /** Importe sin signo: 17 dígitos (15 enteros + 2 decimales), ceros a la izquierda. */
        void num(BigDecimal v) {
            BigDecimal x = esc(v);
            if (x.signum() < 0) throw new IllegalStateException("Importe negativo en casilla sin signo: " + x);
            sb.append(String.format("%017d", x.movePointRight(2).longValueExact()));
        }

        /** Importe con signo: positivo como num(); negativo "N" + 16 dígitos. */
        void signo(BigDecimal v) {
            BigDecimal x = esc(v);
            long c = x.abs().movePointRight(2).longValueExact();
            sb.append(x.signum() < 0 ? "N" + String.format("%016d", c) : String.format("%017d", c));
        }

        /** Porcentaje de 5 posiciones: 3 enteros + 2 decimales (100 % → "10000"). */
        void porcentaje(BigDecimal v) {
            sb.append(String.format("%05d", esc(v).movePointRight(2).longValueExact()));
        }

        @Override public String toString() { return sb.toString(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

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

    /** Si no se informa la cuota, se calcula como base × tipo. */
    private static BigDecimal cuota(BigDecimal cuota, BigDecimal base, String tipo) {
        if (cuota != null) return esc(cuota);
        return base.multiply(new BigDecimal(tipo)).divide(CIEN, 2, RoundingMode.HALF_UP);
    }

    private static String comprobar(String bloque, int esperado, String nombre) {
        if (bloque.length() != esperado) {
            throw new IllegalStateException("Bloque " + nombre + " con longitud "
                    + bloque.length() + " (esperado " + esperado + ")");
        }
        return bloque;
    }

    private static String nz(String s)             { return s == null ? "" : s; }
    private static BigDecimal esc(BigDecimal v)    { return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP); }
    private static BigDecimal max0(BigDecimal v)   { return v.signum() > 0 ? v : BigDecimal.ZERO.setScale(2); }
    private static BigDecimal min(BigDecimal a, BigDecimal b) { return a.compareTo(b) <= 0 ? a : b; }

    // ─────────────────────────────────────────────────────────────────────────
    // Builder
    // ─────────────────────────────────────────────────────────────────────────
    public static class Builder {
        private String  nif       = "";
        private String  apellidos = "";
        private String  nombre    = "";
        private int     ejercicio = java.time.Year.now().getValue();
        private Periodo periodo   = Periodo.PRIMER_TRIMESTRE;

        // Devengado (cuota null = se calcula base × tipo)
        private BigDecimal baseRepercutido4,  cuotaRepercutido4;
        private BigDecimal baseRepercutido10, cuotaRepercutido10;
        private BigDecimal baseRepercutido21, cuotaRepercutido21;
        private BigDecimal baseAdqIntracom, cuotaAdqIntracom;
        private BigDecimal baseInversionSujetoPasivo, cuotaInversionSujetoPasivo;
        private BigDecimal baseModificacion, cuotaModificacion;

        // Deducible
        private BigDecimal baseSoportado4,  cuotaSoportado4;
        private BigDecimal baseSoportado10, cuotaSoportado10;
        private BigDecimal baseSoportado21, cuotaSoportado21;
        private BigDecimal baseInversion, cuotaInversion;
        private BigDecimal baseAdqIntracomDeducible, cuotaAdqIntracomDeducible;
        private BigDecimal baseRectificacionDeducciones, cuotaRectificacionDeducciones;

        // Información adicional
        private BigDecimal entregasIntracomunitarias, exportaciones, noSujetasLocalizacion, sujetasInversionSujetoPasivo;

        // Resultado
        private BigDecimal cuotasCompensarPendientes;
        private boolean    rectificativa = false;
        private String     justificanteAnterior = "";
        private BigDecimal resultadoAnteriorIngresado;
        private String     iban = "";
        private boolean    solicitarDevolucion = false;

        private String versionPrograma  = "LS01";
        private String nifDesarrollador = "";

        public Builder nif(String v)        { this.nif = v == null ? "" : v.toUpperCase(Locale.ROOT).replaceAll("[\\s-]", ""); return this; }
        /** Apellidos (persona física) o razón social. */
        public Builder apellidos(String v)  { this.apellidos = v; return this; }
        /** Nombre de pila (persona física). */
        public Builder nombre(String v)     { this.nombre = v; return this; }
        public Builder ejercicio(int v)     { this.ejercicio = v; return this; }
        public Builder periodo(Periodo v)   { this.periodo = v; return this; }

        // [01]-[09]
        public Builder baseIvaRepercutido4(BigDecimal v)   { this.baseRepercutido4 = v; return this; }
        public Builder cuotaIvaRepercutido4(BigDecimal v)  { this.cuotaRepercutido4 = v; return this; }
        public Builder baseIvaRepercutido10(BigDecimal v)  { this.baseRepercutido10 = v; return this; }
        public Builder cuotaIvaRepercutido10(BigDecimal v) { this.cuotaRepercutido10 = v; return this; }
        public Builder baseIvaRepercutido21(BigDecimal v)  { this.baseRepercutido21 = v; return this; }
        public Builder cuotaIvaRepercutido21(BigDecimal v) { this.cuotaRepercutido21 = v; return this; }
        /** [10][11] Adquisiciones intracomunitarias (autorrepercusión). */
        public Builder adquisicionesIntracomunitarias(BigDecimal base, BigDecimal cuota) { this.baseAdqIntracom = base; this.cuotaAdqIntracom = cuota; return this; }
        /** [12][13] Otras operaciones con inversión del sujeto pasivo. */
        public Builder inversionSujetoPasivo(BigDecimal base, BigDecimal cuota) { this.baseInversionSujetoPasivo = base; this.cuotaInversionSujetoPasivo = cuota; return this; }
        /** [14][15] Modificación de bases y cuotas (facturas rectificativas emitidas; pueden ser negativas). */
        public Builder modificacionBasesCuotas(BigDecimal base, BigDecimal cuota) { this.baseModificacion = base; this.cuotaModificacion = cuota; return this; }

        // [28][29]
        public Builder baseIvaSoportado4(BigDecimal v)   { this.baseSoportado4 = v; return this; }
        public Builder cuotaIvaSoportado4(BigDecimal v)  { this.cuotaSoportado4 = v; return this; }
        public Builder baseIvaSoportado10(BigDecimal v)  { this.baseSoportado10 = v; return this; }
        public Builder cuotaIvaSoportado10(BigDecimal v) { this.cuotaSoportado10 = v; return this; }
        public Builder baseIvaSoportado21(BigDecimal v)  { this.baseSoportado21 = v; return this; }
        public Builder cuotaIvaSoportado21(BigDecimal v) { this.cuotaSoportado21 = v; return this; }
        /** [30][31] Bienes de inversión. */
        public Builder baseInversion21(BigDecimal v)     { this.baseInversion = v; return this; }
        public Builder cuotaInversion21(BigDecimal v)    { this.cuotaInversion = v; return this; }
        /** [36][37] Adquisiciones intracomunitarias deducibles. */
        public Builder adquisicionesIntracomunitariasDeducibles(BigDecimal base, BigDecimal cuota) { this.baseAdqIntracomDeducible = base; this.cuotaAdqIntracomDeducible = cuota; return this; }
        /** [40][41] Rectificación de deducciones (facturas rectificativas recibidas; pueden ser negativas). */
        public Builder rectificacionDeducciones(BigDecimal base, BigDecimal cuota) { this.baseRectificacionDeducciones = base; this.cuotaRectificacionDeducciones = cuota; return this; }

        // Información adicional
        public Builder entregasIntracomunitarias(BigDecimal v)     { this.entregasIntracomunitarias = v; return this; }
        public Builder exportaciones(BigDecimal v)                 { this.exportaciones = v; return this; }
        public Builder noSujetasLocalizacion(BigDecimal v)         { this.noSujetasLocalizacion = v; return this; }
        public Builder sujetasInversionSujetoPasivo(BigDecimal v)  { this.sujetasInversionSujetoPasivo = v; return this; }

        /** [110] Cuotas a compensar de periodos anteriores (saldo negativo pendiente). */
        public Builder cuotasCompensarPendientes(BigDecimal v) { this.cuotasCompensarPendientes = v; return this; }
        /** Autoliquidación rectificativa: justificante anterior y [70] lo ya ingresado. */
        public Builder rectificativa(String justificanteAnterior, BigDecimal resultadoAnteriorIngresado) {
            this.rectificativa = true;
            this.justificanteAnterior = justificanteAnterior;
            this.resultadoAnteriorIngresado = resultadoAnteriorIngresado;
            return this;
        }
        /** IBAN para domiciliar el ingreso o recibir la devolución. */
        public Builder iban(String v)                 { this.iban = v; return this; }
        /** Solo 4T: si sale negativo, pedir devolución en vez de compensar. */
        public Builder solicitarDevolucion(boolean v) { this.solicitarDevolucion = v; return this; }
        public Builder versionPrograma(String v)      { this.versionPrograma = v; return this; }
        public Builder nifDesarrollador(String v)     { this.nifDesarrollador = v; return this; }

        public Modelo303BoeGenerator build() {
            if (nif.length() != 9)
                throw new IllegalStateException("NIF obligatorio de 9 caracteres: '" + nif + "'");
            if ((apellidos == null || apellidos.isBlank()) && (nombre == null || nombre.isBlank()))
                throw new IllegalStateException("Apellidos y nombre o razón social obligatorios");
            if (ejercicio < 2026)
                throw new IllegalStateException("Este diseño es para el ejercicio 2026 y siguientes");
            if (periodo == null)
                throw new IllegalStateException("Período obligatorio");
            if (rectificativa && (justificanteAnterior == null || justificanteAnterior.length() != 13))
                throw new IllegalStateException("La rectificativa requiere el justificante anterior de 13 dígitos");
            if (solicitarDevolucion && periodo != Periodo.CUARTO_TRIMESTRE)
                throw new IllegalStateException("En liquidación trimestral solo se puede pedir devolución en el 4T");
            return new Modelo303BoeGenerator(this);
        }
    }
}