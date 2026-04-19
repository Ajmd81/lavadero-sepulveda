package com.lavaderosepulveda.app.fiscal.service;

import com.lavaderosepulveda.app.fiscal.generators.Modelo303BoeGenerator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;

/**
 * Servicio para generar el fichero BOE del Modelo 303 (IVA Autoliquidación).
 *
 * Ofrece dos variantes:
 *   - generarFicheroBoe()      → para el caso habitual de Lavadero Sepúlveda (solo 21%)
 *   - generarFicheroBoeCompleto() → para casos con múltiples tipos (4%, 10%, 21%) y bienes de inversión
 */
@Service
public class Modelo303BoeService {

    private static final Charset ISO = Charset.forName("ISO-8859-1");

    // ─────────────────────────────────────────────────────────────────────────
    // Variante simple (solo tipo 21% — régimen general)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Genera el fichero BOE simplificado (solo IVA al 21%) y devuelve los bytes.
     *
     * @param nif          NIF del declarante (9 chars)
     * @param nombre       Apellidos y nombre / razón social
     * @param ejercicio    Año fiscal (ej: 2026)
     * @param periodo      Período: "1T", "2T", "3T", "4T"
     * @param baseRep21    Base imponible IVA repercutido 21% (ventas)
     * @param cuotaRep21   Cuota IVA repercutido 21%
     * @param baseSop21    Base imponible IVA soportado 21% (gastos corrientes)
     * @param cuotaSop21   Cuota IVA soportado 21% deducible
     * @return             Bytes del fichero en codificación ISO-8859-1
     */
    public byte[] generarFicheroBoe(
            String nif, String nombre, int ejercicio, String periodo,
            BigDecimal baseRep21, BigDecimal cuotaRep21,
            BigDecimal baseSop21, BigDecimal cuotaSop21
    ) throws IOException {

        Modelo303BoeGenerator generator = new Modelo303BoeGenerator.Builder()
                .nif(nif)
                .nombre(nombre)
                .ejercicio(ejercicio)
                .periodo(parsePeriodo(periodo))
                .baseIvaRepercutido21(baseRep21)
                .cuotaIvaRepercutido21(cuotaRep21)
                .baseIvaSoportado21(baseSop21)
                .cuotaIvaSoportado21(cuotaSop21)
                .build();

        return generator.generarContenido().getBytes(ISO);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Variante completa (todos los tipos + bienes de inversión)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Genera el fichero BOE completo a partir del DTO {@link Modelo303Datos}.
     */
    public byte[] generarFicheroBoeCompleto(Modelo303Datos datos) throws IOException {

        Modelo303BoeGenerator generator = new Modelo303BoeGenerator.Builder()
                .nif(datos.nif)
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
                .build();

        return generator.generarContenido().getBytes(ISO);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DTO
    // ─────────────────────────────────────────────────────────────────────────

    /** DTO de entrada para el endpoint POST de exportación completa. */
    public static class Modelo303Datos {
        public String nif;
        public String nombre;
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
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Modelo303BoeGenerator.Periodo parsePeriodo(String p) {
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
