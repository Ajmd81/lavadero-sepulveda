package com.lavaderosepulveda.app.dto.facturacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class ProveedorDTO {
    private Long id;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    private String nombre;
    @Size(max = 20)
    @Pattern(regexp = "^$|^[A-Za-z0-9\\-]{5,20}$", message = "NIF/CIF con formato inválido")
    private String nif;
    @Size(max = 255)
    private String direccion;
    @Size(max = 100)
    private String ciudad;
    @Size(max = 10)
    private String codigoPostal;
    @Size(max = 20)
    @Pattern(regexp = "^$|^[+0-9\\s\\-()]{6,20}$", message = "Teléfono con formato inválido")
    private String telefono;
    @Email(message = "Formato de email no válido")
    @Size(max = 150)
    private String email;
    @Size(max = 100)
    private String contacto;
    @Size(max = 42)
    @Pattern(regexp = "^$|^[A-Za-z]{2}[0-9A-Za-z\\s]{13,40}$", message = "IBAN con formato inválido")
    private String iban;
    @Size(max = 2000)
    private String notas;
    @Size(max = 50)
    private String categoria;
    @Size(max = 50)
    private String formaPago;
    private Boolean activo;

    // Constructores
    public ProveedorDTO() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

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

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
