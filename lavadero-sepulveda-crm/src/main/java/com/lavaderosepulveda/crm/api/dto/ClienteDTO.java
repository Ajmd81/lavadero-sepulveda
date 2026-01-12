package com.lavaderosepulveda.crm.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClienteDTO {
    
    private Long id;
    
    private String nombre;
    
    private String apellidos;
    
    private String telefono;
    
    private String email;
    
    @JsonAlias({"nif", "NIF", "dni", "cif"})
    private String nif;
    
    private String direccion;
    
    @JsonAlias({"codigoPostal", "codigo_postal", "cp"})
    private String codigoPostal;
    
    private String ciudad;
    
    private String provincia;
    
    // Datos del vehículo
    private String matricula;
    
    private String marca;
    
    private String modelo;
    
    private String color;
    
    @JsonAlias({"vehiculoHabitual", "vehiculo_habitual"})
    private String vehiculoHabitual;
    
    // Estadísticas del cliente
    @JsonAlias({"totalCitas", "total_citas"})
    private Integer totalCitas;
    
    @JsonAlias({"citasCompletadas", "citas_completadas"})
    private Integer citasCompletadas;
    
    @JsonAlias({"citasCanceladas", "citas_canceladas"})
    private Integer citasCanceladas;
    
    @JsonAlias({"citasNoPresentadas", "citas_no_presentadas"})
    private Integer citasNoPresentadas;
    
    @JsonAlias({"totalFacturado", "total_facturado"})
    private Double totalFacturado;
    
    @JsonAlias({"fechaPrimeraCita", "fecha_primera_cita"})
    private LocalDateTime fechaPrimeraCita;
    
    @JsonAlias({"fechaUltimaCita", "fecha_ultima_cita"})
    private LocalDateTime fechaUltimaCita;
    
    private String notas;
    
    private Boolean activo;
    
    @JsonAlias({"createdAt", "created_at"})
    private LocalDateTime createdAt;
    
    @JsonAlias({"updatedAt", "updated_at"})
    private LocalDateTime updatedAt;
    
    // Campos calculados (si vienen del API)
    private String nombreCompleto;
    private Double ticketMedio;
    private Double tasaCompletacion;
    private Double tasaNoPresentacion;
    
    // Constructores
    public ClienteDTO() {}
    
    public ClienteDTO(Long id, String nombre, String apellidos) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
    }
    
    // Método para obtener nombre completo
    public String getNombreCompleto() {
        if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
            return nombreCompleto;
        }
        if (apellidos != null && !apellidos.isEmpty()) {
            return nombre + " " + apellidos;
        }
        return nombre;
    }
    
    // Método para obtener dirección completa
    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder();
        if (direccion != null && !direccion.isEmpty()) {
            sb.append(direccion);
        }
        if (codigoPostal != null && !codigoPostal.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(codigoPostal);
        }
        if (ciudad != null && !ciudad.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(ciudad);
        }
        if (provincia != null && !provincia.isEmpty() && !provincia.equalsIgnoreCase(ciudad)) {
            if (sb.length() > 0) sb.append(" (");
            sb.append(provincia);
            sb.append(")");
        }
        return sb.toString();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
    
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getVehiculoHabitual() { return vehiculoHabitual; }
    public void setVehiculoHabitual(String vehiculoHabitual) { this.vehiculoHabitual = vehiculoHabitual; }
    
    public Integer getTotalCitas() { return totalCitas; }
    public void setTotalCitas(Integer totalCitas) { this.totalCitas = totalCitas; }
    
    public Integer getCitasCompletadas() { return citasCompletadas; }
    public void setCitasCompletadas(Integer citasCompletadas) { this.citasCompletadas = citasCompletadas; }
    
    public Integer getCitasCanceladas() { return citasCanceladas; }
    public void setCitasCanceladas(Integer citasCanceladas) { this.citasCanceladas = citasCanceladas; }
    
    public Integer getCitasNoPresentadas() { return citasNoPresentadas; }
    public void setCitasNoPresentadas(Integer citasNoPresentadas) { this.citasNoPresentadas = citasNoPresentadas; }
    
    public Double getTotalFacturado() { return totalFacturado; }
    public void setTotalFacturado(Double totalFacturado) { this.totalFacturado = totalFacturado; }
    
    public LocalDateTime getFechaPrimeraCita() { return fechaPrimeraCita; }
    public void setFechaPrimeraCita(LocalDateTime fechaPrimeraCita) { this.fechaPrimeraCita = fechaPrimeraCita; }
    
    public LocalDateTime getFechaUltimaCita() { return fechaUltimaCita; }
    public void setFechaUltimaCita(LocalDateTime fechaUltimaCita) { this.fechaUltimaCita = fechaUltimaCita; }
    
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    
    public Double getTicketMedio() { return ticketMedio; }
    public void setTicketMedio(Double ticketMedio) { this.ticketMedio = ticketMedio; }
    
    public Double getTasaCompletacion() { return tasaCompletacion; }
    public void setTasaCompletacion(Double tasaCompletacion) { this.tasaCompletacion = tasaCompletacion; }
    
    public Double getTasaNoPresentacion() { return tasaNoPresentacion; }
    public void setTasaNoPresentacion(Double tasaNoPresentacion) { this.tasaNoPresentacion = tasaNoPresentacion; }
    
    @Override
    public String toString() {
        String nifStr = (nif != null && !nif.isEmpty()) ? " (" + nif + ")" : "";
        return getNombreCompleto() + nifStr;
    }
}