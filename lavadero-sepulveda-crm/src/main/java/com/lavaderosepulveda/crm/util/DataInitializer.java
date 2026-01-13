package com.lavaderosepulveda.crm.util;

import com.lavaderosepulveda.crm.model.dto.*;
import com.lavaderosepulveda.crm.model.entity.*;
import com.lavaderosepulveda.crm.model.enums.*;
import com.lavaderosepulveda.crm.repository.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para inicializar datos de prueba en la base de datos.
 * Útil para desarrollo y demos.
 */
@Slf4j
public class DataInitializer {
    
    private final ServicioRepository servicioRepository;
    private final ClienteRepository clienteRepository;
    private final CitaRepository citaRepository;
    
    public DataInitializer() {
        this.servicioRepository = new ServicioRepository();
        this.clienteRepository = new ClienteRepository();
        this.citaRepository = new CitaRepository();
    }
    
    public void inicializarDatosPrueba() {
        log.info("Iniciando carga de datos de prueba...");
        
        try {
            // Verificar si ya existen datos
            if (servicioRepository.count() > 0) {
                log.info("Ya existen datos en la base de datos. Saltando inicialización.");
                return;
            }
            
            // Crear servicios
            List<Servicio> servicios = crearServicios();
            log.info("Creados {} servicios", servicios.size());
            
            // Crear clientes
            List<Cliente> clientes = crearClientes();
            log.info("Creados {} clientes", clientes.size());
            
            // Crear citas
            List<Cita> citas = crearCitas(clientes, servicios);
            log.info("Creadas {} citas", citas.size());
            
            log.info("Datos de prueba inicializados correctamente");
            
        } catch (Exception e) {
            log.error("Error al inicializar datos de prueba", e);
        }
    }
    
    private List<Servicio> crearServicios() {
        List<Servicio> servicios = new ArrayList<>();
        
        // Lavados básicos
        servicios.add(crearServicio("Lavado Exterior Básico", 
            "Lavado exterior con agua y jabón", 15.0, 20, "LAVADO_BASICO"));
        servicios.add(crearServicio("Lavado Exterior Completo", 
            "Lavado exterior completo con secado", 25.0, 30, "LAVADO_COMPLETO"));
        
        // Lavados completos
        servicios.add(crearServicio("Lavado Interior", 
            "Aspirado y limpieza interior completa", 30.0, 40, "LAVADO_COMPLETO"));
        servicios.add(crearServicio("Lavado Completo", 
            "Exterior + Interior completo", 50.0, 60, "LAVADO_COMPLETO"));
        
        // Servicios premium
        servicios.add(crearServicio("Lavado Premium", 
            "Lavado completo + encerado + pulido", 80.0, 90, "LAVADO_PREMIUM"));
        servicios.add(crearServicio("Detailing Completo", 
            "Lavado detallado profesional completo", 150.0, 180, "LAVADO_PREMIUM"));
        
        // Servicios adicionales
        servicios.add(crearServicio("Encerado", 
            "Aplicación de cera protectora", 35.0, 45, "ENCERADO"));
        servicios.add(crearServicio("Pulido", 
            "Pulido de carrocería", 60.0, 90, "PULIDO"));
        servicios.add(crearServicio("Limpieza de Tapicería", 
            "Limpieza profunda de asientos", 40.0, 60, "LIMPIEZA_INTERIOR"));
        servicios.add(crearServicio("Limpieza de Motor", 
            "Lavado y limpieza del motor", 45.0, 45, "MOTOR"));
        
        for (Servicio servicio : servicios) {
            servicioRepository.save(servicio);
        }
        
        return servicios;
    }
    
    private Servicio crearServicio(String nombre, String descripcion, Double precio, 
                                   Integer duracion, String categoria) {
        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setPrecio(precio);
        servicio.setDuracionEstimada(duracion);
        servicio.setCategoria(categoria);
        servicio.setIva(21.0);
        servicio.setActivo(true);
        return servicio;
    }
    
    private List<Cliente> crearClientes() {
        List<Cliente> clientes = new ArrayList<>();
        
        clientes.add(crearCliente("Juan", "García López", "666111222", 
            "juan.garcia@email.com", "1234ABC", "BMW", "Serie 3", "Negro"));
        clientes.add(crearCliente("María", "Rodríguez Sánchez", "666222333", 
            "maria.rodriguez@email.com", "5678DEF", "Audi", "A4", "Blanco"));
        clientes.add(crearCliente("Carlos", "Martínez Pérez", "666333444", 
            "carlos.martinez@email.com", "9012GHI", "Mercedes", "Clase C", "Gris"));
        clientes.add(crearCliente("Ana", "López Fernández", "666444555", 
            "ana.lopez@email.com", "3456JKL", "Volkswagen", "Golf", "Azul"));
        clientes.add(crearCliente("Pedro", "González Ruiz", "666555666", 
            "pedro.gonzalez@email.com", "7890MNO", "Seat", "León", "Rojo"));
        clientes.add(crearCliente("Laura", "Sánchez Moreno", "666666777", 
            "laura.sanchez@email.com", "1357PQR", "Ford", "Focus", "Negro"));
        clientes.add(crearCliente("Miguel", "Fernández Díaz", "666777888", 
            "miguel.fernandez@email.com", "2468STU", "Renault", "Megane", "Blanco"));
        clientes.add(crearCliente("Carmen", "Díaz Jiménez", "666888999", 
            "carmen.diaz@email.com", "3579VWX", "Peugeot", "308", "Plata"));
        clientes.add(crearCliente("Antonio", "Jiménez Muñoz", "666999000", 
            "antonio.jimenez@email.com", "4680YZA", "Opel", "Astra", "Azul"));
        clientes.add(crearCliente("Isabel", "Muñoz Álvarez", "666000111", 
            "isabel.munoz@email.com", "5791BCD", "Nissan", "Qashqai", "Gris"));
        
        for (Cliente cliente : clientes) {
            clienteRepository.save(cliente);
        }
        
        return clientes;
    }
    
