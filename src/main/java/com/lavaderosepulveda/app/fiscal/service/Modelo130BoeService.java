package com.lavaderosepulveda.app.fiscal.service;

import com.lavaderosepulveda.app.fiscal.generators.Modelo130BoeGenerator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio para generar el fichero de importación del Modelo 130
 * (IRPF — Pago fraccionado, autónomos en estimación directa).
 *
 * El cálculo de las casillas está en {@link Modelo130BoeGenerator};
 * este servicio hace de puente entre el controlador REST y el generador
 * y devuelve también los importes calculados para guardarlos en el historial.
 */
@Service
public class Modelo130BoeService {

    // ─────────────────────────────────────────────────────────────────────────
    // Resultado
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fichero generado y cifras calculadas por el generador.
     *
     * @param contenido        Bytes del fichero (ISO-8859-1)
     * @param nombreFichero    {NIF}{EJERCICIO}{PERIODO}.130
     * @param rendimientoNeto  Casilla [03] = [01] - [02] (puede ser negativa)
     * @param pagoFraccionado  Casilla [19], resultado de la declaración (lo que se ingresa)
     * @param tipoDeclaracion  I (ingreso), U (domiciliación), N (negativa) o B (a deducir)
     */
    public record Resultado130(
            byte[] contenido,
            String nombreFichero,
            BigDecimal rendimientoNeto,
            BigDecimal pagoFraccionado,
            String tipoDeclaracion
    ) {}

    // ─────────────────────────────────────────────────────────────────────────
    // Generación
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Genera el Modelo 130.
     *
     * @param nif               NIF del declarante (9 chars)
     * @param nombre            Nombre de pila, o apellidos y nombre completos si {@code apellidos} es null
     * @param apellidos         Apellidos (opcional)
     * @param ejercicio         Año fiscal (ej: 2026)
     * @param periodo           "1T", "2T", "3T" o "4T"
     * @param ingresos          [01] Ingresos ACUMULADOS desde el 1 de enero
     * @param gastos            [02] Gastos deducibles ACUMULADOS desde el 1 de enero
     * @param retenciones       [06] Retenciones soportadas acumuladas (habitualmente 0)
     * @param pagosAnteriores   [05] Suma de los pagos fraccionados positivos de trimestres anteriores
     */
    public Resultado130 generar(
            String nif,
            String nombre,
            String apellidos,
            int ejercicio,
            String periodo,
            BigDecimal ingresos,
            BigDecimal gastos,
            BigDecimal retenciones,
            BigDecimal pagosAnteriores
    ) {
        boolean sinApellidos = apellidos == null || apellidos.isBlank();

        Modelo130BoeGenerator generator = new Modelo130BoeGenerator.Builder()
                .nif(nif)
                .apellidos(sinApellidos ? nombre : apellidos)
                .nombre(sinApellidos ? "" : nombre)
                .ejercicio(ejercicio)
                .periodo(parsePeriodo(periodo))
                .ingresos(orCero(ingresos))
                .gastos(orCero(gastos))
                .retenciones(orCero(retenciones))
                .pagosFraccionadosAnteriores(orCero(pagosAnteriores))
                .build();

        BigDecimal rendimientoNeto = orCero(ingresos).subtract(orCero(gastos))
                .setScale(2, RoundingMode.HALF_UP);

        return new Resultado130(
                generator.generarBytes(),
                generator.getNombreFichero(),
                rendimientoNeto,
                generator.getResultado(),
                String.valueOf(generator.getTipoDeclaracion().getCodigo())
        );
    }

    /** Solo los bytes del fichero (compatibilidad). */
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
        return generar(nif, nombre, null, ejercicio, periodo,
                ingresosTrimestre, gastosTrimestre, retencionesComputadas, pagosAnteriores).contenido();
    }

    /** Solo los bytes del fichero, sin retenciones ni pagos anteriores (1T). */
    public byte[] generarFicheroBoe(
            String nif,
            String nombre,
            int ejercicio,
            String periodo,
            BigDecimal ingresosTrimestre,
            BigDecimal gastosTrimestre
    ) throws IOException {
        return generarFicheroBoe(nif, nombre, ejercicio, periodo,
                ingresosTrimestre, gastosTrimestre, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /** Variante con DTO completo (endpoint POST). */
    public Resultado130 generarCompleto(Modelo130Datos datos, BigDecimal pagosAnteriores) {
        return generar(
                datos.nif, datos.nombre, datos.apellidos, datos.ejercicio, datos.periodo,
                datos.ingresosTrimestre, datos.gastosTrimestre,
                datos.retencionesComputadas, pagosAnteriores
        );
    }

    /** Solo los bytes del fichero a partir del DTO (compatibilidad). */
    public byte[] generarFicheroBoeCompleto(Modelo130Datos datos) throws IOException {
        return generarCompleto(datos, datos.pagosAnteriores).contenido();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DTO
    // ─────────────────────────────────────────────────────────────────────────

    /** DTO de entrada para el endpoint POST de exportación. */
    public static class Modelo130Datos {
        public String nif;
        /** Nombre de pila, o apellidos y nombre completos si no se envía {@code apellidos}. */
        public String nombre;
        /** Opcional: apellidos por separado. */
        public String apellidos;
        public int    ejercicio;
        public String periodo;                    // "1T" | "2T" | "3T" | "4T"

        /** Casilla 01 — Ingresos computables acumulados desde el 1 de enero. */
        public BigDecimal ingresosTrimestre;

        /** Casilla 02 — Gastos deducibles acumulados desde el 1 de enero. */
        public BigDecimal gastosTrimestre;

        /** Casilla 06 — Retenciones soportadas acumuladas (normalmente 0). */
        public BigDecimal retencionesComputadas;

        /**
         * Casilla 05 — Pagos fraccionados de trimestres anteriores.
         * Si viene null se calcula a partir del historial de declaraciones.
         */
        public BigDecimal pagosAnteriores;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Modelo130BoeGenerator.Periodo parsePeriodo(String p) {
        if (p == null) throw new IllegalArgumentException("Período obligatorio. Use 1T, 2T, 3T o 4T");
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