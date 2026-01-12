package com.lavaderosepulveda.app.model;

import com.lavaderosepulveda.app.model.enums.TipoLavado;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TipoLavadoConverter implements AttributeConverter<TipoLavado, String> {

    @Override
    public String convertToDatabaseColumn(TipoLavado tipoLavado) {
        if (tipoLavado == null) {
            return null;
        }
        return tipoLavado.name().toLowerCase();
    }

    @Override
    public TipoLavado convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return TipoLavado.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Manejo de valores que no coinciden exactamente
            for (TipoLavado tipo : TipoLavado.values()) {
                if (dbData.equalsIgnoreCase(tipo.name()) ||
                        dbData.replace('_', ' ').equalsIgnoreCase(tipo.name().replace('_', ' '))) {
                    return tipo;
                }
            }
            throw e;
        }
    }
}