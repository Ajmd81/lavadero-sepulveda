package com.lavaderosepulveda.crm.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacturaEmitidaDTO {

    private Long id;

    @JsonAlias({ "numero", "numeroFactura", "numero_factura" })
    private String numeroFactura;

    @JsonAlias({ "fecha", "fechaEmision", "fecha_emision" })
    private String fechaEmision;

    private Long clienteId;

    @JsonAlias({ "clienteNombre", "cliente_nombre", "nombreCliente" })
    private String clienteNombre;

    @JsonAlias({ "clienteNif", "cliente_nif", "nifCliente" })
    private String clienteNif;

    @JsonAlias({ "clienteDireccion", "cliente_direccion", "direccionCliente" })
    private String clienteDireccion;

    @JsonAlias({ "tipoFactura", "tipo_factura", "tipo" })
    private String tipoFactura;

    @JsonAlias({ "baseImponible", "base_imponible", "base" })
    private BigDecimal baseImponible;

    @JsonAlias({ "tipoIva", "tipo_iva", "porcentajeIva" })
    private BigDecimal tipoIva;

    @JsonAlias({ "cuotaIva", "cuota_iva", "importeIva", "iva" })
    private BigDecimal cuotaIva;

    private BigDecimal total;
    private String estado;

    @JsonAlias({ "metodoPago", "metodo_pago", "formaPago" })
    private String metodoPago;

    @JsonAlias({ "fechaPago", "fecha_pago" })
    private String fechaPago;

    @JsonAlias({ "citaId", "cita_id" })
    private Long citaId;

    // ✅ NUEVO: Lista de líneas de factura
    private List<LineaFactura> lineas;

    // ✅ NUEVO: Clase interna para las líneas
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineaFactura {
        private Long id;
        private Long citaId;
        private String concepto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }

    // ✅ NUEVO: Método para obtener el concepto de las líneas
    public String getConcepto() {
        if (lineas == null || lineas.isEmpty()) {
            return "";
        }
        if (lineas.size() == 1) {
            return lineas.get(0).getConcepto() != null ? lineas.get(0).getConcepto() : "";
        }
        // Múltiples líneas: concatenar conceptos
        return lineas.stream()
                .map(LineaFactura::getConcepto)
                .filter(c -> c != null && !c.isEmpty())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    @Override
    public String toString() {
        return "FacturaEmitidaDTO{" +
                "id=" + id +
                ", numeroFactura='" + numeroFactura + '\'' +
                ", fechaEmision='" + fechaEmision + '\'' +
                ", clienteNombre='" + clienteNombre + '\'' +
                ", concepto='" + getConcepto() + '\'' +
                ", total=" + total +
                ", estado='" + estado + '\'' +
                '}';
    }
}