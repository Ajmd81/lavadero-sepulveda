package com.lavaderosepulveda.crm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDTO {

    private Long id;
    private String numero;
    private String fecha;
    private String tipo;
    private String estado;
    private String metodoPago;

    // Datos del cliente
    private Long clienteId;
    private String clienteNombre;
    private String clienteNif;
    private String clienteDireccion;
    private String clienteEmail;
    private String clienteTelefono;

    // Líneas de la factura
    private List<LineaFacturaDTO> lineas;

    // Importes
    private BigDecimal baseImponible;
    private BigDecimal tipoIva;
    private BigDecimal importeIva;
    private BigDecimal total;

    // Otros
    private String fechaPago;
    private String observaciones;

    // Métodos de utilidad para la tabla
    public String getTipoFormateado() {
        if (tipo == null)
            return "";
        return tipo.equals("SIMPLIFICADA") ? "Simplificada" : "Completa";
    }

    public String getEstadoFormateado() {
        if (estado == null)
            return "";
        return estado.equals("PENDIENTE") ? "Pendiente" : "Pagada";
    }

    public String getMetodoPagoFormateado() {
        if (metodoPago == null)
            return "-";
        switch (metodoPago) {
            case "EFECTIVO":
                return "Efectivo";
            case "TARJETA":
                return "Tarjeta";
            case "BIZUM":
                return "Bizum";
            case "TRANSFERENCIA":
                return "Transferencia";
            default:
                return metodoPago;
        }
    }

    public String getTotalFormateado() {
        if (total == null)
            return "0,00 €";
        return String.format("%,.2f €", total).replace(",", "X").replace(".", ",").replace("X", ".");
    }

    public String getBaseImponibleFormateada() {
        if (baseImponible == null)
            return "0,00 €";
        return String.format("%,.2f €", baseImponible).replace(",", "X").replace(".", ",").replace("X", ".");
    }

    public String getImporteIvaFormateado() {
        if (importeIva == null)
            return "0,00 €";
        return String.format("%,.2f €", importeIva).replace(",", "X").replace(".", ",").replace("X", ".");
    }

    // DTO para líneas de factura
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineaFacturaDTO {
        private Long id;
        private Long citaId;
        private String concepto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

        public String getPrecioFormateado() {
            if (precioUnitario == null)
                return "0,00 €";
            return String.format("%,.2f €", precioUnitario);
        }

        public String getSubtotalFormateado() {
            if (subtotal == null)
                return "0,00 €";
            return String.format("%,.2f €", subtotal);
        }
    }
}
