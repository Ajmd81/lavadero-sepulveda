package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.ClienteDTO;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.EstadoCita;
import com.lavaderosepulveda.app.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private CitaRepository citaRepository;

    /**
     * Obtiene todos los clientes únicos extrayendo de las citas
     */
    public List<ClienteDTO> obtenerTodosLosClientes() {
        List<Cita> todasLasCitas = citaRepository.findAll();
        
        // Agrupar citas por teléfono (identificador único del cliente)
        Map<String, List<Cita>> citasPorTelefono = todasLasCitas.stream()
            .collect(Collectors.groupingBy(Cita::getTelefono));
        
        // Convertir cada grupo en un ClienteDTO
        return citasPorTelefono.entrySet().stream()
            .map(entry -> construirClienteDTO(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(ClienteDTO::getNombre))
            .collect(Collectors.toList());
    }

    /**
     * Obtener cliente por ID (hash del teléfono)
     */
    public Optional<ClienteDTO> obtenerClientePorId(Long id) {
        // El ID es el hashCode del teléfono
        List<Cita> todasLasCitas = citaRepository.findAll();
        
        return todasLasCitas.stream()
            .filter(cita -> generarId(cita.getTelefono()).equals(id))
            .findFirst()
            .map(cita -> {
                List<Cita> citasCliente = citaRepository.findByTelefono(cita.getTelefono());
                return construirClienteDTO(cita.getTelefono(), citasCliente);
            });
    }

    /**
     * Buscar cliente por teléfono
     */
    public Optional<ClienteDTO> obtenerClientePorTelefono(String telefono) {
        List<Cita> citasCliente = citaRepository.findByTelefono(telefono);
        
        if (citasCliente.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(construirClienteDTO(telefono, citasCliente));
    }

    /**
     * Construye un ClienteDTO a partir de las citas de un cliente
     */
    private ClienteDTO construirClienteDTO(String telefono, List<Cita> citas) {
        if (citas.isEmpty()) {
            return null;
        }
        
        // Tomar la cita más reciente para datos básicos
        Cita citaMasReciente = citas.stream()
            .max(Comparator.comparing(Cita::getFecha))
            .orElse(citas.get(0));
        
        ClienteDTO cliente = new ClienteDTO();
        
        // Datos básicos
        cliente.setId(generarId(telefono));
        cliente.setTelefono(telefono);
        cliente.setEmail(citaMasReciente.getEmail());
        
        // Separar nombre y apellidos
        String[] nombreCompleto = citaMasReciente.getNombre().split(" ", 2);
        cliente.setNombre(nombreCompleto[0]);
        cliente.setApellidos(nombreCompleto.length > 1 ? nombreCompleto[1] : "");
        
        // Calcular si está activo (tiene citas en los últimos 6 meses)
        LocalDate hace6Meses = LocalDate.now().minusMonths(6);
        boolean tieneCitasRecientes = citas.stream()
            .anyMatch(cita -> cita.getFecha().isAfter(hace6Meses));
        cliente.setActivo(tieneCitasRecientes);
        
        // Estadísticas
        cliente.setTotalCitas(citas.size());
        cliente.setCitasCompletadas((int) citas.stream()
            .filter(c -> c.getEstado() == EstadoCita.COMPLETADA)
            .count());
        cliente.setCitasCanceladas((int) citas.stream()
            .filter(c -> c.getEstado() == EstadoCita.CANCELADA)
            .count());
        cliente.setCitasNoPresentadas((int) citas.stream()
            .filter(c -> c.getEstado() == EstadoCita.NO_PRESENTADO)
            .count());
        
        // Total facturado (solo citas completadas)
        double totalFacturado = citas.stream()
            .filter(c -> c.getEstado() == EstadoCita.COMPLETADA)
            .mapToDouble(c -> c.getTipoLavado().getPrecio())
            .sum();
        cliente.setTotalFacturado(totalFacturado);
        
        // Vehículo habitual (el más usado)
        Map<String, Long> vehiculosFrecuencia = citas.stream()
            .collect(Collectors.groupingBy(Cita::getModeloVehiculo, Collectors.counting()));
        
        String vehiculoHabitual = vehiculosFrecuencia.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(citaMasReciente.getModeloVehiculo());
        cliente.setVehiculoHabitual(vehiculoHabitual);
        
        return cliente;
    }

    /**
     * Genera un ID único a partir del teléfono
     */
    private Long generarId(String telefono) {
        return (long) Math.abs(telefono.hashCode());
    }
}