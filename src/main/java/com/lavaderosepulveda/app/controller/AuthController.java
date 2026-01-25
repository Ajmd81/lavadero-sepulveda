package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.LoginRequest;
import com.lavaderosepulveda.app.dto.LoginResponse;
import com.lavaderosepulveda.app.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Por ahora, validación simple
        // TODO: Implementar autenticación real con base de datos
        if ("admin".equals(request.getUsername()) && 
            "admin123".equals(request.getPassword())) {
            
            // Crear usuario
            UserDTO user = new UserDTO();
            user.setUsername("admin");
            user.setNombre("Administrador");
            user.setRole("ADMIN");
            
            // Crear respuesta con token (por ahora un token simple)
            LoginResponse response = new LoginResponse();
            response.setToken("token-" + System.currentTimeMillis());
            response.setUser(user);
            
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(401).body("Credenciales inválidas");
    }
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
        // Verificar token
        if (token != null && token.startsWith("Bearer ")) {
            UserDTO user = new UserDTO();
            user.setUsername("admin");
            user.setNombre("Administrador");
            user.setRole("ADMIN");
            
            return ResponseEntity.ok(user);
        }
        
        return ResponseEntity.status(401).body("Token inválido");
    }
}