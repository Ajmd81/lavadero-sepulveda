package com.lavaderosepulveda.app.model;

import com.lavaderosepulveda.app.model.enums.EstadoDeclaracion;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro histórico de cada fichero BOE generado (Modelo 303 / 130).
 *
 * Se crea automáticamente cada vez que el usuario pulsa "Generar fichero"
 * y puede marcarse como PRESENTADO una vez enviado a la AEAT.
 *
 * Tabla: declaraciones_fiscales
 */
@Entity
@Table(
    name = "declaraciones_fiscales",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_declaracion_modelo_ejercicio_periodo",
        columnNames = {"modelo", "ejercicio", "periodo"}
    )
)
public class DeclaracionFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** "303" o "130" */
    @Column(nullable = false, length = 3)
    private String modelo;

    /** Año fiscal: 2026 */
    @Column(nullable = false)
    private Integer ejercicio;

    /** "1T", "2T", "3T", "4T" */
    @Column(nullable = false, length = 2)
    private String periodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoDeclaracion estado = EstadoDeclaracion.GENERADO;

    /** Fecha en la que se generó el fichero BOE */
    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    /** Fecha en la que se marcó como presentado ante la AEAT (puede ser null) */
    @Column(name = "fecha_presentacion")
    private LocalDate fechaPresentacion;

    // ─── Importes snapshot del momento de generación ──────────────────────
    // Se guardan para tener constancia aunque las facturas cambien después.

    // Modelo 303 — IVA
    @Column(name = "base_repercutida",  precision = 12, scale = 2)
    private BigDecimal baseRepercutida  = BigDecimal.ZERO;

    @Column(name = "cuota_repercutida", precision = 12, scale = 2)
    private BigDecimal cuotaRepercutida = BigDecimal.ZERO;

    @Column(name = "base_soportada",    precision = 12, scale = 2)
    private BigDecimal baseSoportada    = BigDecimal.ZERO;

    @Column(name = "cuota_soportada",   precision = 12, scale = 2)
    private BigDecimal cuotaSoportada   = BigDecimal.ZERO;

    /** Resultado IVA = cuotaRepercutida − cuotaSoportada */
    @Column(name = "resultado_iva",     precision = 12, scale = 2)
    private BigDecimal resultadoIva     = BigDecimal.ZERO;

    // Modelo 130 — IRPF
    @Column(name = "ingresos_trimestre", precision = 12, scale = 2)
    private BigDecimal ingresosTrimestre = BigDecimal.ZERO;

    @Column(name = "gastos_trimestre",   precision = 12, scale = 2)
    private BigDecimal gastosTrimestre   = BigDecimal.ZERO;

    @Column(name = "rendimiento_neto",   precision = 12, scale = 2)
    private BigDecimal rendimientoNeto   = BigDecimal.ZERO;

    /** Pago fraccionado 20% del rendimiento neto positivo */
    @Column(name = "pago_fraccionado",   precision = 12, scale = 2)
    private BigDecimal pagoFraccionado   = BigDecimal.ZERO;

    /** Nombre del fichero descargado: ej. 44372838L20261T.303 */
    @Column(name = "nombre_fichero", length = 40)
    private String nombreFichero;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt      = LocalDateTime.now();
        updatedAt      = LocalDateTime.now();
        fechaGeneracion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters y Setters
    // ─────────────────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Integer getEjercicio() { return ejercicio; }
    public void setEjercicio(Integer ejercicio) { this.ejercicio = ejercicio; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public EstadoDeclaracion getEstado() { return estado; }
    public void setEstado(EstadoDeclaracion estado) { this.estado = estado; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
