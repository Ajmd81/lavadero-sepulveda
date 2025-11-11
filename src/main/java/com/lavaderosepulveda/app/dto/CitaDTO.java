package com.lavaderosepulveda.app.dto;

/**
 * DTO para recibir los datos de citas desde la aplicación Android
 */
public class CitaDTO {
    private Long id;
    private String fecha;
    private String hora;
    private String nombre;
    private String email;
    private String telefono;
    private String modeloVehiculo;
    private String tipoLavado;
    private String estado;

    // Constructor vacío requerido por Jackson
    public CitaDTO() {
    }

    // Constructor con todos los campos
    public CitaDTO(Long id, String fecha, String hora, String nombre, String email,
                   String telefono, String modeloVehiculo, String tipoLavado, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.modeloVehiculo = modeloVehiculo;
        this.tipoLavado = tipoLavado;
        this.estado = estado;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getModeloVehiculo() {
        return modeloVehiculo;
    }

    public void setModeloVehiculo(String modeloVehiculo) {
        this.modeloVehiculo = modeloVehiculo;
    }

    public String getTipoLavado() {
        return tipoLavado;
    }

    public void setTipoLavado(String tipoLavado) {
        this.tipoLavado = tipoLavado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "CitaDTO{" +
                "id=" + id +
                ", fecha='" + fecha + '\'' +
                ", hora='" + hora + '\'' +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", modeloVehiculo='" + modeloVehiculo + '\'' +
                ", tipoLavado='" + tipoLavado + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}