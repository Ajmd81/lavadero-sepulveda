package com.lavaderosepulveda.app.model.enums;

/**
 * Modos de horario disponibles para el lavadero
 */
public enum ModoHorario {
    SOLO_MAÑANA("Solo Mañana"),
    SOLO_TARDE("Solo Tarde"),
    COMPLETO("Completo (Mañana y Tarde)");

    private final String displayName;

    ModoHorario(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}