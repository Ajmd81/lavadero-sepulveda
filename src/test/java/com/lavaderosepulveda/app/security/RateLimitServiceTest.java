package com.lavaderosepulveda.app.security;

import com.lavaderosepulveda.app.security.RateLimitService.Politica;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitServiceTest {

    private final RateLimitService service = new RateLimitService(60, 300, 240, 6000);

    @Test
    void loginPorIpBloqueaAlSextoIntento() {
        for (int i = 0; i < 5; i++) {
            assertThat(service.permitido(Politica.LOGIN_IP, "1.2.3.4")).isTrue();
        }
        assertThat(service.permitido(Politica.LOGIN_IP, "1.2.3.4")).isFalse();
        assertThat(service.segundosHastaReset(Politica.LOGIN_IP, "1.2.3.4")).isPositive();
        // Otra IP no se ve afectada
        assertThat(service.permitido(Politica.LOGIN_IP, "5.6.7.8")).isTrue();
    }

    @Test
    void fallosPorUsuarioSeReinicianTrasLoginCorrecto() {
        for (int i = 0; i < 10; i++) service.consumir(Politica.LOGIN_USUARIO, "admin");
        assertThat(service.bloqueado(Politica.LOGIN_USUARIO, "admin")).isTrue();
        service.reiniciar(Politica.LOGIN_USUARIO, "admin");
        assertThat(service.bloqueado(Politica.LOGIN_USUARIO, "admin")).isFalse();
    }

    @Test
    void rafagaAnonimaLimitadaA20En5Segundos() {
        int permitidas = 0;
        for (int i = 0; i < 30; i++) {
            if (service.permitido(Politica.API_IP_ANONIMO, "9.9.9.9")) permitidas++;
        }
        assertThat(permitidas).isEqualTo(20);
    }
}
