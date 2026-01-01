package com.lavaderosepulveda.crm.api.dto;

import java.math.BigDecimal;
import java.util.List;

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

    // Constructores
    public FacturaDTO() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteNif() {
        return clienteNif;
    }

    public void setClienteNif(String clienteNif) {
        this.clienteNif = clienteNif;
    }

    public String getClienteDireccion() {
        return clienteDireccion;
    }

    public void setClienteDireccion(String clienteDireccion) {
        this.clienteDireccion = clienteDireccion;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public List<LineaFacturaDTO> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaFacturaDTO> lineas) {
        this.lineas = lineas;
    }

    public BigDecimal getBaseImponible() {
        return baseImponible;
    }

    public void setBaseImponible(BigDecimal baseImponible) {
        this.baseImponible = baseImponible;
    }

    public BigDecimal getTipoIva() {
        return tipoIva;
    }

    public void setTipoIva(BigDecimal tipoIva) {
        this.tipoIva = tipoIva;
    }

    public BigDecimal getImporteIva() {
        return importeIva;
    }

    public void setImporteIva(BigDecimal importeIva) {
        this.importeIva = importeIva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // Métodos de utilidad para la tabla
    public String getTipoFormateado() {
        if (tipo == null) return "";
        return tipo.equals("SIMPLIFICADA") ? "Simplificada" : "Completa";
    }

    public String getEstadoFormateado() {
        if (estado == null) return "";
        return estado.equals("PENDIENTE") ? "Pendiente" : "Pagada";
    }

    public String getMetodoPagoFormateado() {
        if (metodoPago == null) return "-";
        switch (metodoPago) {
            case "EFECTIVO": return "Efectivo";
            case "TARJETA": return "Tarjeta";
            case "BIZUM": return "Bizum";
            case "TRANSFERENCIA": return "Transferencia";
            default: return metodoPago;
        }
    }

    public String getTotalFormateado() {
        if (total == null) return "0,00 €";
        return String.format("%,.2f €", total).replace(",", "X").replace(".", ",").replace("X", ".");
    }

    public String getBaseImponibleFormateada() {
        if (baseImponible == null) return "0,00 €";
        return String.format("%,.2f €", baseImponible).replace(",", "X").replace(".", ",").replace("X", ".");
    }

    public String getImporteIvaFormateado() {
        if (importeIva == null) return "0,00 €";
        return String.format("%,.2f €", importeIva).replace(",", "X").replace(".", ",").replace("X", ".");
    }

    // DTO para líneas de factura
    public static class LineaFacturaDTO {
        private Long id;
        private Long citaId;
        private String concepto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

        public LineaFacturaDTO() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getCitaId() {
            return citaId;
        }

        public void setCitaId(Long citaId) {
            this.citaId = citaId;
        }

        public String getConcepto() {
            return concepto;
        }

        public void setConcepto(String concepto) {
            this.concepto = concepto;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(BigDecimal precioUnitario) {
            this.precioUnitario = precioUnitario;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }

        public String getPrecioFormateado() {
            if (precioUnitario == null) return "0,00 €";
            return String.format("%,.2f €", precioUnitario);
        }

        public String getSubtotalFormateado() {
            if (subtotal == null) return "0,00 €";
            return String.format("%,.2f €", subtotal);
        }
    }
}
