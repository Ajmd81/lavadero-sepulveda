package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.ClienteDTO;
import com.lavaderosepulveda.app.model.Cliente;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.EstadoCita;
import com.lavaderosepulveda.app.repository.ClienteRepository;
import com.lavaderosepulveda.app.repository.CitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CitaRepository citaRepository;

    /**
     * Obtener todos los clientes (desde tabla clientes + estadísticas de citas)
     */
    public List<ClienteDTO> obtenerTodosLosClientes() {
        List<Cliente> clientes = clienteRepository.findAll();

        return clientes.stream()
                .map(this::convertirADTO)
                .sorted(Comparator.comparing(ClienteDTO::getNombre))
                .collect(Collectors.toList());
    }

    /**
     * Obtener cliente por ID
     */
    public Optional<ClienteDTO> obtenerClientePorId(Long id) {
        return clienteRepository.findById(id)
                .map(this::convertirADTO);
    }

    /**
     * Obtener cliente por teléfono
     */
    public Optional<ClienteDTO> obtenerClientePorTelefono(String telefono) {
        return clienteRepository.findByTelefono(telefono)
                .map(this::convertirADTO);
    }

    /**
     * Crear nuevo cliente
     */
    @Transactional
    public ClienteDTO crearCliente(ClienteDTO clienteDTO) {
        // Validar que no exista un cliente con ese teléfono
        if (clienteRepository.existsByTelefono(clienteDTO.getTelefono())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese teléfono: " + clienteDTO.getTelefono());
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(clienteDTO.getNombre());
        cliente.setApellidos(clienteDTO.getApellidos());
        cliente.setTelefono(clienteDTO.getTelefono());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setVehiculoHabitual(clienteDTO.getVehiculoHabitual());
        cliente.setActivo(clienteDTO.getActivo() != null ? clienteDTO.getActivo() : true);

        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("Cliente creado: {} - {}", clienteGuardado.getId(), clienteGuardado.getNombre());

        return convertirADTO(clienteGuardado);
    }

    /**
     * Actualizar cliente existente
     */
    @Transactional
    public ClienteDTO actualizarCliente(Long id, ClienteDTO clienteDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        // Validar que el teléfono no esté en uso por otro cliente
        if (!cliente.getTelefono().equals(clienteDTO.getTelefono())) {
            if (clienteRepository.existsByTelefono(clienteDTO.getTelefono())) {
                throw new IllegalArgumentException("Ya existe otro cliente con ese teléfono: " + clienteDTO.getTelefono());
            }
        }

        cliente.setNombre(clienteDTO.getNombre());
        cliente.setApellidos(clienteDTO.getApellidos());
        cliente.setTelefono(clienteDTO.getTelefono());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setVehiculoHabitual(clienteDTO.getVehiculoHabitual());
        cliente.setActivo(clienteDTO.getActivo());

        Cliente clienteActualizado = clienteRepository.save(cliente);
        log.info("Cliente actualizado: {} - {}", clienteActualizado.getId(), clienteActualizado.getNombre());

        return convertirADTO(clienteActualizado);
    }

    /**
     * Eliminar cliente
     */
    @Transactional
    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        // Verificar si tiene citas asociadas
        List<Cita> citasCliente = citaRepository.findByTelefono(cliente.getTelefono());
        if (!citasCliente.isEmpty()) {
            log.warn("Cliente {} tiene {} citas asociadas. Marcando como inactivo en lugar de eliminar.",
                    id, citasCliente.size());
            cliente.setActivo(false);
            clienteRepository.save(cliente);
        } else {
            clienteRepository.deleteById(id);
            log.info("Cliente eliminado: {} - {}", id, cliente.getNombre());
        }
    }

    /**
     * Migrar clientes desde citas (usar una vez para migración inicial)
     */
    @Transactional
    public int migrarClientesDesdeCitas() {
        List<Cita> todasLasCitas = citaRepository.findAll();

        // Agrupar citas por teléfono
        Map<String, List<Cita>> citasPorTelefono = todasLasCitas.stream()
                .collect(Collectors.groupingBy(Cita::getTelefono));

        int clientesMigrados = 0;

        for (Map.Entry<String, List<Cita>> entry : citasPorTelefono.entrySet()) {
            String telefono = entry.getKey();

            // Verificar si ya existe el cliente
            if (clienteRepository.existsByTelefono(telefono)) {
                continue;
            }

            List<Cita> citas = entry.getValue();
            Cita citaMasReciente = citas.stream()
                    .max(Comparator.comparing(Cita::getFecha))
                    .orElse(citas.get(0));

            Cliente cliente = new Cliente();
            cliente.setTelefono(telefono);
            cliente.setEmail(citaMasReciente.getEmail());

            // Separar nombre y apellidos
            String[] nombreCompleto = citaMasReciente.getNombre().split(" ", 2);
            cliente.setNombre(nombreCompleto[0]);
            cliente.setApellidos(nombreCompleto.length > 1 ? nombreCompleto[1] : "");

            // Vehículo habitual
            Map<String, Long> vehiculosFrecuencia = citas.stream()
                    .collect(Collectors.groupingBy(Cita::getModeloVehiculo, Collectors.counting()));

            String vehiculoHabitual = vehiculosFrecuencia.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(citaMasReciente.getModeloVehiculo());
            cliente.setVehiculoHabitual(vehiculoHabitual);

            // Determinar si está activo
            LocalDate hace6Meses = LocalDate.now().minusMonths(6);
            boolean tieneCitasRecientes = citas.stream()
                    .anyMatch(cita -> cita.getFecha().isAfter(hace6Meses));
            cliente.setActivo(tieneCitasRecientes);

            clienteRepository.save(cliente);
            clientesMigrados++;
        }

        log.info("Migración completada: {} clientes migrados", clientesMigrados);
        return clientesMigrados;
    }

    /**
     * Convertir entidad Cliente a ClienteDTO (con estadísticas de citas)
     */
    private ClienteDTO convertirADTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();

        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setApellidos(cliente.getApellidos());
        dto.setTelefono(cliente.getTelefono());
        dto.setEmail(cliente.getEmail());
        dto.setVehiculoHabitual(cliente.getVehiculoHabitual());
        dto.setActivo(cliente.getActivo());

        // Obtener estadísticas de las citas del cliente
        List<Cita> citas = citaRepository.findByTelefono(cliente.getTelefono());

        dto.setTotalCitas(citas.size());
        dto.setCitasCompletadas((int) citas.stream()
                .filter(c -> c.getEstado() == EstadoCita.COMPLETADA)
                .count());
        dto.setCitasCanceladas((int) citas.stream()
                .filter(c -> c.getEstado() == EstadoCita.CANCELADA)
                .count());
        dto.setCitasNoPresentadas((int) citas.stream()
                .filter(c -> c.getEstado() == EstadoCita.NO_PRESENTADO)
                .count());

        // Total facturado (solo citas completadas)
        double totalFacturado = citas.stream()
                .filter(c -> c.getEstado() == EstadoCita.COMPLETADA)
                .mapToDouble(c -> c.getTipoLavado().getPrecio())
                .sum();
        dto.setTotalFacturado(totalFacturado);

        return dto;
    }
}