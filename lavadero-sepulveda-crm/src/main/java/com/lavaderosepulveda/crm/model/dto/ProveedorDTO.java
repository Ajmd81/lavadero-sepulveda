package com.lavaderosepulveda.crm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Override
    public String toString() {
        return nombre;
    }
}
