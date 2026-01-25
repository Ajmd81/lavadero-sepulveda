package main.java.com.lavaderosepulveda.app.dto;

public class UserDTO {
    private String username;
    private String nombre;
    private String role;

    // Constructores
    public UserDTO() {}

    public UserDTO(String username, String nombre, String role) {
        this.username = username;
        this.nombre = nombre;
        this.role = role;
    }

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}