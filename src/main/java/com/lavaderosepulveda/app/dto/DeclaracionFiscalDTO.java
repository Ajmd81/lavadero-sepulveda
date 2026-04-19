package com.lavaderosepulveda.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para el historial de declaraciones fiscales.
 * Incluye todos los importes snapshot y el estado de presentación.
 */
public class DeclaracionFiscalDTO {

    private Long id;

    /** "303" o "130" */
    private String modelo;

    /** Año fiscal */
    private Integer ejercicio;

    /** "1T" | "2T" | "3T" | "4T" */
    private String periodo;

    /** "GENERADO" | "PRESENTADO" */
    private String estado;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", shape = JsonFormat.Shape.STRING)
    private LocalDateTime fechaGeneracion;

    @JsonFormat(pattern = "dd/MM/yyyy", shape = JsonFormat.Shape.STRING)
    private LocalDate fechaPresentacion;

    // Importes Modelo 303
    private BigDecimal baseRepercutida;
    private BigDecimal cuotaRepercutida;
    private BigDecimal baseSoportada;
    private BigDecimal cuotaSoportada;
    private BigDecimal resultadoIva;

    // Importes Modelo 130
    private BigDecimal ingresosTrimestre;
    private BigDecimal gastosTrimestre;
    private BigDecimal rendimientoNeto;
    private BigDecimal pagoFraccionado;

    /** Nombre del fichero BOE generado */
    private String nombreFichero;

    // ── Getters y Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Integer getEjercicio() { return ejercicio; }
    public void setEjercicio(Integer ejercicio) { this.ejercicio = ejercicio; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public LocalDate getFechaPresentacion() { return fechaPresentacion; }
    public void setFechaPresentacion(LocalDate fechaPresentacion) { this.fechaPresentacion = fechaPresentacion; }

    public BigDecimal getBaseRepercutida() { return baseRepercutida; }
    public void setBaseRepercutida(BigDecimal baseRepercutida) { this.baseRepercutida = baseRepercutida; }

    public BigDecimal getCuotaRepercutida() { return cuotaRepercutida; }
    public void setCuotaRepercutida(BigDecimal cuotaRepercutida) { this.cuotaRepercutida = cuotaRepercutida; }

    public BigDecimal getBaseSoportada() { return baseSoportada; }
    public void setBaseSoportada(BigDecimal baseSoportada) { this.baseSoportada = baseSoportada; }

    public BigDecimal getCuotaSoportada() { return cuotaSoportada; }
    public void setCuotaSoportada(BigDecimal cuotaSoportada) { this.cuotaSoportada = cuotaSoportada; }

    public BigDecimal getResultadoIva() { return resultadoIva; }
    public void setResultadoIva(BigDecimal resultadoIva) { this.resultadoIva = resultadoIva; }

    public BigDecimal getIngresosTrimestre() { return ingresosTrimestre; }
    public void setIngresosTrimestre(BigDecimal ingresosTrimestre) { this.ingresosTrimestre = ingresosTrimestre; }

    public BigDecimal getGastosTrimestre() { return gastosTrimestre; }
    public void setGastosTrimestre(BigDecimal gastosTrimestre) { this.gastosTrimestre = gastosTrimestre; }

    public BigDecimal getRendimientoNeto() { return rendimientoNeto; }
    public void setRendimientoNeto(BigDecimal rendimientoNeto) { this.rendimientoNeto = rendimientoNeto; }

    public BigDecimal getPagoFraccionado() { return pagoFraccionado; }
    public void setPagoFraccionado(BigDecimal pagoFraccionado) { this.pagoFraccionado = pagoFraccionado; }

    public String getNombreFichero() { return nombreFichero; }
    public void setNombreFichero(String nombreFichero) { this.nombreFichero = nombreFichero; }
}
