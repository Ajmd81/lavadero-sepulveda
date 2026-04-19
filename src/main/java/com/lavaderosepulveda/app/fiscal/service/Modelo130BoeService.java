package com.lavaderosepulveda.app.fiscal.service;

import com.lavaderosepulveda.app.fiscal.generators.Modelo130BoeGenerator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;

/**
 * Servicio para generar el fichero BOE del Modelo 130
 * (IRPF — Pago fraccionado, autónomos en estimación directa).
 *
 * La lógica de cálculo que aplica el 20% sobre el rendimiento neto positivo
 * está en el propio {@link Modelo130BoeGenerator}; este servicio sólo hace
 * de puente entre el controlador REST y el generador.
 */
@Service
public class Modelo130BoeService {

    private static final Charset ISO = Charset.forName("ISO-8859-1");

    // ─────────────────────────────────────────────────────────────────────────
    // Generación
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Genera el fichero BOE del Modelo 130 y devuelve su contenido en bytes ISO-8859-1.
     *
     * @param nif                    NIF del declarante (9 chars)
     * @param nombre                 Apellidos y nombre / razón social
     * @param ejercicio              Año fiscal (ej: 2026)
     * @param periodo                Período: "1T", "2T", "3T", "4T"
     * @param ingresosTrimestre      Ingresos computables acumulados hasta el trimestre (casilla 01)
     * @param gastosTrimestre        Gastos deducibles acumulados hasta el trimestre (casilla 02)
     * @param retencionesComputadas  Retenciones soportadas acumuladas (casilla 11) — habitualmente 0
     * @param pagosAnteriores        Pagos fraccionados ingresados en trimestres anteriores (casilla 13)
     * @return                       Bytes del fichero en codificación ISO-8859-1
     */
    public byte[] generarFicheroBoe(
            String nif,
            String nombre,
            int ejercicio,
            String periodo,
            BigDecimal ingresosTrimestre,
            BigDecimal gastosTrimestre,
            BigDecimal retencionesComputadas,
            BigDecimal pagosAnteriores
    ) throws IOException {

        Modelo130BoeGenerator generator = new Modelo130BoeGenerator.Builder()
                .nif(nif)
                .nombre(nombre)
                .ejercicio(ejercicio)
                .periodo(parsePeriodo(periodo))
                .ingresosTrimestre(orCero(ingresosTrimestre))
                .gastosTrimestre(orCero(gastosTrimestre))
                .retencionesComputadas(orCero(retencionesComputadas))
                .pagosAnteriores(orCero(pagosAnteriores))
                .build();

        return generator.generarContenido().getBytes(ISO);
    }

    /**
     * Sobrecarga cómoda para el caso más habitual de Lavadero Sepúlveda:
     * sin retenciones y siendo el primer trimestre del año (sin pagos anteriores).
     */
    public byte[] generarFicheroBoe(
            String nif,
            String nombre,
            int ejercicio,
            String periodo,
            BigDecimal ingresosTrimestre,
            BigDecimal gastosTrimestre
    ) throws IOException {
        return generarFicheroBoe(
                nif, nombre, ejercicio, periodo,
                ingresosTrimestre, gastosTrimestre,
                BigDecimal.ZERO, BigDecimal.ZERO
        );
    }

    /**
     * Variante con DTO completo (útil para el endpoint POST).
     */
    public byte[] generarFicheroBoeCompleto(Modelo130Datos datos) throws IOException {
        return generarFicheroBoe(
                datos.nif, datos.nombre, datos.ejercicio, datos.periodo,
                datos.ingresosTrimestre, datos.gastosTrimestre,
                datos.retencionesComputadas, datos.pagosAnteriores
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DTO
    // ─────────────────────────────────────────────────────────────────────────

    /** DTO de entrada para el endpoint POST de exportación. */
    public static class Modelo130Datos {
        public String nif;
        public String nombre;
        public int    ejercicio;
        public String periodo;                    // "1T" | "2T" | "3T" | "4T"

        /** Casilla 01 — Ingresos computables acumulados. */
        public BigDecimal ingresosTrimestre;

        /** Casilla 02 — Gastos deducibles acumulados. */
        public BigDecimal gastosTrimestre;

        /** Casilla 11 — Retenciones soportadas acumuladas (normalmente 0). */
        public BigDecimal retencionesComputadas;

        /** Casilla 13 — Pagos fraccionados de trimestres anteriores (0 en 1T). */
        public BigDecimal pagosAnteriores;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Modelo130BoeGenerator.Periodo parsePeriodo(String p) {
        return switch (p.toUpperCase().trim()) {
            case "1T" -> Modelo130BoeGenerator.Periodo.PRIMER_TRIMESTRE;
            case "2T" -> Modelo130BoeGenerator.Periodo.SEGUNDO_TRIMESTRE;
            case "3T" -> Modelo130BoeGenerator.Periodo.TERCER_TRIMESTRE;
            case "4T" -> Modelo130BoeGenerator.Periodo.CUARTO_TRIMESTRE;
            default   -> throw new IllegalArgumentException("Período no válido: " + p + ". Use 1T, 2T, 3T o 4T");
        };
    }

    private BigDecimal orCero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
