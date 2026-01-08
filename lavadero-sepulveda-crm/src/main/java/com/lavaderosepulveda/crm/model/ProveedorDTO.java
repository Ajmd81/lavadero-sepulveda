package com.lavaderosepulveda.crm.model;

public class ProveedorDTO {
    private Long id;
    private String nombre;
    private String nif;
    private String direccion;
    private String telefono;
    private String email;
    private String contacto;
    private String iban;
    private String notas;
    private Boolean activo;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre;
    }
}
