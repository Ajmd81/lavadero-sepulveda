package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.TipoLavado;
import com.lavaderosepulveda.app.service.CitaService;
import com.lavaderosepulveda.app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private EmailService emailService;

    // Página principal
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Mostrar formulario para crear una cita
    @GetMapping("/nueva-cita")
    public String mostrarFormulario(Model model) {
        model.addAttribute("cita", new Cita());
        model.addAttribute("tiposLavado", TipoLavado.values());
        return "formulario";
    }


    // Procesar el formulario para crear una cita
    @PostMapping("/guardar-cita")
    public String guardarCita(@RequestParam(value="tipoLavadoName", required=false) String tipoLavadoName,
                              @Valid @ModelAttribute Cita cita, BindingResult bindingResult,
                              Model model, RedirectAttributes redirectAttributes) {

        if (tipoLavadoName != null && !tipoLavadoName.isEmpty()) {
            try {
                TipoLavado tipoLavado = TipoLavado.valueOf(tipoLavadoName);
                cita.setTipoLavado(tipoLavado);
            } catch (IllegalArgumentException e) {
                bindingResult.rejectValue("tipoLavado", "error.tipoLavado", "Tipo de lavado inválido");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposLavado", TipoLavado.values());
            return "formulario";
        }

        try {
            // Calcular el precio total y depósito basados en el tipo de lavado seleccionado
            if (cita.getTipoLavado() != null) {
                BigDecimal precioTotal = cita.getTipoLavado().getPrecio();
                BigDecimal deposito = precioTotal.multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);

                cita.setPrecioTotal(precioTotal);
                cita.setDeposito(deposito);
                cita.setDepositoPagado(false); // Por defecto, el depósito no está pagado
            }

            Cita citaGuardada = citaService.crearCita(cita);

            // Enviar email con instrucciones de pago
            emailService.enviarEmailReservaPendiente(citaGuardada);

            redirectAttributes.addFlashAttribute("mensaje", "¡Reserva pre-registrada! Por favor, realiza el pago del depósito mediante Bizum para confirmarla.");
            redirectAttributes.addFlashAttribute("cita", citaGuardada);
            return "redirect:/instrucciones-pago";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/nueva-cita";
        }
    }

    // Página de confirmación
    @GetMapping("/confirmacion")
    public String confirmacion() {
        return "confirmacion";
    }

    // Endpoint para obtener horarios disponibles (usado por AJAX)
    @GetMapping("/horarios-disponibles")
    @ResponseBody
    public List<LocalTime> obtenerHorariosDisponibles(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citaService.obtenerHorariosDisponibles(fecha);
    }

    // Endpoint para ver las citas
    @GetMapping("/admin/listado-citas")
    public String listarTodasLasCitas(Model model) {
        // Obtener la fecha actual
        LocalDate fechaActual = LocalDate.now();
        LocalTime horaActual = LocalTime.now();

        // Obtener todas las citas
        List<Cita> citas = citaService.obtenerTodasLasCitas();

        // Filtrar citas pendientes (futuras)
        List<Cita> citasPendientes = citas.stream()
                .filter(cita -> cita.getFecha().isAfter(fechaActual) ||
                        (cita.getFecha().isEqual(fechaActual) && cita.getHora().isAfter(horaActual)))
                .collect(Collectors.toList());

        // Agrupar las citas pendientes por fecha y ordenar por hora
        Map<LocalDate, List<Cita>> citasPorFechaDesordenado = citasPendientes.stream()
                .collect(Collectors.groupingBy(
                        Cita::getFecha,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    list.sort(Comparator.comparing(Cita::getHora));
                                    return list;
                                }
                        )
                ));

        // Crear un nuevo mapa ordenado por fecha (ascendente)
        Map<LocalDate, List<Cita>> citasPorFechaOrdenado = new TreeMap<>(Comparator.naturalOrder());
        citasPorFechaOrdenado.putAll(citasPorFechaDesordenado);

        // Convertir a un mapa con fechas formateadas como cadenas
        Map<String, List<Cita>> citasPorFechaFormateado = new LinkedHashMap<>();

        // Formatear cada fecha al formato deseado (dd/MM/yyyy)
        for (Map.Entry<LocalDate, List<Cita>> entry : citasPorFechaOrdenado.entrySet()) {
            String fechaFormateada = entry.getKey().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            citasPorFechaFormateado.put(fechaFormateada, entry.getValue());
        }

        // Obtener citas históricas (pasadas)
        List<Cita> citasHistoricas = citas.stream()
                .filter(cita -> cita.getFecha().isBefore(fechaActual) ||
                        (cita.getFecha().isEqual(fechaActual) && cita.getHora().isBefore(horaActual)))
                .sorted(Comparator.comparing(Cita::getFecha).reversed()
                        .thenComparing(Cita::getHora))
                .collect(Collectors.toList());

        // Obtener clientes con 2 o más faltas
        List<Cita> clientesConFaltas = citas.stream()
                .filter(cita -> cita.getFaltas() != null && cita.getFaltas() >= 2)
                .collect(Collectors.toList());

        // Pasar todos los datos al modelo
        model.addAttribute("citasPorFecha", citasPorFechaFormateado);
        model.addAttribute("citasHistoricas", citasHistoricas);
        model.addAttribute("clientesConFaltas", clientesConFaltas);

        return "admin/listado-citas";
    }

    // Endpoint para eliminar las citas
    @GetMapping("/admin/eliminar-cita/{id}")
    public String eliminarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            citaService.eliminarCita(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cita eliminada con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la cita: " + e.getMessage());
        }
        return "redirect:/admin/listado-citas";
    }

    // Nuevo endpoint para marcar asistencia
    @GetMapping("/admin/marcar-asistencia/{id}/{asistio}")
    public String marcarAsistencia(@PathVariable Long id, @PathVariable Boolean asistio,
                                   RedirectAttributes redirectAttributes) {
        try {
            Optional<Cita> citaOpt = citaService.obtenerCitaPorId(id);

            if (citaOpt.isPresent()) {
                Cita cita = citaOpt.get();

                // Verificar que el depósito está pagado
                if (!cita.getDepositoPagado()) {
                    redirectAttributes.addFlashAttribute("error",
                            "No se puede registrar asistencia para una cita sin depósito pagado.");
                    return "redirect:/admin/listado-citas";
                }

                cita.setAsistida(asistio);

                // Si no asistió, incrementar contador de faltas
                if (!asistio) {
                    if (cita.getFaltas() == null) {
                        cita.setFaltas(1);
                    } else {
                        cita.setFaltas(cita.getFaltas() + 1);
                    }

                    // Enviar email informando de la pérdida del depósito
                    emailService.enviarEmailNoAsistencia(cita);

                    redirectAttributes.addFlashAttribute("mensaje",
                            "Se ha registrado la falta de asistencia. El cliente ha perdido su depósito de " +
                                    cita.getDeposito() + "€.");
                } else {
                    // Si asistió, enviar email de agradecimiento
                    emailService.enviarEmailAgradecimiento(cita);

                    redirectAttributes.addFlashAttribute("mensaje",
                            "Se ha registrado la asistencia correctamente. Al cliente se le descontará el depósito de " +
                                    cita.getDeposito() + "€ del precio total.");
                }

                citaService.actualizarCita(id, cita);
            } else {
                redirectAttributes.addFlashAttribute("error", "No se encontró la cita con ID " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar asistencia: " + e.getMessage());
        }
        return "redirect:/admin/listado-citas";
    }
    // Añadir estos endpoints al CitaController.java

    // Página de instrucciones de pago
    @GetMapping("/instrucciones-pago")
    public String instruccionesPago(Model model) {
        return "instrucciones-pago";
    }

    // Endpoint para confirmar pago de depósito
    @PostMapping("/confirmar-pago")
    public String confirmarPago(@RequestParam("citaId") Long citaId,
                                @RequestParam("referenciaBizum") String referenciaBizum,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Cita> citaOpt = citaService.obtenerCitaPorId(citaId);
            if (citaOpt.isPresent()) {
                Cita cita = citaOpt.get();
                cita.setDepositoPagado(true);
                cita.setReferenciaBizum(referenciaBizum);
                cita.setFechaPagoDeposito(LocalDate.now());

                citaService.actualizarCita(citaId, cita);

                // Enviar email de confirmación final
                emailService.enviarEmailConfirmacion(cita);

                redirectAttributes.addFlashAttribute("mensaje", "¡Pago confirmado! Tu cita ha sido reservada. Hemos enviado un email de confirmación.");
                return "redirect:/confirmacion";
            } else {
                redirectAttributes.addFlashAttribute("error", "No se encontró la cita especificada.");
                return "redirect:/";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al confirmar el pago: " + e.getMessage());
            return "redirect:/";
        }
    }

    // Endpoint para confirmar pago del depósito desde el panel de administración
    @GetMapping("/admin/confirmar-deposito/{id}")
    public String confirmarDepositoAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Cita> citaOpt = citaService.obtenerCitaPorId(id);
            if (citaOpt.isPresent()) {
                Cita cita = citaOpt.get();
                cita.setDepositoPagado(true);
                cita.setFechaPagoDeposito(LocalDate.now());

                citaService.actualizarCita(id, cita);

                redirectAttributes.addFlashAttribute("mensaje", "Pago del depósito confirmado para la cita #" + id);
            } else {
                redirectAttributes.addFlashAttribute("error", "No se encontró la cita especificada.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al confirmar el pago: " + e.getMessage());
        }
        return "redirect:/admin/listado-citas";
    }
}