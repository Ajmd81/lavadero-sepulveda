package com.lavaderosepulveda.app.dto.auth;

public class LoginResponse {
    private String token;
    private UserDTO user;
    /** Momento de expiración del token (epoch millis). El frontend renueva antes de llegar aquí. */
    private Long expiresAt;

    public LoginResponse() {}

    public LoginResponse(String token, UserDTO user, Long expiresAt) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
}
