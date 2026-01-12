package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.ClienteDTO;
import com.lavaderosepulveda.app.model.Cliente;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.enums.EstadoCita;
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
     * ACTUALIZADO: Permite clientes sin teléfono y busca por NIF para evitar duplicados
     */
    @Transactional
    public ClienteDTO crearCliente(ClienteDTO clienteDTO) {
        // Si tiene NIF, verificar si ya existe y actualizar en lugar de crear duplicado
        if (clienteDTO.getNif() != null && !clienteDTO.getNif().trim().isEmpty()) {
            Optional<Cliente> existentePorNif = clienteRepository.findByNif(clienteDTO.getNif().trim());
            if (existentePorNif.isPresent()) {
                log.info("Cliente con NIF {} ya existe (ID: {}), actualizando...",
                        clienteDTO.getNif(), existentePorNif.get().getId());
                return actualizarCliente(existentePorNif.get().getId(), clienteDTO);
            }
        }

        // Validar teléfono duplicado SOLO si se proporciona teléfono
        if (clienteDTO.getTelefono() != null && !clienteDTO.getTelefono().trim().isEmpty()) {
            if (clienteRepository.existsByTelefono(clienteDTO.getTelefono().trim())) {
                // En lugar de error, solo log warning (permite mismo teléfono para centralitas)
                log.warn("Ya existe un cliente con teléfono: {}. Creando de todos modos.",
                        clienteDTO.getTelefono());
            }
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(clienteDTO.getNombre());
        cliente.setApellidos(clienteDTO.getApellidos());
        cliente.setTelefono(clienteDTO.getTelefono());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setVehiculoHabitual(clienteDTO.getVehiculoHabitual());
        cliente.setActivo(clienteDTO.getActivo() != null ? clienteDTO.getActivo() : true);

        // Campos adicionales CRM
        cliente.setNif(clienteDTO.getNif());
        cliente.setDireccion(clienteDTO.getDireccion());
        cliente.setCodigoPostal(clienteDTO.getCodigoPostal());
        cliente.setCiudad(clienteDTO.getCiudad());
        cliente.setProvincia(clienteDTO.getProvincia());
        cliente.setMatricula(clienteDTO.getMatricula());
        cliente.setMarca(clienteDTO.getMarca());
        cliente.setModelo(clienteDTO.getModelo());
        cliente.setColor(clienteDTO.getColor());
        cliente.setNotas(clienteDTO.getNotas());

        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("Cliente creado: {} - {} - {}", clienteGuardado.getId(),
                clienteGuardado.getNif(), clienteGuardado.getNombre());

        return convertirADTO(clienteGuardado);
    }

    /**
     * Actualizar cliente existente
     */
    @Transactional
    public ClienteDTO actualizarCliente(Long id, ClienteDTO clienteDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        // Ya no validamos teléfono duplicado (permitimos centralitas compartidas)

        cliente.setNombre(clienteDTO.getNombre());
        cliente.setApellidos(clienteDTO.getApellidos());
        cliente.setTelefono(clienteDTO.getTelefono());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setVehiculoHabitual(clienteDTO.getVehiculoHabitual());
        if (clienteDTO.getActivo() != null) {
            cliente.setActivo(clienteDTO.getActivo());
        }

        // Campos adicionales CRM
        cliente.setNif(clienteDTO.getNif());
        cliente.setDireccion(clienteDTO.getDireccion());
        cliente.setCodigoPostal(clienteDTO.getCodigoPostal());
        cliente.setCiudad(clienteDTO.getCiudad());
        cliente.setProvincia(clienteDTO.getProvincia());
        cliente.setMatricula(clienteDTO.getMatricula());
        cliente.setMarca(clienteDTO.getMarca());
        cliente.setModelo(clienteDTO.getModelo());
        cliente.setColor(clienteDTO.getColor());
        cliente.setNotas(clienteDTO.getNotas());

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

        // Verificar si tiene citas asociadas (solo si tiene teléfono)
        if (cliente.getTelefono() != null && !cliente.getTelefono().isEmpty()) {
            List<Cita> citasCliente = citaRepository.findByTelefono(cliente.getTelefono());
            if (!citasCliente.isEmpty()) {
                log.warn("Cliente {} tiene {} citas asociadas. Marcando como inactivo en lugar de eliminar.",
                        id, citasCliente.size());
                cliente.setActivo(false);
                clienteRepository.save(cliente);
                return;
            }
        }

        clienteRepository.deleteById(id);
        log.info("Cliente eliminado: {} - {}", id, cliente.getNombre());
    }

    /**
     * Migrar clientes desde citas (usar una vez para migración inicial)
     */
    @Transactional
    public int migrarClientesDesdeCitas() {
        List<Cita> todasLasCitas = citaRepository.findAll();

        // Agrupar citas por teléfono
        Map<String, List<Cita>> citasPorTelefono = todasLasCitas.stream()
                .filter(c -> c.getTelefono() != null && !c.getTelefono().isEmpty())
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
                    .filter(c -> c.getModeloVehiculo() != null)
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
     * Obtener clientes activos
     */
    public List<ClienteDTO> obtenerClientesActivos() {
        List<Cliente> clientes = clienteRepository.findByActivoTrue();
        return clientes.stream()
                .map(this::convertirADTO)
                .sorted(Comparator.comparing(ClienteDTO::getNombre))
                .collect(Collectors.toList());
    }

    /**
     * Buscar clientes por nombre
     */
    public List<ClienteDTO> buscarPorNombre(String nombre) {
        List<Cliente> clientes = clienteRepository.findByNombreContainingIgnoreCase(nombre);
        return clientes.stream()
                .map(this::convertirADTO)
                .sorted(Comparator.comparing(ClienteDTO::getNombre))
                .collect(Collectors.toList());
    }

    /**
     * Contar total de clientes
     */
    public long contarClientes() {
        return clienteRepository.count();
    }

    /**
     * Contar clientes activos
     */
    public long contarClientesActivos() {
        return clienteRepository.countByActivoTrue();
    }

    /**
     * Obtener top clientes por facturación
     */
    public List<ClienteDTO> obtenerTopClientesPorFacturacion(int limit) {
        List<ClienteDTO> todosLosClientes = obtenerTodosLosClientes();

        return todosLosClientes.stream()
                .filter(c -> c.getTotalFacturado() != null && c.getTotalFacturado() > 0)
                .sorted(Comparator.comparing(ClienteDTO::getTotalFacturado).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Obtener clientes con más no presentaciones (clientes problemáticos)
     */
    public List<ClienteDTO> obtenerClientesConMasNoPresentaciones(int limit) {
        List<ClienteDTO> todosLosClientes = obtenerTodosLosClientes();

        return todosLosClientes.stream()
                .filter(c -> c.getCitasNoPresentadas() != null && c.getCitasNoPresentadas() > 0)
                .sorted(Comparator.comparing(ClienteDTO::getCitasNoPresentadas).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Obtener estadísticas generales de clientes
     */
    public Map<String, Object> obtenerEstadisticasClientes() {
        List<ClienteDTO> clientes = obtenerTodosLosClientes();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClientes", clientes.size());
        stats.put("clientesActivos", clientes.stream().filter(c -> Boolean.TRUE.equals(c.getActivo())).count());
        stats.put("clientesInactivos", clientes.stream().filter(c -> !Boolean.TRUE.equals(c.getActivo())).count());

        // Total facturado global
        double totalFacturadoGlobal = clientes.stream()
                .mapToDouble(c -> c.getTotalFacturado() != null ? c.getTotalFacturado() : 0)
                .sum();
        stats.put("totalFacturadoGlobal", totalFacturadoGlobal);

        // Promedio de facturación por cliente
        double promedioFacturacion = clientes.isEmpty() ? 0 : totalFacturadoGlobal / clientes.size();
        stats.put("promedioFacturacionPorCliente", promedioFacturacion);

        // Total de no presentaciones
        int totalNoPresentaciones = clientes.stream()
                .mapToInt(c -> c.getCitasNoPresentadas() != null ? c.getCitasNoPresentadas() : 0)
                .sum();
        stats.put("totalNoPresentaciones", totalNoPresentaciones);

        return stats;
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

        // Campos adicionales CRM
        dto.setNif(cliente.getNif());
        dto.setDireccion(cliente.getDireccion());
        dto.setCodigoPostal(cliente.getCodigoPostal());
        dto.setCiudad(cliente.getCiudad());
        dto.setProvincia(cliente.getProvincia());
        dto.setMatricula(cliente.getMatricula());
        dto.setMarca(cliente.getMarca());
        dto.setModelo(cliente.getModelo());
        dto.setColor(cliente.getColor());
        dto.setNotas(cliente.getNotas());

        // Obtener estadísticas de las citas del cliente (solo si tiene teléfono)
        if (cliente.getTelefono() != null && !cliente.getTelefono().isEmpty()) {
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
                    .filter(c -> c.getEstado() == EstadoCita.COMPLETADA && c.getTipoLavado() != null)
                    .mapToDouble(c -> c.getTipoLavado().getPrecio())
                    .sum();
            dto.setTotalFacturado(totalFacturado);
        } else {
            // Cliente sin teléfono (importado de contabilidad)
            dto.setTotalCitas(0);
            dto.setCitasCompletadas(0);
            dto.setCitasCanceladas(0);
            dto.setCitasNoPresentadas(0);
            dto.setTotalFacturado(0.0);
        }

        return dto;
    }
}