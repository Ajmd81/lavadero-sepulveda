package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.LoginRequest;
import com.lavaderosepulveda.app.dto.LoginResponse;
import com.lavaderosepulveda.app.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        
        System.out.println("=================================");
        System.out.println("INTENTO DE LOGIN:");
        System.out.println("Usuario recibido: " + request.getUsername());
        System.out.println("Usuario esperado: " + adminUsername);
        System.out.println("=================================");
        
        // Validar credenciales contra las variables de entorno
        if (adminUsername.equals(request.getUsername()) && 
            adminPassword.equals(request.getPassword())) {
            
            UserDTO user = new UserDTO();
            user.setUsername(adminUsername);
            user.setNombre("Administrador");
            user.setRole("ADMIN");
            
            LoginResponse response = new LoginResponse();
            response.setToken("Bearer-token-" + System.currentTimeMillis());
            response.setUser(user);
            
            System.out.println("✅ Login exitoso para: " + adminUsername);
            
            return ResponseEntity.ok(response);
        }
        
        System.out.println("❌ Login fallido - Credenciales incorrectas");
        return ResponseEntity.status(401).body("Credenciales inválidas");
    }
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && token.startsWith("Bearer")) {
            UserDTO user = new UserDTO();
            user.setUsername(adminUsername);
            user.setNombre("Administrador");
            user.setRole("ADMIN");
            
            return ResponseEntity.ok(user);
        }
        
        return ResponseEntity.status(401).body("Token inválido");
    }
}