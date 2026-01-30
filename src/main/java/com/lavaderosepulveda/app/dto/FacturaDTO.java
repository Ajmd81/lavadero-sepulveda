package com.lavaderosepulveda.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lavaderosepulveda.app.model.enums.EstadoFactura;
import com.lavaderosepulveda.app.model.enums.MetodoPago;
import com.lavaderosepulveda.app.model.enums.TipoFactura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FacturaDTO {

    private Long id;
    
    @JsonAlias({"numeroFactura", "numero_factura"})
    private String numero;
    
    @JsonAlias({"fechaEmision", "fecha_emision", "fecha"})
    @JsonFormat(pattern = "dd/MM/yyyy", shape = JsonFormat.Shape.STRING)
    private LocalDate fecha;
    
    private String tipo;
    private String estado;
    @JsonAlias({"metodo_pago", "metodoPago"})
    private String metodoPago;
    
    // Datos del cliente
    @JsonAlias({"cliente_id"})
    private Long clienteId;
    @JsonAlias({"cliente_nombre", "nombreCliente"})
    private String clienteNombre;
    @JsonAlias({"cliente_nif", "nifCliente"})
    private String clienteNif;
    @JsonAlias({"cliente_direccion", "direccionCliente"})
    private String clienteDireccion;
    @JsonAlias({"cliente_email", "emailCliente"})
    private String clienteEmail;
    @JsonAlias({"cliente_telefono", "telefonoCliente"})
    private String clienteTelefono;
    
    // Líneas de la factura
    private List<LineaFacturaDTO> lineas;
    
    // Importes
    private BigDecimal baseImponible;
    private BigDecimal tipoIva;
    private BigDecimal importeIva;
    private BigDecimal total;
    
    // Otros
    @JsonFormat(pattern = "dd/MM/yyyy", shape = JsonFormat.Shape.STRING)
    private LocalDate fechaPago;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
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

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // DTO interno para líneas de factura
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
    }
}