    private Cliente crearCliente(String nombre, String apellidos, String telefono,
                                String email, String matricula, String marca, 
                                String modelo, String color) {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setApellidos(apellidos);
        cliente.setTelefono(telefono);
        cliente.setEmail(email);
        cliente.setMatricula(matricula);
        cliente.setMarca(marca);
        cliente.setModelo(modelo);
        cliente.setColor(color);
        cliente.setActivo(true);
        cliente.setDireccion("Calle Ejemplo, " + (int)(Math.random() * 100));
        cliente.setCodigoPostal("280" + (10 + (int)(Math.random() * 40)));
        cliente.setCiudad("Madrid");
        cliente.setProvincia("Madrid");
        return cliente;
    }
    
    private List<Cita> crearCitas(List<Cliente> clientes, List<Servicio> servicios) {
        List<Cita> citas = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();
        
        // Crear citas pasadas
        for (int i = 0; i < 20; i++) {
            Cliente cliente = clientes.get((int)(Math.random() * clientes.size()));
            LocalDateTime fecha = ahora.minusDays((int)(Math.random() * 30));
            
            Cita cita = new Cita();
            cita.setCliente(cliente);
            cita.setFechaHora(fecha);
            cita.setEstado(EstadoCita.COMPLETADA);
            cita.setMatricula(cliente.getMatricula());
            cita.setMarcaModelo(cliente.getMarca() + " " + cliente.getModelo());
            
            // Agregar 1-3 servicios aleatorios
            int numServicios = 1 + (int)(Math.random() * 3);
            List<Servicio> serviciosCita = new ArrayList<>();
            for (int j = 0; j < numServicios; j++) {
                Servicio servicio = servicios.get((int)(Math.random() * servicios.size()));
                if (!serviciosCita.contains(servicio)) {
                    serviciosCita.add(servicio);
                }
            }
            cita.setServicios(serviciosCita);
            
            citaRepository.save(cita);
            citas.add(cita);
            
            // Actualizar estadísticas del cliente
            cliente.setTotalCitas(cliente.getTotalCitas() + 1);
            cliente.setCitasCompletadas(cliente.getCitasCompletadas() + 1);
            cliente.setTotalFacturado(cliente.getTotalFacturado() + cita.getImporteTotal());
            if (cliente.getFechaPrimeraCita() == null) {
                cliente.setFechaPrimeraCita(fecha);
            }
            cliente.setFechaUltimaCita(fecha);
            clienteRepository.save(cliente);
        }
        
        // Crear citas futuras
        for (int i = 0; i < 10; i++) {
            Cliente cliente = clientes.get((int)(Math.random() * clientes.size()));
            LocalDateTime fecha = ahora.plusDays((int)(Math.random() * 14) + 1);
            
            Cita cita = new Cita();
            cita.setCliente(cliente);
            cita.setFechaHora(fecha);
            cita.setEstado(EstadoCita.PENDIENTE);
            cita.setMatricula(cliente.getMatricula());
            cita.setMarcaModelo(cliente.getMarca() + " " + cliente.getModelo());
            
            // Agregar servicios
            int numServicios = 1 + (int)(Math.random() * 2);
            List<Servicio> serviciosCita = new ArrayList<>();
            for (int j = 0; j < numServicios; j++) {
                Servicio servicio = servicios.get((int)(Math.random() * servicios.size()));
                if (!serviciosCita.contains(servicio)) {
                    serviciosCita.add(servicio);
                }
            }
            cita.setServicios(serviciosCita);
            
            citaRepository.save(cita);
            citas.add(cita);
            
            cliente.setTotalCitas(cliente.getTotalCitas() + 1);
            clienteRepository.save(cliente);
        }
        
        // Crear algunas citas con no presentaciones
        for (int i = 0; i < 3; i++) {
            Cliente cliente = clientes.get((int)(Math.random() * clientes.size()));
            LocalDateTime fecha = ahora.minusDays((int)(Math.random() * 15) + 1);
            
            Cita cita = new Cita();
            cita.setCliente(cliente);
            cita.setFechaHora(fecha);
            cita.setEstado(EstadoCita.NO_PRESENTADO);
            cita.setMatricula(cliente.getMatricula());
            cita.setMarcaModelo(cliente.getMarca() + " " + cliente.getModelo());
            
            List<Servicio> serviciosCita = new ArrayList<>();
            serviciosCita.add(servicios.get(0));
            cita.setServicios(serviciosCita);
            
            citaRepository.save(cita);
            citas.add(cita);
            
            cliente.setTotalCitas(cliente.getTotalCitas() + 1);
            cliente.setCitasNoPresentadas(cliente.getCitasNoPresentadas() + 1);
            clienteRepository.save(cliente);
        }
        
        return citas;
    }
}
