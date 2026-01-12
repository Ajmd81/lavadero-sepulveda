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

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
            Cita citaGuardada = citaService.crearCita(cita);
            logger.info("Cita creada exitosamente: ID {}, Cliente: {}",
                    citaGuardada.getId(), citaGuardada.getNombre());

            // Enviar email si el servicio está disponible
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
        if (emailService != null && emailService.isServicioDisponible()) {
            try {
                if (cita.getEmail() != null && !cita.getEmail().trim().isEmpty()) {
                    emailService.enviarEmailConfirmacion(cita);
                    logger.info("Email de confirmación enviado a: {}", cita.getEmail());
                } else {
                    logger.debug("No se envía email: dirección vacía para cita ID {}", cita.getId());
                }
            } catch (Exception e) {
                // Error en email no debe afectar la creación de la cita
                logger.warn("Error enviando email de confirmación para cita ID {}: {}",
                        cita.getId(), e.getMessage());
            }
        } else {
            logger.debug("Servicio de email no disponible - no se envía confirmación");
        }
    }
}