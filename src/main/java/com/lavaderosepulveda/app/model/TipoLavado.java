package com.lavaderosepulveda.app.model;

import java.math.BigDecimal;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum TipoLavado {
    LAVADO_COMPLETO_TURISMO("Lavado Completo Turismo", new BigDecimal("23.0")),
    LAVADO_INTERIOR_TURISMO("Lavado Interior Turismo", new BigDecimal("16.0")),
    LAVADO_EXTERIOR_TURISMO("Lavado Exterior Turismo", new BigDecimal("12.0")),
    LAVADO_COMPLETO_RANCHERA("Lavado Completo Turismo Ranchera", new BigDecimal("26.0")),
    LAVADO_INTERIOR_RANCHERA("Lavado Interior Turismo Ranchera", new BigDecimal("18.0")),
    LAVADO_EXTERIOR_RANCHERA("Lavado Exterior Turismo Ranchera", new BigDecimal("13.0")),
    LAVADO_COMPLETO_MONOVOLUMEN("Lavado Completo Monovolumen/Todoterreno Pequeño", new BigDecimal("28.0")),
    LAVADO_INTERIOR_MONOVOLUMEN("Lavado Interior Monovolumen/Todoterreno Pequeño", new BigDecimal("19.0")),
    LAVADO_EXTERIOR_MONOVOLUMEN("Lavado Exterior Monovolumen/Todoterreno Pequeño", new BigDecimal("14.0")),
    LAVADO_COMPLETO_TODOTERRENO("Lavado Completo Todoterreno Grande", new BigDecimal("31.0")),
    LAVADO_INTERIOR_TODOTERRENO("Lavado Interior Todoterreno Grande", new BigDecimal("20.0")),
    LAVADO_EXTERIOR_TODOTERRENO("Lavado Exterior Todoterreno Grande", new BigDecimal("16.0")),
    LAVADO_COMPLETO_FURGONETA_PEQUEÑA("Lavado Completo Furgoneta Pequeña", new BigDecimal("30.0")),
    LAVADO_INTERIOR_FURGONETA_PEQUEÑA("Lavado Interior Furgoneta Pequeña", new BigDecimal("20.0")),
    LAVADO_EXTERIOR_FURGONETA_PEQUEÑA("Lavado Exterior Furgoneta Pequeña", new BigDecimal("15.0")),
    LAVADO_COMPLETO_FURGONETA_GRANDE("Lavado Completo Furgoneta Grande", new BigDecimal("35.0")),
    LAVADO_INTERIOR_FURGONETA_GRANDE("Lavado Interior Furgoneta Grande", new BigDecimal("25.0")),
    LAVADO_EXTERIOR_FURGONETA_GRANDE("Lavado Exterior Furgoneta Grande", new BigDecimal("20.0")),
    TRATAMIENTO_OZONO("Tratamiento de Ozono", new BigDecimal("15.0")),
    ENCERADO("Encerado de Vehículo a Mano", new BigDecimal("25.0")),
    TAPICERIA_SIN_DESMONTAR("Limpieza de tapicería sin desmontar asientos", new BigDecimal("100.0")),
    TAPICERIA_DESMONTANDO("Limpieza de tapicería desmontando asientos", new BigDecimal("150.0"));

    private final String descripcion;
    private final BigDecimal precio;

    TipoLavado(String descripcion, BigDecimal precio) {
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    @Override
    public String toString() {
        return descripcion + " (" + precio + "€)";
    }

    @Converter
    public static class TipoLavadoConverter implements AttributeConverter<TipoLavado, String> {
        @Override
        public String convertToDatabaseColumn(TipoLavado tipoLavado) {
            if (tipoLavado == null) {
                return null;
            }
            return tipoLavado.name();
        }

        @Override
        public TipoLavado convertToEntityAttribute(String code) {
            if (code == null) {
                return null;
            }
            return TipoLavado.valueOf(code);
        }
    }
}