package com.lavaderosepulveda.app.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    private final ClientIpResolver resolver = new ClientIpResolver("X-Real-IP");

    @Test
    void usaXRealIpCuandoExiste() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Real-IP", "88.1.2.3");
        req.addHeader("X-Forwarded-For", "1.1.1.1, 88.1.2.3");
        assertThat(resolver.resolver(req)).isEqualTo("88.1.2.3");
    }

    @Test
    void sinXRealIpUsaElUltimoValorDeXff_noElQueEscribeElCliente() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "6.6.6.6, 88.1.2.3"); // 6.6.6.6 falsificado por el cliente
        assertThat(resolver.resolver(req)).isEqualTo("88.1.2.3");
    }

    @Test
    void ignoraValoresQueNoSonIps() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Real-IP", "<script>");
        req.setRemoteAddr("10.0.0.5");
        assertThat(resolver.resolver(req)).isEqualTo("10.0.0.5");
    }
}
