package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import com.lavaderosepulveda.app.model.VehicleModel;
import com.lavaderosepulveda.app.repository.VehicleModelRepository;
import com.lavaderosepulveda.app.service.CitaService;
import com.lavaderosepulveda.app.service.EmailService;
import com.lavaderosepulveda.app.service.HorarioService;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador refactorizado para la interfaz web PÚBLICA de citas
 * SOLO contiene endpoints públicos - Los endpoints /admin/* están en AdminController
 * Usa los nuevos services y utilities para eliminar duplicación
 */
@Controller
public class CitaController {

    private static final Logger logger = LoggerFactory.getLogger(CitaController.class);

    @Autowired
    private CitaService citaService;

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VehicleModelRepository modelRepository;

    /**
     * Página principal
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Mostrar formulario para crear una cita
     */
    @GetMapping("/nueva-cita")
    public String mostrarFormulario(Model model) {
        model.addAttribute("cita", new Cita());
        model.addAttribute("tiposLavado", TipoLavado.values());
        return "formulario";
    }

    /**
     * Procesar el formulario para crear una cita - Refactorizado
     */
    @PostMapping("/guardar-cita")
    public String guardarCita(@Valid @ModelAttribute Cita cita,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposLavado", TipoLavado.values());
            return "formulario";
        }

        try {
            // Validaciones adicionales usando HorarioService
            if (!horarioService.esHorarioDisponible(cita.getFecha(), cita.getHora())) {
                model.addAttribute("error", "El horario seleccionado no está disponible");
                model.addAttribute("tiposLavado", TipoLavado.values());
                return "formulario";
            }

            // Crear cita usando servicio refactorizado
            logger.info("GUARDANDO cita para cliente: {} (email: {})", cita.getNombre(), cita.getEmail());
            Cita citaGuardada = citaService.crearCita(cita);
            logger.info("OK: Cita creada exitosamente: ID {}, Cliente: {}",
                    citaGuardada.getId(), citaGuardada.getNombre());

            // Enviar email si el servicio está disponible (redundante pero como respaldo)
            logger.info("PROCESANDO envio de email desde controlador como respaldo...");
            enviarEmailConfirmacionSiEsPosible(citaGuardada);

            // Preparar mensajes para la vista usando DateTimeFormatUtils
            String fechaFormateada = DateTimeFormatUtils.formatearFechaCompleta(citaGuardada.getFecha());
            String horaFormateada = DateTimeFormatUtils.formatearHoraCorta(citaGuardada.getHora());

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

    /**
     * Página de confirmación
     */
    @GetMapping("/confirmacion")
    public String confirmacion() {
        return "confirmacion";
    }

    /**
     * Endpoint AJAX para obtener horarios disponibles - Simplificado
     * Usa HorarioService en lugar de lógica duplicada
     */
    @GetMapping("/horarios-disponibles")
    @ResponseBody
    public List<String> obtenerHorariosDisponibles(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        try {
            // Usar servicio especializado en horarios
            List<LocalTime> horariosDisponibles = horarioService.obtenerHorariosDisponibles(fecha);

            // Convertir a strings usando utility centralizada
            List<String> horariosFormateados = horariosDisponibles.stream()
                    .map(DateTimeFormatUtils::formatearHoraCorta)
                    .collect(Collectors.toList());

            logger.debug("Horarios disponibles para {}: {}", fecha, horariosFormateados);
            return horariosFormateados;

        } catch (Exception e) {
            logger.error("Error obteniendo horarios disponibles para {}: {}", fecha, e.getMessage());
            return List.of(); // Retornar lista vacía en caso de error
        }
    }

    /**
     * API PÚBLICA para obtener todos los modelos (usado por JavaScript en el formulario)
     * Este endpoint es público y no causa conflicto
     */
    @GetMapping("/api/modelos")
    @ResponseBody
    public List<VehicleModel> obtenerTodosLosModelos() {
        try {
            return modelRepository.findAll().stream()
                    .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo modelos para API: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Método privado para envío de email - Centralizado
     */
    private void enviarEmailConfirmacionSiEsPosible(Cita cita) {
        logger.info("VERIFICANDO disponibilidad de EmailService...");
        
        if (emailService == null) {
            logger.error("ERROR: EmailService es NULL en CitaController - Verificar inyección");
            return;
        }
        
        if (!emailService.isServicioDisponible()) {
            logger.error("ERROR: Servicio de email NO DISPONIBLE. Estado: {}", emailService.obtenerEstadoConfiguracion());
            return;
        }
        
        logger.info("OK: EmailService disponible. Procesando envío...");
        
        try {
            if (cita.getEmail() != null && !cita.getEmail().trim().isEmpty()) {
                logger.info("ENVIANDO email a: {}", cita.getEmail());
                emailService.enviarEmailConfirmacion(cita);
                logger.info("OK: Email de confirmación enviado a: {}", cita.getEmail());
            } else {
                logger.error("ERROR: Email vacío/nulo para cita ID {}: '{}'", cita.getId(), cita.getEmail());
            }
        } catch (Exception e) {
            // Error en email no debe afectar la creación de la cita
            logger.error("ERROR al enviar email para cita ID {}: {} | Causa: {} | Estado: {}",
                    cita.getId(), e.getMessage(), e.getCause(), emailService.obtenerEstadoConfiguracion(), e);
        }
    }

    /**
     * Endpoint de diagnóstico para verificar estado de email
     * Accede en: http://localhost:8080/diagnostico-email
     */
    @GetMapping("/diagnostico-email")
    @ResponseBody
    public Map<String, String> diagnosticoEmail() {
        Map<String, String> diagnostico = new java.util.LinkedHashMap<>();

        if (emailService != null) {
            diagnostico.put("estado", "EmailService inyectado correctamente");
            diagnostico.put("disponible", String.valueOf(emailService.isServicioDisponible()));
            diagnostico.put("configuracion", emailService.obtenerEstadoConfiguracion());
        } else {
            diagnostico.put("estado", "ERROR: EmailService no inyectado");
            diagnostico.put("disponible", "false");
            diagnostico.put("configuracion", "No disponible");
        }

        logger.info("Diagnóstico de email solicitado: {}", diagnostico);
        return diagnostico;
    }
}