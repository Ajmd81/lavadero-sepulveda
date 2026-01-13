package com.lavaderosepulveda.crm.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_factura", nullable = false, unique = true)
    private String numeroFactura;

    @Column(name = "serie_factura", nullable = false)
    private String serieFactura = "A";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @Column(name = "fecha_factura", nullable = false)
    private LocalDate fechaFactura;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LineaFactura> lineas = new ArrayList<>();

    @Column(name = "base_imponible", precision = 10, scale = 2)
    private Double baseImponible;

    @Column(name = "total_iva", precision = 10, scale = 2)
    private Double totalIva;

    @Column(name = "total_factura", precision = 10, scale = 2)
    private Double totalFactura;

    @Column(name = "forma_pago")
    private String formaPago; // EFECTIVO, TARJETA, TRANSFERENCIA, BIZUM

    @Column(nullable = false)
    private Boolean pagada = false;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(length = 1000)
    private String observaciones;

    // Control de envío
    @Column(name = "enviada_email")
    private Boolean enviadaEmail = false;

    @Column(name = "fecha_envio_email")
    private LocalDateTime fechaEnvioEmail;

    @Column(name = "enviada_whatsapp")
    private Boolean enviadaWhatsapp = false;

    @Column(name = "fecha_envio_whatsapp")
    private LocalDateTime fechaEnvioWhatsapp;

    // Ruta del PDF generado
    @Column(name = "ruta_pdf")
    private String rutaPdf;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (fechaFactura == null) {
            fechaFactura = LocalDate.now();
        }
        calcularTotales();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calcularTotales();
    }

    // Métodos de utilidad
    public void calcularTotales() {
        if (lineas == null || lineas.isEmpty()) {
            baseImponible = 0.0;
            totalIva = 0.0;
            totalFactura = 0.0;
            return;
        }

        baseImponible = lineas.stream()
            .mapToDouble(LineaFactura::getBaseImponible)
            .sum();

        totalIva = lineas.stream()
            .mapToDouble(LineaFactura::getImporteIva)
            .sum();

        totalFactura = baseImponible + totalIva;
    }

    public void agregarLinea(LineaFactura linea) {
        lineas.add(linea);
        linea.setFactura(this);
        calcularTotales();
    }

    public void eliminarLinea(LineaFactura linea) {
        lineas.remove(linea);
        linea.setFactura(null);
        calcularTotales();
    }

    public boolean isPendientePago() {
        return !pagada && fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    public boolean isVencida() {
        return !pagada && fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }
}
