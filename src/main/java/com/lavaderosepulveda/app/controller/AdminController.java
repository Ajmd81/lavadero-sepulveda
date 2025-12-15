package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.ClienteEstadisticaDTO;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.EstadoCita;
import com.lavaderosepulveda.app.model.VehicleModel;
import com.lavaderosepulveda.app.repository.VehicleModelRepository;
import com.lavaderosepulveda.app.service.CitaService;
import com.lavaderosepulveda.app.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CitaService citaService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VehicleModelRepository vehicleModelRepository;

    /**
     * Página de login
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {

        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }

        if (logout != null) {
            model.addAttribute("mensaje", "Sesión cerrada correctamente");
        }

        return "admin/login";
    }

    /**
     * Listado general de citas agrupadas por fecha
     */
    @GetMapping("/listado-citas")
    public String listadoCitas(Model model, RedirectAttributes redirectAttributes) {
        try {
            Map<String, List<Cita>> citasPorFecha = citaService.obtenerCitasAgrupadasPorFechaFormateada();
            model.addAttribute("citasPorFecha", citasPorFecha);
            return "admin/listado-citas";
        } catch (Exception e) {
            logger.error("Error al cargar listado de citas: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al cargar el listado de citas");
            return "redirect:/admin/login";
        }
    }

    /**
     * Listado de citas filtradas por estado
     */
    @GetMapping("/citas-por-estado")
    public String citasPorEstado(@RequestParam(required = false) EstadoCita estado, Model model) {
        try {
            List<Cita> todasLasCitas = citaService.obtenerTodasLasCitas();

            Map<EstadoCita, List<Cita>> citasPorEstado;

            if (estado != null) {
                // Filtrar por estado específico
                citasPorEstado = todasLasCitas.stream()
                        .filter(cita -> cita.getEstado() == estado)
                        .collect(Collectors.groupingBy(Cita::getEstado));
            } else {
                // Mostrar todas agrupadas por estado
                citasPorEstado = todasLasCitas.stream()
                        .collect(Collectors.groupingBy(Cita::getEstado));
            }

            model.addAttribute("citasPorEstado", citasPorEstado);
            model.addAttribute("estadoSeleccionado", estado);

            return "admin/citas-por-estado";
        } catch (Exception e) {
            logger.error("Error al cargar citas por estado: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar las citas por estado");
            return "admin/listado-citas";
        }
    }

    /**
     * Listado de clientes que no se presentaron
     */
    @GetMapping("/clientes-no-presentados")
    public String clientesNoPresentados(Model model) {
        try {
            List<Cita> citasNoPresentados = citaService.obtenerTodasLasCitas().stream()
                    .filter(cita -> cita.getEstado() == EstadoCita.NO_PRESENTADO)
                    .collect(Collectors.toList());

            model.addAttribute("citas", citasNoPresentados);
            return "admin/clientes-no-presentados";
        } catch (Exception e) {
            logger.error("Error al cargar clientes no presentados: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar el listado");
            return "admin/listado-citas";
        }
    }

    /**
     * Actualizar el estado de una cita
     */
    @PostMapping("/actualizar-estado/{id}")
    public String actualizarEstado(@PathVariable Long id,
                                   @RequestParam EstadoCita estado,
                                   @RequestParam(required = false) String observaciones,
                                   RedirectAttributes redirectAttributes) {
        try {
            Cita cita = citaService.obtenerCitaPorId(id)
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

            EstadoCita estadoAnterior = cita.getEstado();
            cita.setEstado(estado);

            if (observaciones != null && !observaciones.trim().isEmpty()) {
                cita.setObservaciones(observaciones);
            }

            citaService.actualizarCita(id, cita);

            logger.info("Estado de cita {} actualizado de {} a {}", id, estadoAnterior, estado);

            redirectAttributes.addFlashAttribute("mensaje",
                    "Estado actualizado correctamente a: " + estado.getDescripcion());

        } catch (Exception e) {
            logger.error("Error al actualizar estado de cita {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Error al actualizar el estado: " + e.getMessage());
        }

        return "redirect:/admin/listado-citas";
    }

    /**
     * Registrar pago por adelantado de una cita
     */
    @PostMapping("/registrar-pago/{id}")
    public String registrarPago(@PathVariable Long id,
                                @RequestParam String referenciaPago,
                                RedirectAttributes redirectAttributes) {
        try {
            Cita cita = citaService.obtenerCitaPorId(id)
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

            // Registrar el pago
            cita.setPagoAdelantado(true);
            cita.setReferenciaPago(referenciaPago);
            cita.setEstado(EstadoCita.CONFIRMADA);

            citaService.actualizarCita(id, cita);

            logger.info("Pago registrado para cita {}: {}", id, referenciaPago);

            // Enviar email de confirmación de pago
            try {
                if (emailService.isServicioDisponible()) {
                    emailService.enviarEmailConfirmacion(cita);
                    logger.info("Email de confirmación de pago enviado para cita {}", id);
                }
            } catch (Exception emailEx) {
                logger.warn("No se pudo enviar email de confirmación de pago: {}", emailEx.getMessage());
            }

            redirectAttributes.addFlashAttribute("mensaje",
                    "Pago registrado correctamente. Cita confirmada.");

        } catch (Exception e) {
            logger.error("Error al registrar pago para cita {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Error al registrar el pago: " + e.getMessage());
        }

        return "redirect:/admin/listado-citas";
    }

    /**
     * Eliminar una cita
     */
    @GetMapping("/eliminar-cita/{id}")
    public String eliminarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            citaService.eliminarCita(id);

            logger.info("Cita {} eliminada correctamente", id);

            redirectAttributes.addFlashAttribute("mensaje", "Cita eliminada correctamente");

        } catch (Exception e) {
            logger.error("Error al eliminar cita {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Error al eliminar la cita: " + e.getMessage());
        }

        return "redirect:/admin/listado-citas";
    }

    /**
     * Ver modelos de vehículos registrados
     */
    @GetMapping("/modelos-vehiculos")
    public String modelosVehiculos(Model model) {
        try {
            List<VehicleModel> modelos = vehicleModelRepository.findAll();

            // Agrupar modelos por categoría
            Map<String, List<VehicleModel>> modelosPorCategoria = modelos.stream()
                    .collect(Collectors.groupingBy(
                            vm -> vm.getCategory().getName(),
                            Collectors.toList()
                    ));

            model.addAttribute("modelosPorCategoria", modelosPorCategoria);
            return "admin/modelos-vehiculos";

        } catch (Exception e) {
            logger.error("Error al cargar modelos de vehículos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar los modelos de vehículos");
            return "admin/listado-citas";
        }
    }

    /**
     * Dashboard con estadísticas (opcional)
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            List<Cita> todasLasCitas = citaService.obtenerTodasLasCitas();

            // Estadísticas básicas
            long totalCitas = todasLasCitas.size();
            long citasPendientes = todasLasCitas.stream()
                    .filter(c -> c.getEstado() == EstadoCita.PENDIENTE)
                    .count();
            long citasConfirmadas = todasLasCitas.stream()
                    .filter(c -> c.getEstado() == EstadoCita.CONFIRMADA)
                    .count();
            long citasCompletadas = todasLasCitas.stream()
                    .filter(c -> c.getEstado() == EstadoCita.COMPLETADA)
                    .count();
            long citasNoPresentados = todasLasCitas.stream()
                    .filter(c -> c.getEstado() == EstadoCita.NO_PRESENTADO)
                    .count();

            model.addAttribute("totalCitas", totalCitas);
            model.addAttribute("citasPendientes", citasPendientes);
            model.addAttribute("citasConfirmadas", citasConfirmadas);
            model.addAttribute("citasCompletadas", citasCompletadas);
            model.addAttribute("citasNoPresentados", citasNoPresentados);

            return "admin/dashboard";

        } catch (Exception e) {
            logger.error("Error al cargar dashboard: {}", e.getMessage(), e);
            return "redirect:/admin/listado-citas";
        }
    }

    /**
     * Configuración (para futuras implementaciones)
     */
    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        // Por ahora redirige al listado
        // Aquí podrías agregar configuración de horarios, precios, etc.
        return "redirect:/admin/listado-citas";
    }

    /**
     * Estadísticas - Top 10 clientes del último año
     */
    @GetMapping("/estadisticas")
    public String estadisticas(Model model) {
        try {
            logger.info("Accediendo a estadísticas...");

            // Obtener top 10 clientes
            List<ClienteEstadisticaDTO> top10Clientes = citaService.obtenerTop10ClientesUltimoAnio();
            logger.info("Top 10 clientes obtenidos: {}", top10Clientes.size());

            // Obtener estadísticas generales
            Map<String, Object> estadisticasGenerales = citaService.obtenerEstadisticasGenerales();
            logger.info("Estadísticas generales obtenidas: {}", estadisticasGenerales);

            // Agregar al modelo
            model.addAttribute("top10Clientes", top10Clientes);
            model.addAttribute("estadisticas", estadisticasGenerales);

            logger.info("Cargadas estadísticas: {} clientes en top 10", top10Clientes.size());

            return "admin/estadisticas";

        } catch (Exception e) {
            logger.error("Error al cargar estadísticas: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar las estadísticas: " + e.getMessage());
            return "redirect:/admin/listado-citas";
        }
    }
}