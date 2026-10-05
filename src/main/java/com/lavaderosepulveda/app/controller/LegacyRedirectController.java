package com.lavaderosepulveda.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;
import java.util.Map;

/**
 * Redirige con 301 las URLs de la web antigua (.html) a las rutas actuales,
 * para que Google traspase su autoridad y deje de mostrar páginas viejas.
 */
@Controller
public class LegacyRedirectController {

    private static final Map<String, String> REDIRECCIONES = Map.ofEntries(
            Map.entry("/index.html", "/"),
            Map.entry("/index.php", "/"),
            Map.entry("/contacto.html", "/#contacto"),
            Map.entry("/horario.html", "/horario"),
            Map.entry("/horarios.html", "/horario"),
            Map.entry("/galeria.html", "/galeria"),
            Map.entry("/productos.html", "/productos"),
            Map.entry("/tarifas.html", "/tarifas"),
            Map.entry("/precios.html", "/tarifas"),
            Map.entry("/reservas.html", "/nueva-cita"),
            Map.entry("/cita.html", "/nueva-cita")
    );

    @GetMapping({
            "/index.html", "/index.php", "/contacto.html", "/horario.html", "/horarios.html",
            "/galeria.html", "/productos.html", "/tarifas.html", "/precios.html",
            "/reservas.html", "/cita.html"
    })
    public ResponseEntity<Void> redirigir(HttpServletRequest request) {
        String destino = REDIRECCIONES.getOrDefault(request.getRequestURI(), "/");
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("https://www.lavaderosepulveda.es" + destino));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }
}
