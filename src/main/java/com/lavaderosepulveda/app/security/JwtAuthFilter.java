package com.lavaderosepulveda.app.security;

import com.lavaderosepulveda.app.repository.auth.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Autentica la petición a partir del JWT. Un token solo es válido si:
 *  1. Firma, issuer y expiración son correctos (JwtService).
 *  2. No ha sido revocado por logout (TokenBlacklist).
 *  3. El usuario existe y está activo en BD.
 *  4. Su "ver" coincide con Usuario.tokenVersion (cambio de contraseña / cerrar todas las sesiones).
 *
 * Si falla cualquier punto la petición sigue como anónima; Spring Security devolverá 401
 * si el endpoint requiere autenticación.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final TokenBlacklist tokenBlacklist;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            JwtService.extraerBearer(request.getHeader("Authorization"))
                    .flatMap(jwtService::validar)
                    .filter(info -> !tokenBlacklist.estaRevocado(info.jti()))
                    .ifPresent(info -> usuarioRepository.findById(info.userId())
                            .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                            .filter(u -> JwtService.versionDe(u) == info.version())
                            .ifPresent(usuario -> {
                                var principal = new AuthenticatedUser(usuario.getId(), usuario.getUsername(), info.jti());
                                var auth = new UsernamePasswordAuthenticationToken(
                                        principal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            }));
        }

        filterChain.doFilter(request, response);
    }
}
