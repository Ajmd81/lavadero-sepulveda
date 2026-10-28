package com.lavaderosepulveda.app.dto.facturacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FacturaDTO {

    private Long id;
    
    @JsonAlias({"numeroFactura", "numero_factura"})
    @Size(max = 30)
    private String numero;
    
    @JsonAlias({"fechaEmision", "fecha_emision", "fecha"})
    @JsonFormat(pattern = "dd/MM/yyyy", shape = JsonFormat.Shape.STRING)
    private LocalDate fecha;
    
    @Size(max = 30)
    private String tipo;
    @Size(max = 30)
    private String estado;
    @JsonAlias({"metodo_pago", "metodoPago"})
    @Size(max = 30)
    private String metodoPago;
    
    // Datos del cliente
    @JsonAlias({"cliente_id"})
    @Positive
    private Long clienteId;
    @JsonAlias({"cliente_nombre", "nombreCliente"})
    @Size(max = 200)
    private String clienteNombre;
    @JsonAlias({"cliente_nif", "nifCliente"})
    @Size(max = 20)
    @Pattern(regexp = "^$|^[A-Za-z0-9\\-]{5,20}$", message = "NIF/CIF con formato inválido")
    private String clienteNif;
    @JsonAlias({"cliente_direccion", "direccionCliente"})
    @Size(max = 255)
    private String clienteDireccion;
    @JsonAlias({"cliente_email", "emailCliente"})
    @Email(message = "Formato de email no válido")
    @Size(max = 150)
    private String clienteEmail;
    @JsonAlias({"cliente_telefono", "telefonoCliente"})
    @Size(max = 20)
    @Pattern(regexp = "^$|^[+0-9\\s\\-()]{6,20}$", message = "Teléfono con formato inválido")
    private String clienteTelefono;
    
    // Líneas de la factura
    @Valid
    @Size(max = 200, message = "Demasiadas líneas en la factura")
    private List<LineaFacturaDTO> lineas;
    
    // Importes
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal baseImponible;
    @DecimalMin(value = "0", message = "No puede ser negativo")
    @DecimalMax(value = "100", message = "No puede superar 100")
    @Digits(integer = 3, fraction = 4)
    private BigDecimal tipoIva;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal importeIva;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal total;
    
    // Otros
    @JsonFormat(pattern = "dd/MM/yyyy", shape = JsonFormat.Shape.STRING)
    private LocalDate fechaPago;
    @Size(max = 2000)
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
        @Positive
        private Long citaId;
        @Size(max = 255)
        private String concepto;
        @Min(0)
        @Max(100000)
        private Integer cantidad;
        @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
        private BigDecimal precioUnitario;
        @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
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
