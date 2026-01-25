package main.java.com.lavaderosepulveda.app.dto;

public class LoginResponse {
    private String token;
    private UserDTO user;

    // Constructores
    public LoginResponse() {}

    public LoginResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}