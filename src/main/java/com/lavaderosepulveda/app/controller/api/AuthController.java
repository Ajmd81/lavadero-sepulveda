package com.lavaderosepulveda.app.controller.api;

import com.lavaderosepulveda.app.dto.auth.ActualizarPerfilRequest;
import com.lavaderosepulveda.app.dto.auth.CambiarPasswordRequest;
import com.lavaderosepulveda.app.dto.auth.CambiarUsernameRequest;
import com.lavaderosepulveda.app.dto.auth.LoginRequest;
import com.lavaderosepulveda.app.dto.auth.LoginResponse;
import com.lavaderosepulveda.app.dto.auth.UserDTO;
import com.lavaderosepulveda.app.model.auth.Usuario;
import com.lavaderosepulveda.app.repository.auth.UsuarioRepository;
import com.lavaderosepulveda.app.security.AuthenticatedUser;
import com.lavaderosepulveda.app.security.ClientIpResolver;
import com.lavaderosepulveda.app.security.JwtService;
import com.lavaderosepulveda.app.security.JwtService.TokenEmitido;
import com.lavaderosepulveda.app.security.JwtService.TokenInfo;
import com.lavaderosepulveda.app.security.LoginRateLimiter;
import com.lavaderosepulveda.app.security.TokenBlacklist;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Autenticación y perfil del usuario del CRM.
 *
 * Regla de oro: el usuario sobre el que se actúa SIEMPRE sale del token
 * (@AuthenticationPrincipal), nunca de un "username" enviado en el body o la URL.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /**
     * Hash BCrypt de una contraseña aleatoria. Se compara contra él cuando el usuario no existe
     * para que la respuesta tarde lo mismo y no se pueda averiguar qué usernames existen.
     */
    private static final String HASH_FICTICIO =
            "$2a$10$//uqzSS5QkgDVbiG72oLEuVaZ6XNVadgNxDp.0DLXcuX0RNuRCKqu";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter rateLimiter;
    private final JwtService jwtService;
    private final TokenBlacklist tokenBlacklist;
    private final ClientIpResolver ipResolver;

    // ─── LOGIN ────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {

        // ① Rate limiting por IP
        String ip = ipResolver.resolver(httpRequest);
        if (!rateLimiter.intentoPermitido(ip)) {
            long espera = rateLimiter.segundosHastaReset(ip);
            return demasiadosIntentos(espera);
        }

        // ② Rate limiting por usuario (intentos fallidos desde cualquier IP)
        String username = request.getUsername().trim();
        if (rateLimiter.usuarioBloqueado(username)) {
            log.warn("Login bloqueado temporalmente para usuario '{}' (ip {})", username, ip);
            return demasiadosIntentos(rateLimiter.segundosHastaDesbloqueoUsuario(username));
        }

        // ③ Validar credenciales en tiempo constante
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        String hash = usuarioOpt.map(Usuario::getPassword).orElse(HASH_FICTICIO);
        boolean passwordOk = passwordEncoder.matches(request.getPassword(), hash);

        if (usuarioOpt.isEmpty() || !passwordOk || !Boolean.TRUE.equals(usuarioOpt.get().getActivo())) {
            rateLimiter.registrarFallo(username);
            log.warn("Login fallido para usuario '{}' desde ip {}", username, ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas"));
        }

        Usuario usuario = usuarioOpt.get();
        rateLimiter.registrarExito(username);

        // ④ Emitir JWT y actualizar último acceso
        TokenEmitido emitido = jwtService.generarToken(usuario);
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new LoginResponse(emitido.token(), toUserDTO(usuario),
                emitido.expiresAt().toEpochMilli()));
    }

    // ─── VERIFY ───────────────────────────────────────────────────────────────
    // Si el token no es válido, Spring Security ya responde 401 antes de llegar aquí.

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@AuthenticationPrincipal AuthenticatedUser actual) {
        return usuarioRepository.findById(actual.id())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(toUserDTO(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no encontrado o inactivo")));
    }

    // ─── REFRESH ──────────────────────────────────────────────────────────────
    // El frontend lo llama mientras hay actividad. Si el usuario deja el CRM abierto sin
    // tocarlo, el token (30 min) caduca y tendrá que volver a entrar.

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@AuthenticationPrincipal AuthenticatedUser actual,
                                     @RequestHeader("Authorization") String authHeader) {
        Optional<TokenInfo> info = JwtService.extraerBearer(authHeader).flatMap(jwtService::validar);
        if (info.isEmpty() || !jwtService.sesionRenovable(info.get())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "La sesión ha alcanzado su duración máxima. Vuelve a iniciar sesión."));
        }

        return usuarioRepository.findById(actual.id())
                .<ResponseEntity<?>>map(usuario -> {
                    TokenEmitido nuevo = jwtService.generarToken(usuario, info.get().authTime());
                    tokenBlacklist.revocar(info.get().jti(), info.get().expiresAt()); // el anterior deja de valer
                    return ResponseEntity.ok(new LoginResponse(nuevo.token(), toUserDTO(usuario),
                            nuevo.expiresAt().toEpochMilli()));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // ─── LOGOUT ───────────────────────────────────────────────────────────────
    // Revoca el token de ESTE dispositivo. Público para que funcione aunque el token ya haya caducado.

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        JwtService.extraerBearer(authHeader)
                .flatMap(jwtService::validar)
                .ifPresent(info -> tokenBlacklist.revocar(info.jti(), info.expiresAt()));
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    /** Cierra la sesión en TODOS los dispositivos (p.ej. si crees que te han robado el acceso). */
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@AuthenticationPrincipal AuthenticatedUser actual) {
        return usuarioRepository.findById(actual.id())
                .<ResponseEntity<?>>map(u -> {
                    u.invalidarTodosLosTokens();
                    usuarioRepository.save(u);
                    log.info("Usuario {} ha cerrado todas sus sesiones", u.getUsername());
                    return ResponseEntity.ok(Map.of("message", "Se han cerrado todas las sesiones"));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // ─── PERFIL ───────────────────────────────────────────────────────────────

    /** El parámetro ?username= que manda el frontend antiguo se ignora: siempre es el usuario del token. */
    @GetMapping("/perfil")
    public ResponseEntity<?> getPerfil(@AuthenticationPrincipal AuthenticatedUser actual) {
        return usuarioRepository.findById(actual.id())
                .<ResponseEntity<?>>map(usuario -> {
                    Map<String, Object> perfil = new HashMap<>();
                    perfil.put("username", usuario.getUsername());
                    perfil.put("nombreCompleto", usuario.getNombreCompleto());
                    perfil.put("email", usuario.getEmail());
                    perfil.put("dni", usuario.getDni());
                    return ResponseEntity.ok(perfil);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado")));
    }

    @PutMapping("/perfil/datos")
    public ResponseEntity<?> actualizarDatos(@AuthenticationPrincipal AuthenticatedUser actual,
                                             @Valid @RequestBody ActualizarPerfilRequest req) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(actual.id());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado"));
        }
        Usuario usuario = usuarioOpt.get();

        if (req.nombreCompleto() != null && !req.nombreCompleto().isBlank()) {
            usuario.setNombreCompleto(req.nombreCompleto().trim());
        }
        if (req.email() != null && !req.email().isBlank()) {
            usuario.setEmail(req.email().trim());
        }
        if (req.dni() != null && !req.dni().isBlank()) {
            Optional<Usuario> dniExistente = usuarioRepository.findByDni(req.dni());
            if (dniExistente.isPresent() && !dniExistente.get().getId().equals(usuario.getId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "El DNI ya está registrado en otro usuario"));
            }
            usuario.setDni(req.dni());
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("message", "Datos actualizados correctamente"));
    }

    @PutMapping("/perfil/username")
    public ResponseEntity<?> cambiarUsername(@AuthenticationPrincipal AuthenticatedUser actual,
                                             @Valid @RequestBody CambiarUsernameRequest req) {
        String nuevo = req.nuevoUsername().trim();
        if (usuarioRepository.findByUsername(nuevo).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El username ya está en uso"));
        }
        return usuarioRepository.findById(actual.id())
                .<ResponseEntity<?>>map(usuario -> {
                    usuario.setUsername(nuevo);
                    usuarioRepository.save(usuario);
                    return ResponseEntity.ok(Map.of("message", "Username actualizado"));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado")));
    }

    /**
     * Cambiar contraseña invalida TODAS las sesiones abiertas (si alguien te robó el token,
     * deja de valer) y devuelve un token nuevo para que esta sesión continúe.
     */
    @PutMapping("/perfil/password")
    public ResponseEntity<?> cambiarPassword(@AuthenticationPrincipal AuthenticatedUser actual,
                                             @Valid @RequestBody CambiarPasswordRequest req) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(actual.id());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado"));
        }
        Usuario usuario = usuarioOpt.get();

        // Un token robado no debe servir para adivinar la contraseña actual a base de intentos
        if (rateLimiter.usuarioBloqueado(usuario.getUsername())) {
            return demasiadosIntentos(rateLimiter.segundosHastaDesbloqueoUsuario(usuario.getUsername()));
        }
        if (!passwordEncoder.matches(req.passwordActual(), usuario.getPassword())) {
            rateLimiter.registrarFallo(usuario.getUsername());
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña actual es incorrecta"));
        }

        usuario.setPassword(passwordEncoder.encode(req.passwordNueva()));
        usuario.invalidarTodosLosTokens();
        usuarioRepository.save(usuario);

        TokenEmitido emitido = jwtService.generarToken(usuario);
        log.info("Usuario {} ha cambiado su contraseña; sesiones anteriores invalidadas", usuario.getUsername());

        return ResponseEntity.ok(Map.of(
                "message", "Contraseña actualizada correctamente",
                "token", emitido.token(),
                "expiresAt", emitido.expiresAt().toEpochMilli()));
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private UserDTO toUserDTO(Usuario usuario) {
        UserDTO user = new UserDTO();
        user.setUsername(usuario.getUsername());
        user.setNombre(usuario.getNombreCompleto());
        user.setRole("ADMIN");
        return user;
    }

    private ResponseEntity<?> demasiadosIntentos(long espera) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(espera))
                .body(Map.of(
                        "error", "Demasiados intentos fallidos. Espera " + espera + " segundos.",
                        "retryAfter", espera));
    }
}
