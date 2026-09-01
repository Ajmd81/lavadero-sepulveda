package com.lavaderosepulveda.app.model.enums;

/**
 * Enum para representar los días de la semana
 */
public enum DiaSemana {
    LUNES("Lunes", 0),
    MARTES("Martes", 1),
    MIERCOLES("Miércoles", 2),
    JUEVES("Jueves", 3),
    VIERNES("Viernes", 4),
    SABADO("Sábado", 5),
    DOMINGO("Domingo", 6);

    private final String displayName;
    private final int order;

    DiaSemana(String displayName, int order) {
        this.displayName = displayName;
        this.order = order;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getOrder() {
        return order;
    }

    /**
     * Obtiene el enum por el número de día (0=Lunes, 6=Domingo)
     */
    public static DiaSemana fromOrder(int order) {
        for (DiaSemana dia : values()) {
            if (dia.order == order) {
                return dia;
            }
        }
        throw new IllegalArgumentException("Orden inválido: " + order);
    }
}