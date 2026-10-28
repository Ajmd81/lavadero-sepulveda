package com.lavaderosepulveda.app.model.auth;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // BCrypt

    /**
     * Versión de los tokens del usuario. Todo JWT lleva esta versión en el claim "ver";
     * incrementarla invalida de golpe todos los tokens emitidos antes (cambio de contraseña,
     * "cerrar sesión en todos los dispositivos", desactivar al usuario...).
     * Nullable para que ddl-auto=update pueda añadir la columna con filas existentes (null = 0).
     */
    @Column(name = "token_version")
    private Integer tokenVersion = 0;

    public void invalidarTodosLosTokens() {
        this.tokenVersion = (tokenVersion == null ? 0 : tokenVersion) + 1;
    }

    @Column(nullable = false)
    private String nombreCompleto = "Administrador";

    private String email;

    @Column(unique = true)
    private String dni;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;
}