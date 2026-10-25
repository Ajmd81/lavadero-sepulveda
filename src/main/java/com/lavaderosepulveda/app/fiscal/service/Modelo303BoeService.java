package com.lavaderosepulveda.app.fiscal.service;

import com.lavaderosepulveda.app.fiscal.generators.Modelo303BoeGenerator;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Servicio para generar el fichero de importación del Modelo 303 (IVA – Autoliquidación).
 *
 * Ofrece dos variantes:
 *   - generar()          → caso habitual de Lavadero Sepúlveda (solo 21 %)
 *   - generarCompleto()  → varios tipos (4 %, 10 %, 21 %) y bienes de inversión
 *
 * Además de los bytes del fichero devuelve el resultado calculado por el generador,
 * para guardarlo en el historial de declaraciones.
 */
@Service
public class Modelo303BoeService {

    // ─────────────────────────────────────────────────────────────────────────
    // Resultado
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fichero generado y cifras calculadas por el generador.
     *
     * @param contenido                 Bytes del fichero (ISO-8859-1)
     * @param nombreFichero             {NIF}{EJERCICIO}{PERIODO}.303
     * @param resultado                 Casilla [71], resultado de la liquidación
     * @param tipoDeclaracion           I, U, C, D o N
     * @param cuotasPendientesCompensar Saldo a compensar que queda para el siguiente trimestre
     */
    public record Resultado303(
            byte[] contenido,
            String nombreFichero,
            BigDecimal resultado,
            String tipoDeclaracion,
            BigDecimal cuotasPendientesCompensar
    ) {}

    // ─────────────────────────────────────────────────────────────────────────
    // Variante simple (solo tipo 21 % — régimen general)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Genera el 303 simplificado (solo IVA al 21 %).
     *
     * @param nif                        NIF del declarante (9 chars)
     * @param nombre                     Nombre de pila, o apellidos y nombre completos si {@code apellidos} es null
     * @param apellidos                  Apellidos (opcional)
     * @param ejercicio                  Año fiscal (ej: 2026)
     * @param periodo                    "1T", "2T", "3T" o "4T"
     * @param baseRep21                  [07] Base IVA repercutido 21 %
     * @param cuotaRep21                 [09] Cuota IVA repercutido 21 % (null = base × 21 %)
     * @param baseSop21                  [28] Base IVA soportado corriente
     * @param cuotaSop21                 [29] Cuota IVA soportado deducible
     * @param cuotasCompensarPendientes  [110] Saldo a compensar de trimestres anteriores
     * @param iban                       IBAN para domiciliar el ingreso (opcional)
     */
    public Resultado303 generar(
            String nif, String nombre, String apellidos, int ejercicio, String periodo,
            BigDecimal baseRep21, BigDecimal cuotaRep21,
            BigDecimal baseSop21, BigDecimal cuotaSop21,
            BigDecimal cuotasCompensarPendientes, String iban
    ) {
        Modelo303BoeGenerator generator = new Modelo303BoeGenerator.Builder()
                .nif(nif)
                .apellidos(apellidos)
                .nombre(nombre)
                .ejercicio(ejercicio)
                .periodo(parsePeriodo(periodo))
                .baseIvaRepercutido21(baseRep21)
                .cuotaIvaRepercutido21(cuotaRep21)
                .baseIvaSoportado21(baseSop21)
                .cuotaIvaSoportado21(cuotaSop21)
                .cuotasCompensarPendientes(orCero(cuotasCompensarPendientes))
                .iban(iban)
                .build();

        return toResultado(generator);
    }

