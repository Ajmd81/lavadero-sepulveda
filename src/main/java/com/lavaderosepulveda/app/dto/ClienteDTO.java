package com.lavaderosepulveda.app.dto;

public class ClienteDTO {
    private Long id;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String email;
    private Boolean activo;

    // Estadísticas
    private Integer totalCitas;
    private Integer citasCompletadas;
    private Integer citasCanceladas;
    private Integer citasNoPresentadas;
    private Double totalFacturado;
    private String vehiculoHabitual;

    // Constructores
    public ClienteDTO() {
    }

    public ClienteDTO(Long id, String nombre, String apellidos, String telefono, String email, Boolean activo,
                      Integer totalCitas, Integer citasCompletadas, Integer citasCanceladas,
                      Integer citasNoPresentadas, Double totalFacturado, String vehiculoHabitual) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.email = email;
        this.activo = activo;
        this.totalCitas = totalCitas;
        this.citasCompletadas = citasCompletadas;
        this.citasCanceladas = citasCanceladas;
        this.citasNoPresentadas = citasNoPresentadas;
        this.totalFacturado = totalFacturado;
        this.vehiculoHabitual = vehiculoHabitual;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Integer getTotalCitas() {
        return totalCitas;
    }

    public void setTotalCitas(Integer totalCitas) {
        this.totalCitas = totalCitas;
    }

    public Integer getCitasCompletadas() {
        return citasCompletadas;
    }

    public void setCitasCompletadas(Integer citasCompletadas) {
        this.citasCompletadas = citasCompletadas;
    }

    public Integer getCitasCanceladas() {
        return citasCanceladas;
    }

    public void setCitasCanceladas(Integer citasCanceladas) {
        this.citasCanceladas = citasCanceladas;
    }

    public Integer getCitasNoPresentadas() {
        return citasNoPresentadas;
    }

    public void setCitasNoPresentadas(Integer citasNoPresentadas) {
        this.citasNoPresentadas = citasNoPresentadas;
    }

    public Double getTotalFacturado() {
        return totalFacturado;
    }

    public void setTotalFacturado(Double totalFacturado) {
        this.totalFacturado = totalFacturado;
    }

    public String getVehiculoHabitual() {
        return vehiculoHabitual;
    }

    public void setVehiculoHabitual(String vehiculoHabitual) {
        this.vehiculoHabitual = vehiculoHabitual;
    }

    @Override
    public String toString() {
        return "ClienteDTO{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", activo=" + activo +
                ", totalCitas=" + totalCitas +
                ", citasCompletadas=" + citasCompletadas +
                ", citasCanceladas=" + citasCanceladas +
                ", citasNoPresentadas=" + citasNoPresentadas +
                ", totalFacturado=" + totalFacturado +
                ", vehiculoHabitual='" + vehiculoHabitual + '\'' +
                '}';
    }
}