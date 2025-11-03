package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.CitaDTO;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.TipoLavado;
import com.lavaderosepulveda.app.service.CitaService;
import com.lavaderosepulveda.app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class CitaApiController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/citas")
    public List<Cita> listarCitas() {
        return citaService.obtenerTodasLasCitas();
    }

    @GetMapping("/citas/{id}")
    public ResponseEntity<Optional<Cita>> obtenerCitaPorId(@PathVariable Long id) {
        try {
            Optional<Cita> cita = citaService.obtenerCitaPorId(id);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/citas")
    public ResponseEntity<?> crearCita(@RequestBody CitaDTO citaDTO) {
        try {
            // Imprimir el DTO recibido para depuración
            System.out.println("Recibida solicitud para crear cita: " + citaDTO);

            // Validación básica de campos obligatorios
            if (citaDTO.getNombre() == null || citaDTO.getFecha() == null || citaDTO.getHora() == null) {
                return ResponseEntity.badRequest().body("Los campos nombre, fecha y hora son obligatorios");
            }

            // Convertir DTO a entidad
            Cita cita = new Cita();

            try {
                // Convertir fecha String a LocalDate
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate fecha = LocalDate.parse(citaDTO.getFecha(), dateFormatter);

                // Convertir hora String a LocalTime
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime hora = LocalTime.parse(citaDTO.getHora(), timeFormatter);

                // Convertir String a Enum TipoLavado
                TipoLavado tipoLavado;
                try {
                    tipoLavado = TipoLavado.valueOf(citaDTO.getTipoLavado().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Tipo de lavado no válido: " + citaDTO.getTipoLavado());
                }

                // Configurar la entidad
                if (citaDTO.getId() != null) {
                    cita.setId(citaDTO.getId());
                }
                cita.setNombre(citaDTO.getNombre());
                cita.setEmail(citaDTO.getEmail() != null ? citaDTO.getEmail() : "");
                cita.setTelefono(citaDTO.getTelefono());
                cita.setModeloVehiculo(citaDTO.getModeloVehiculo());
                cita.setTipoLavado(tipoLavado);
                cita.setFecha(fecha);
                cita.setHora(hora);

            } catch (DateTimeParseException e) {
                e.printStackTrace();
                String mensaje = "Error al procesar fecha u hora: " + e.getMessage();
                System.err.println(mensaje);
                return ResponseEntity.badRequest().body(mensaje);
            } catch (Exception e) {
                e.printStackTrace();
                String mensaje = "Error al procesar datos: " + e.getMessage();
                System.err.println(mensaje);
                return ResponseEntity.badRequest().body(mensaje);
            }

            // Guardar cita
            try {
                Cita nuevaCita = citaService.crearCita(cita);
                System.out.println("Cita creada exitosamente: " + nuevaCita);

                // Enviar email de confirmación si el email no está vacío
                if (nuevaCita.getEmail() != null && !nuevaCita.getEmail().isEmpty()) {
                    try {
                        emailService.enviarEmailConfirmacion(nuevaCita);
                        System.out.println("Email de confirmación enviado a: " + nuevaCita.getEmail());
                    } catch (Exception emailError) {
                        // Log del error pero continuamos, no afecta a la creación de la cita
                        System.err.println("Error al enviar email de confirmación: " + emailError.getMessage());
                        emailError.printStackTrace();
                    }
                }

                return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
            } catch (Exception e) {
                e.printStackTrace();
                String mensaje = "Error al guardar la cita: " + e.getMessage();
                System.err.println(mensaje);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mensaje);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado: " + e.getMessage());
        }
    }

    @GetMapping("/citas/horarios-disponibles")
    public ResponseEntity<List<String>> obtenerHorariosDisponibles(
            @RequestParam("fecha") String fechaStr) {
        try {
            // Convertir String a LocalDate
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fecha = LocalDate.parse(fechaStr, formatter);

            // Obtener los horarios disponibles usando el método existente
            List<LocalTime> horariosDisponibles = citaService.obtenerHorariosDisponibles(fecha);

            // Filtro adicional para excluir la hora 14:00 específicamente para la API
            horariosDisponibles = horariosDisponibles.stream()
                    .filter(hora -> hora.getHour() != 14)
                    .collect(Collectors.toList());

            // Convertir LocalTime a String en formato HH:mm
            List<String> horariosFormateados = new ArrayList<>();
            for (LocalTime hora : horariosDisponibles) {
                horariosFormateados.add(hora.format(DateTimeFormatter.ofPattern("HH:mm")));
            }

            // Imprimir para depuración
            System.out.println("Horarios disponibles enviados: " + horariosFormateados);

            return ResponseEntity.ok(horariosFormateados);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/citas/{id}")
    public ResponseEntity<Void> eliminarCita(@PathVariable Long id) {
        try {
            citaService.eliminarCita(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/citas/verificar-disponibilidad")
    public ResponseEntity<Boolean> verificarDisponibilidad(
            @RequestParam("fecha") String fechaStr,
            @RequestParam("hora") String horaStr) {
        try {
            // Convertir String a LocalDate y LocalTime
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fecha = LocalDate.parse(fechaStr, dateFormatter);

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime hora = LocalTime.parse(horaStr, timeFormatter);

            // Verificar si existe cita
            boolean existeCita = citaService.existeCitaEnFechaHora(fecha, hora);

            return ResponseEntity.ok(existeCita);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/citas/por-fecha")
    public ResponseEntity<Map<String, List<Cita>>> obtenerCitasPorFecha() {
        try {
            // Obtener todas las citas
            List<Cita> citas = citaService.obtenerTodasLasCitas();

            // Agrupar las citas por fecha y ordenar por hora dentro de cada grupo
            Map<LocalDate, List<Cita>> citasPorFechaDesordenado = citas.stream()
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

            // Ordenar las fechas de forma descendente (más reciente primero)
            List<LocalDate> fechasOrdenadas = new ArrayList<>(citasPorFechaDesordenado.keySet());
            fechasOrdenadas.sort(Comparator.reverseOrder()); // Cambio de naturalOrder a reverseOrder

            // Convertir las claves LocalDate a String para facilitar la serialización
            Map<String, List<Cita>> citasPorFechaStr = new LinkedHashMap<>(); // LinkedHashMap mantiene el orden de inserción
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Añadir las fechas ordenadas al mapa final
            for (LocalDate fecha : fechasOrdenadas) {
                String fechaStr = fecha.format(formatter);
                citasPorFechaStr.put(fechaStr, citasPorFechaDesordenado.get(fecha));
            }

            return ResponseEntity.ok(citasPorFechaStr);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
