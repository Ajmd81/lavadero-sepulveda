package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.LoginRequest;
import com.lavaderosepulveda.app.dto.LoginResponse;
import com.lavaderosepulveda.app.dto.UserDTO;
import com.lavaderosepulveda.app.model.Usuario;
import com.lavaderosepulveda.app.repository.UsuarioRepository;
import com.lavaderosepulveda.app.security.JwtService;
import com.lavaderosepulveda.app.security.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
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
    private final LoginRateLimiter rateLimiter;
    private final JwtService jwtService;

    // ─── LOGIN ────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        // ① Rate limiting por IP
        String ip = obtenerIpReal(httpRequest);
        if (!rateLimiter.intentoPermitido(ip)) {
            long espera = rateLimiter.segundosHastaReset(ip);
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Demasiados intentos fallidos. Espera " + espera + " segundos.",
                    "retryAfter", espera
            ));
        }

        // ② Validar credenciales
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(request.getUsername());

        if (usuarioOpt.isEmpty() || !usuarioOpt.get().getActivo()) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        // ③ Generar JWT firmado y actualizar último acceso
        String token = jwtService.generarToken(usuario.getUsername());

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        UserDTO user = new UserDTO();
        user.setUsername(usuario.getUsername());
        user.setNombre(usuario.getNombreCompleto());
        user.setRole("ADMIN");

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(user);

        return ResponseEntity.ok(response);
    }

    // ─── VERIFY TOKEN ─────────────────────────────────────────────────────────

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token inválido");
        }

        String token = authHeader.substring(7);

        if (!jwtService.esValido(token)) {
            return ResponseEntity.status(401).body("Token expirado o inválido");
        }

        String username = jwtService.extraerUsername(token);

        return usuarioRepository.findByUsername(username)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .map(usuario -> {
                    UserDTO user = new UserDTO();
                    user.setUsername(usuario.getUsername());
                    user.setNombre(usuario.getNombreCompleto());
                    user.setRole("ADMIN");
                    return ResponseEntity.ok((Object) user);
                })
                .orElse(ResponseEntity.status(401).body("Usuario no encontrado o inactivo"));
    }

    // ─── LOGOUT ───────────────────────────────────────────────────────────────
    // Con JWT stateless el logout real se gestiona en el cliente (eliminar token).
    // Este endpoint existe para que el frontend tenga un punto de cierre limpio.

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    // ─── OBTENER PERFIL ───────────────────────────────────────────────────────

    @GetMapping("/perfil")
    public ResponseEntity<?> getPerfil(@RequestParam String username) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        Map<String, Object> perfil = new HashMap<>();
        perfil.put("username", usuario.getUsername());
        perfil.put("nombreCompleto", usuario.getNombreCompleto());
        perfil.put("email", usuario.getEmail());
        perfil.put("dni", usuario.getDni());

        return ResponseEntity.ok(perfil);
    }

    // ─── ACTUALIZAR DATOS PERSONALES ──────────────────────────────────────────

    @PutMapping("/perfil/datos")
    public ResponseEntity<?> actualizarDatos(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String nombreCompleto = payload.get("nombreCompleto");
        String email = payload.get("email");
        String dni = payload.get("dni");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        if (nombreCompleto != null && !nombreCompleto.isBlank()) usuario.setNombreCompleto(nombreCompleto);
        if (email != null && !email.isBlank()) usuario.setEmail(email);

        if (dni != null && !dni.isBlank()) {
            if (!dni.matches("^[0-9]{8}[A-Z]$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Formato de DNI inválido. Ejemplo: 12345678A"));
            }
            Optional<Usuario> dniExistente = usuarioRepository.findByDni(dni);
            if (dniExistente.isPresent() && !dniExistente.get().getUsername().equals(username)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El DNI ya está registrado en otro usuario"));
            }
            usuario.setDni(dni);
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("message", "Datos actualizados correctamente"));
    }

    // ─── CAMBIAR USERNAME ─────────────────────────────────────────────────────

    @PutMapping("/perfil/username")
    public ResponseEntity<?> cambiarUsername(@RequestBody Map<String, String> payload) {
        String currentUsername = payload.get("currentUsername");
        String nuevoUsername = payload.get("nuevoUsername");

        if (nuevoUsername == null || nuevoUsername.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nuevo username no puede estar vacío"));
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(currentUsername);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }

        if (usuarioRepository.findByUsername(nuevoUsername).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El username ya está en uso"));
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setUsername(nuevoUsername);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("message", "Username actualizado"));
    }

    // ─── CAMBIAR CONTRASEÑA ───────────────────────────────────────────────────

    @PutMapping("/perfil/password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String passwordActual = payload.get("passwordActual");
        String passwordNueva = payload.get("passwordNueva");

        if (passwordNueva == null || passwordNueva.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La contraseña debe tener al menos 6 caracteres"));
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña actual es incorrecta"));
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────

    private String obtenerIpReal(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}