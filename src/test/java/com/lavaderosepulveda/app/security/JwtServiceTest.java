package com.lavaderosepulveda.app.security;

import com.lavaderosepulveda.app.model.auth.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRETO = "x".repeat(64);
    private static final String OTRO_SECRETO = "y".repeat(64);

    private Usuario usuario(long id, String username, int version) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername(username);
        u.setTokenVersion(version);
        return u;
    }

    @Test
    void rechazaSecretosCortos() {
        assertThatThrownBy(() -> new JwtService("corto", 30, 8))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tokenValidoContieneIdVersionYJti() {
        JwtService jwt = new JwtService(SECRETO, 30, 8);
        JwtService.TokenEmitido t = jwt.generarToken(usuario(7, "antonio", 3));

        JwtService.TokenInfo info = jwt.validar(t.token()).orElseThrow();
        assertThat(info.userId()).isEqualTo(7L);
        assertThat(info.username()).isEqualTo("antonio");
        assertThat(info.version()).isEqualTo(3);
        assertThat(info.jti()).isNotBlank();
        assertThat(t.expiresAt()).isBefore(Instant.now().plus(31, ChronoUnit.MINUTES));
    }

    @Test
    void tokenFirmadoConOtraClaveNoEsValido() {
        String token = new JwtService(OTRO_SECRETO, 30, 8).generarToken(usuario(1, "a", 0)).token();
        assertThat(new JwtService(SECRETO, 30, 8).validar(token)).isEmpty();
    }

    @Test
    void tokenManipuladoNoEsValido() {
        JwtService jwt = new JwtService(SECRETO, 30, 8);
        String token = jwt.generarToken(usuario(1, "a", 0)).token();
        String manipulado = token.substring(0, token.length() - 2) + "xx";
        assertThat(jwt.validar(manipulado)).isEmpty();
        assertThat(jwt.validar("")).isEmpty();
        assertThat(jwt.validar(null)).isEmpty();
    }

    @Test
    void laRenovacionNoSuperaElMaximoDeSesion() {
        JwtService jwt = new JwtService(SECRETO, 30, 8);
        // Login hace 7h50m: el token renovado debe caducar a las 8h, no 30 min después
        Instant login = Instant.now().minus(470, ChronoUnit.MINUTES);
        JwtService.TokenEmitido t = jwt.generarToken(usuario(1, "a", 0), login);
        assertThat(t.expiresAt()).isBeforeOrEqualTo(login.plus(8, ChronoUnit.HOURS));

        JwtService.TokenInfo info = jwt.validar(t.token()).orElseThrow();
        assertThat(jwt.sesionRenovable(info)).isTrue();
    }

    @Test
    void sesionExcedidaNoEsRenovable() {
        JwtService jwt = new JwtService(SECRETO, 30, 8);
        JwtService.TokenInfo info = new JwtService.TokenInfo(1L, "a", 0,
                Instant.now().minus(9, ChronoUnit.HOURS), "jti", Instant.now().plusSeconds(60));
        assertThat(jwt.sesionRenovable(info)).isFalse();
    }

    @Test
    void extraerBearer() {
        assertThat(JwtService.extraerBearer("Bearer abc")).contains("abc");
        assertThat(JwtService.extraerBearer("Basic abc")).isEmpty();
        assertThat(JwtService.extraerBearer(null)).isEmpty();
        assertThat(JwtService.extraerBearer("Bearer ")).isEmpty();
    }
}
