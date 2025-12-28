package com.lavaderosepulveda.crm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lineas_factura")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    @Column(nullable = false)
    private String concepto;

    @Column(nullable = false)
    private Integer cantidad = 1;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private Double precioUnitario;

    @Column(nullable = false, precision = 5, scale = 2)
    private Double iva = 21.0;

    @Column(precision = 5, scale = 2)
    private Double descuento = 0.0; // porcentaje

    // Métodos calculados
    public Double getBaseImponible() {
        double base = precioUnitario * cantidad;
        if (descuento > 0) {
            base -= base * (descuento / 100);
        }
        return base;
    }

    public Double getImporteIva() {
        return getBaseImponible() * (iva / 100);
    }

    public Double getTotal() {
        return getBaseImponible() + getImporteIva();
    }
}
