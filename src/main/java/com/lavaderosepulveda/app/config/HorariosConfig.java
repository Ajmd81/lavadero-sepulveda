package com.lavaderosepulveda.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuración centralizada de horarios del lavadero
 * Permite modificar horarios desde application.yml sin cambiar código
 */
@Component
@ConfigurationProperties(prefix = "app.horarios")
public class HorariosConfig {

    private Turno manana = new Turno();
    private Turno tarde = new Turno();
    private Turno sabado = new Turno();
    private int intervaloMinutos = 60;

    // Constructores
    public HorariosConfig() {
        // Valores por defecto
        manana.setInicio(8);
        manana.setFin(15);

        tarde.setInicio(0);
        tarde.setFin(0);

        sabado.setInicio(9);
        sabado.setFin(14);
    }

    // Getters y Setters
    public Turno getManana() {
        return manana;
    }

    public void setManana(Turno manana) {
        this.manana = manana;
    }

    public Turno getTarde() {
        return tarde;
    }

    public void setTarde(Turno tarde) {
        this.tarde = tarde;
    }

    public Turno getSabado() {
        return sabado;
    }

    public void setSabado(Turno sabado) {
        this.sabado = sabado;
    }

    public int getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setIntervaloMinutos(int intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }

    /**
     * Clase interna para representar un turno de trabajo
     */
    public static class Turno {
        private int inicio;
        private int fin;
        private List<Integer> excluir = List.of();

        // Constructores
        public Turno() {
        }

        public Turno(int inicio, int fin) {
            this.inicio = inicio;
            this.fin = fin;
        }

        public Turno(int inicio, int fin, List<Integer> excluir) {
            this.inicio = inicio;
            this.fin = fin;
            this.excluir = excluir != null ? excluir : List.of();
        }

        // Getters y Setters
        public int getInicio() {
            return inicio;
        }

        public void setInicio(int inicio) {
            this.inicio = inicio;
        }

        public int getFin() {
            return fin;
        }

        public void setFin(int fin) {
            this.fin = fin;
        }

        public List<Integer> getExcluir() {
            return excluir;
        }

        public void setExcluir(List<Integer> excluir) {
            this.excluir = excluir != null ? excluir : List.of();
        }

        /**
         * Verifica si una hora está excluida de este turno
         */
        public boolean isHoraExcluida(int hora) {
            return excluir.contains(hora);
        }

        /**
         * Valida si la configuración del turno es válida
         */
        public boolean isValid() {
            if (inicio == 0 || fin == 0) {
                return true;
            }
            return inicio >= 0 && fin <= 24 && inicio < fin;
        }

        @Override
        public String toString() {
            return String.format("Turno{inicio=%d, fin=%d, excluir=%s}", inicio, fin, excluir);
        }
    }

    /**
     * Valida toda la configuración de horarios
     */
    public boolean isConfiguracionValida() {
        return manana.isValid() &&
                tarde.isValid() &&
                sabado.isValid() &&
                intervaloMinutos > 0;
    }

    @Override
    public String toString() {
        return String.format("HorariosConfig{mañana=%s, tarde=%s, sabado=%s, intervalo=%d min}",
                manana, tarde, sabado, intervaloMinutos);
    }
}