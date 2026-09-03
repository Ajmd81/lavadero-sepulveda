package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.HorarioDiaSemana;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import com.lavaderosepulveda.app.model.VehicleModel;
import com.lavaderosepulveda.app.repository.DiaCerradoRepository;
import com.lavaderosepulveda.app.repository.HorarioDiaSemanaRepository;
import com.lavaderosepulveda.app.repository.VehicleModelRepository;
import com.lavaderosepulveda.app.security.CitaRateLimiter;
import com.lavaderosepulveda.app.service.CitaService;
import com.lavaderosepulveda.app.service.HorarioService;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CitaController {

    private static final Logger logger = LoggerFactory.getLogger(CitaController.class);

    @Autowired private CitaService citaService;
    @Autowired private HorarioService horarioService;
    @Autowired private VehicleModelRepository modelRepository;
    @Autowired private DiaCerradoRepository diasCerradoRepository;
    @Autowired private CitaRateLimiter citaRateLimiter;
    @Autowired private HorarioDiaSemanaRepository horarioDiaSemanaRepository;

    // ─── PÁGINA PRINCIPAL ─────────────────────────────────────────────────────

    @GetMapping("/")
    public String index(Model model) {
        // Construye el array JSON de specialOpeningHoursSpecification para el
        // JSON-LD de la home: cada DiaCerrado de los próximos 365 días se convierte
        // en una entrada con opens/closes "00:00" (convención schema.org para cerrado).
        LocalDate hoy = LocalDate.now();
        List<LocalDate> diasCerrados = diasCerradoRepository
                .findFechasByRango(hoy, hoy.plusDays(365));

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < diasCerrados.size(); i++) {
            String fecha = diasCerrados.get(i).toString(); // ISO yyyy-MM-dd
            if (i > 0) sb.append(",");
            sb.append("{\"@type\":\"OpeningHoursSpecification\",")
              .append("\"validFrom\":\"").append(fecha).append("\",")
              .append("\"validThrough\":\"").append(fecha).append("\",")
              .append("\"opens\":\"00:00\",")
              .append("\"closes\":\"00:00\"}");
        }
        sb.append("]");
        model.addAttribute("specialHoursJson", sb.toString());
        return "index";
    }

    // ─── FORMULARIO DE NUEVA CITA ─────────────────────────────────────────────

    @GetMapping("/nueva-cita")
    public String mostrarFormulario(Model model) {
        model.addAttribute("cita", new Cita());
        model.addAttribute("tiposLavado", TipoLavado.values());
        return "formulario";
    }

    // ─── GUARDAR CITA ─────────────────────────────────────────────────────────

    @PostMapping("/guardar-cita")
    public String guardarCita(
            @Valid @ModelAttribute Cita cita,
            BindingResult bindingResult,
            @RequestParam(value = "website", required = false) String honeypot,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest httpRequest) {

        // ① Honeypot
        if (honeypot != null && !honeypot.isBlank()) {
            logger.warn("BOT detectado (honeypot) desde IP: {}", obtenerIpReal(httpRequest));
            return "redirect:/confirmacion";
        }

        // ② Rate limiting por IP
        String ip = obtenerIpReal(httpRequest);
        if (!citaRateLimiter.intentoPermitido(ip)) {
            long espera = citaRateLimiter.segundosHastaReset(ip);
            long minutos = Math.max(1, espera / 60);
            logger.warn("Rate limit superado para IP: {}", ip);
            model.addAttribute("error",
                    "Demasiadas solicitudes. Por favor, espera " + minutos + " minuto(s) antes de intentarlo de nuevo.");
            model.addAttribute("tiposLavado", TipoLavado.values());
            return "formulario";
        }

        // ③ Bean Validation
        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposLavado", TipoLavado.values());
            return "formulario";
        }

        // ④ Rango de fecha
        LocalDate hoy = LocalDate.now();
        if (cita.getFecha().isBefore(hoy)) {
            model.addAttribute("error", "No puedes reservar en una fecha pasada.");
            model.addAttribute("tiposLavado", TipoLavado.values());
            return "formulario";
        }
        if (cita.getFecha().isAfter(hoy.plusDays(60))) {
            model.addAttribute("error", "Solo puedes reservar con un máximo de 60 días de antelación.");
            model.addAttribute("tiposLavado", TipoLavado.values());
            return "formulario";
        }

        try {
            if (!horarioService.esHorarioDisponible(cita.getFecha(), cita.getHora())) {
                model.addAttribute("error", "El horario seleccionado no está disponible.");
                model.addAttribute("tiposLavado", TipoLavado.values());
                return "formulario";
            }

            resolverModeloVehiculo(cita);
            logger.info("GUARDANDO cita para cliente: {} (email: {})", cita.getNombre(), cita.getEmail());

            // crearCita() ya gestiona el envío de email de forma asíncrona internamente
            Cita citaGuardada = citaService.crearCita(cita);
            logger.info("OK: Cita creada exitosamente: ID {}, Cliente: {}",
                    citaGuardada.getId(), citaGuardada.getNombre());

            String fechaFormateada = DateTimeFormatUtils.formatearFechaCompleta(citaGuardada.getFecha());
            String horaFormateada  = DateTimeFormatUtils.formatearHoraCorta(citaGuardada.getHora());

            redirectAttributes.addFlashAttribute("mensaje",
                    "¡Cita reservada con éxito para el " + fechaFormateada + " a las " + horaFormateada + "!");
            redirectAttributes.addFlashAttribute("cita", citaGuardada);

            return "redirect:/confirmacion";

        } catch (Exception e) {
            logger.error("Error al guardar cita: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/nueva-cita";
        }
    }

    // ─── CONFIRMACIÓN ─────────────────────────────────────────────────────────

    @GetMapping("/confirmacion")
    public String confirmacion() {
        return "confirmacion";
    }

    // ─── HORARIOS DISPONIBLES (AJAX) ──────────────────────────────────────────

    @GetMapping("/horarios-disponibles")
    @ResponseBody
    public List<String> obtenerHorariosDisponibles(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            if (diasCerradoRepository.existsByFecha(fecha)) {
                return List.of();
            }
            return horarioService.obtenerHorariosDisponibles(fecha).stream()
                    .map(DateTimeFormatUtils::formatearHoraCorta)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo horarios disponibles para {}: {}", fecha, e.getMessage());
            return List.of();
        }
    }

    @GetMapping("/api/horarios-dia-semana")
    @ResponseBody
    public ResponseEntity<List<HorarioDiaSemana>> obtenerHorariosDiaSemana() {
        try {
            List<HorarioDiaSemana> horarios = horarioDiaSemanaRepository.findAllByOrderByDiaSemanaAsc();
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            logger.error("Error obteniendo horarios por día: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ─── MODELOS (API PÚBLICA) ────────────────────────────────────────────────

    @GetMapping("/api/modelos")
    @ResponseBody
    public List<VehicleModel> obtenerTodosLosModelos() {
        try {
            return modelRepository.findAll().stream()
                    .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo modelos: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // ─── HELPERS PRIVADOS ─────────────────────────────────────────────────────

    private void resolverModeloVehiculo(Cita cita) {
        String valor = cita.getModeloVehiculo();
        if (valor == null || valor.isBlank()) return;
        try {
            Long id = Long.parseLong(valor);
            modelRepository.findById(id)
                    .ifPresent(m -> cita.setModeloVehiculo(m.getName()));
        } catch (NumberFormatException e) {
            // Ya venía como texto — no tocar
        }
    }

    private String obtenerIpReal(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}