    /** Solo los bytes del fichero (compatibilidad). */
    public byte[] generarFicheroBoe(
            String nif, String nombre, int ejercicio, String periodo,
            BigDecimal baseRep21, BigDecimal cuotaRep21,
            BigDecimal baseSop21, BigDecimal cuotaSop21
    ) throws IOException {
        return generar(nif, nombre, null, ejercicio, periodo,
                baseRep21, cuotaRep21, baseSop21, cuotaSop21, BigDecimal.ZERO, null).contenido();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Variante completa (todos los tipos + bienes de inversión)
    // ─────────────────────────────────────────────────────────────────────────

    /** Genera el 303 completo a partir del DTO {@link Modelo303Datos}. */
    public Resultado303 generarCompleto(Modelo303Datos datos) {

        Modelo303BoeGenerator generator = new Modelo303BoeGenerator.Builder()
                .nif(datos.nif)
                .apellidos(datos.apellidos)
                .nombre(datos.nombre)
                .ejercicio(datos.ejercicio)
                .periodo(parsePeriodo(datos.periodo))
                // Repercutido
                .baseIvaRepercutido4(orCero(datos.baseRep4))
                .cuotaIvaRepercutido4(orCero(datos.cuotaRep4))
                .baseIvaRepercutido10(orCero(datos.baseRep10))
                .cuotaIvaRepercutido10(orCero(datos.cuotaRep10))
                .baseIvaRepercutido21(orCero(datos.baseRep21))
                .cuotaIvaRepercutido21(orCero(datos.cuotaRep21))
                // Soportado corrientes
                .baseIvaSoportado4(orCero(datos.baseSop4))
                .cuotaIvaSoportado4(orCero(datos.cuotaSop4))
                .baseIvaSoportado10(orCero(datos.baseSop10))
                .cuotaIvaSoportado10(orCero(datos.cuotaSop10))
                .baseIvaSoportado21(orCero(datos.baseSop21))
                .cuotaIvaSoportado21(orCero(datos.cuotaSop21))
                // Bienes de inversión
                .baseInversion21(orCero(datos.baseInversion))
                .cuotaInversion21(orCero(datos.cuotaInversion))
                // Compensación y forma de pago
                .cuotasCompensarPendientes(orCero(datos.cuotasCompensarPendientes))
                .iban(datos.iban)
                .solicitarDevolucion(datos.solicitarDevolucion)
                .build();

        return toResultado(generator);
    }

    /** Solo los bytes del fichero a partir del DTO (compatibilidad). */
    public byte[] generarFicheroBoeCompleto(Modelo303Datos datos) throws IOException {
        return generarCompleto(datos).contenido();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DTO
    // ─────────────────────────────────────────────────────────────────────────

    /** DTO de entrada para el endpoint POST de exportación completa. */
    public static class Modelo303Datos {
        public String nif;
        /** Nombre de pila, o apellidos y nombre completos si no se envía {@code apellidos}. */
        public String nombre;
        /** Opcional: apellidos por separado. */
        public String apellidos;
        public int    ejercicio;
        public String periodo;          // "1T" | "2T" | "3T" | "4T"

        // Repercutido (ventas)
        public BigDecimal baseRep4,  cuotaRep4;
        public BigDecimal baseRep10, cuotaRep10;
        public BigDecimal baseRep21, cuotaRep21;

        // Soportado (gastos corrientes)
        public BigDecimal baseSop4,  cuotaSop4;
        public BigDecimal baseSop10, cuotaSop10;
        public BigDecimal baseSop21, cuotaSop21;

        // Bienes de inversión
        public BigDecimal baseInversion, cuotaInversion;

        /** [110] Cuotas a compensar pendientes de trimestres anteriores. */
        public BigDecimal cuotasCompensarPendientes;

        /** Opcional: IBAN para domiciliar el ingreso o recibir la devolución. */
        public String iban;

        /** Solo 4T: pedir devolución si el resultado es negativo (requiere IBAN). */
        public boolean solicitarDevolucion;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Resultado303 toResultado(Modelo303BoeGenerator generator) {
        return new Resultado303(
                generator.generarBytes(),
                generator.getNombreFichero(),
                generator.getResultado(),
                String.valueOf(generator.getTipoDeclaracion().getCodigo()),
                generator.getCuotasPendientesCompensar()
        );
    }

    private Modelo303BoeGenerator.Periodo parsePeriodo(String p) {
        if (p == null) throw new IllegalArgumentException("Período obligatorio. Use 1T, 2T, 3T o 4T");
        return switch (p.toUpperCase().trim()) {
            case "1T" -> Modelo303BoeGenerator.Periodo.PRIMER_TRIMESTRE;
            case "2T" -> Modelo303BoeGenerator.Periodo.SEGUNDO_TRIMESTRE;
            case "3T" -> Modelo303BoeGenerator.Periodo.TERCER_TRIMESTRE;
            case "4T" -> Modelo303BoeGenerator.Periodo.CUARTO_TRIMESTRE;
            default   -> throw new IllegalArgumentException("Período no válido: " + p + ". Use 1T, 2T, 3T o 4T");
        };
    }

    private BigDecimal orCero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}