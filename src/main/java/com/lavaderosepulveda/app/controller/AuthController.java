package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.LoginRequest;
import com.lavaderosepulveda.app.dto.LoginResponse;
import com.lavaderosepulveda.app.dto.UserDTO;
import com.lavaderosepulveda.app.model.Usuario;
import com.lavaderosepulveda.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(request.getUsername());
        
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().getActivo()) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
        
        // Actualizar último acceso
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);
        
        UserDTO user = new UserDTO();
        user.setUsername(usuario.getUsername());
        user.setNombre(usuario.getNombreCompleto());
        user.setRole("ADMIN");
        
        LoginResponse response = new LoginResponse();
        response.setToken("Bearer-token-" + System.currentTimeMillis());
        response.setUser(user);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && token.startsWith("Bearer")) {
            // En producción usarías JWT real
            UserDTO user = new UserDTO();
            user.setUsername("admin");
            user.setNombre("Administrador");
            user.setRole("ADMIN");
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).body("Token inválido");
    }

    // Actualizar datos personales (nombre y email)
    @PutMapping("/perfil/datos")
    public ResponseEntity<?> actualizarDatos(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String nombreCompleto = payload.get("nombreCompleto");
        String email = payload.get("email");    

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setEmail(email);
        usuarioRepository.save(usuario);    

        return ResponseEntity.ok(Map.of("message", "Datos actualizados"));
    }
    
    // Cambiar username
    @PutMapping("/perfil/username")
    public ResponseEntity<?> cambiarUsername(@RequestBody Map<String, String> payload) {
        String currentUsername = payload.get("currentUsername");
        String nuevoUsername = payload.get("nuevoUsername");
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(currentUsername);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }
        
        if (usuarioRepository.findByUsername(nuevoUsername).isPresent()) {
            return ResponseEntity.status(400).body(Map.of("error", "El username ya existe"));
        }
        
        Usuario usuario = usuarioOpt.get();
        usuario.setUsername(nuevoUsername);
        usuarioRepository.save(usuario);
        
        return ResponseEntity.ok(Map.of("message", "Username actualizado"));
    }
    
    // Cambiar password
    @PutMapping("/perfil/password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String passwordActual = payload.get("passwordActual");
        String passwordNueva = payload.get("passwordNueva");
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            return ResponseEntity.status(400).body(Map.of("error", "Contraseña actual incorrecta"));
        }
        
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
        
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada"));
    }
}