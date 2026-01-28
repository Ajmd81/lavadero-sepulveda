package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.model.*;
import com.lavaderosepulveda.app.model.enums.*;
import com.lavaderosepulveda.app.repository.CitaRepository;
import com.lavaderosepulveda.app.repository.ClienteRepository;
import com.lavaderosepulveda.app.repository.FacturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class FacturaService {

    private static final Logger log = LoggerFactory.getLogger(FacturaService.class);

    // Datos fiscales del emisor
    private static final String EMISOR_NOMBRE = "ANTONIO JESUS MARTINEZ DÍAZ";
    private static final String EMISOR_NIF = "44372838L";
    private static final String EMISOR_DIRECCION = "C/ Ingeniero Ruiz de Azua s/n Local 8, 14006 Córdoba";
    private static final BigDecimal IVA_PORCENTAJE = new BigDecimal("21.00");

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // ========================================
    // CRUD BÁSICO
    // ========================================

    /**
     * Obtener todas las facturas
     */
    public List<Factura> obtenerTodas() {
        return facturaRepository.findAll();
    }

    /**
     * Obtener factura por ID
     */
    public Optional<Factura> obtenerPorId(Long id) {
        return facturaRepository.findById(id);
    }

    /**
     * Obtener factura por número
     */
    public Optional<Factura> obtenerPorNumero(String numero) {
        return facturaRepository.findByNumero(numero);
    }

    /**
     * Eliminar factura (solo si está pendiente)
     */
    @Transactional
    public void eliminar(Long id) {
        // Cargar la factura CON sus líneas (esto activa el cascade)
        Factura factura = facturaRepository.findByIdWithLineas(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        // Desmarcar las citas asociadas como facturadas
        for (LineaFactura linea : factura.getLineas()) {
            if (linea.getCitaId() != null) {
                citaRepository.findById(linea.getCitaId()).ifPresent(cita -> {
                    cita.setFacturada(false);
                    cita.setFacturaId(null);
                    citaRepository.save(cita);
                });
            }
        }

        // IMPORTANTE: usar delete(factura) en lugar de deleteById(id)
        facturaRepository.delete(factura);
        log.info("Factura {} eliminada", factura.getNumero());
    }

    // ========================================
    // CREACIÓN DE FACTURAS
    // ========================================

    /**
     * Crear factura simplificada desde una cita completada
     */
    @Transactional
    public Factura crearFacturaSimplificadaDesdeCita(Long citaId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (cita.getFacturada() != null && cita.getFacturada()) {
            throw new RuntimeException("La cita ya está facturada");
        }

        Factura factura = new Factura();
        factura.setTipo(TipoFactura.SIMPLIFICADA);
        factura.setFecha(LocalDate.now());
        factura.setAnio(LocalDate.now().getYear());

        // Generar número de factura
        generarNumeroFactura(factura);

        // Datos del cliente (sin NIF ni dirección para simplificada)
        factura.setClienteNombre(cita.getNombre());
        factura.setClienteTelefono(cita.getTelefono());
        factura.setClienteEmail(cita.getEmail());

        // Añadir línea con el servicio
        LineaFactura linea = crearLineaDesdeCita(cita);
        factura.addLinea(linea);

        // Guardar factura
        factura = facturaRepository.save(factura);

        // Marcar cita como facturada
        cita.setFacturada(true);
        cita.setFacturaId(factura.getId());
        citaRepository.save(cita);

        log.info("Factura simplificada {} creada desde cita {}", factura.getNumero(), citaId);
        return factura;
    }

    /**
     * Crear factura completa para un cliente
     */
    @Transactional
    public Factura crearFacturaCompleta(Long clienteId, List<Long> citaIds, String clienteNif,
            String clienteDireccion) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Factura factura = new Factura();
        factura.setTipo(TipoFactura.COMPLETA);
        factura.setFecha(LocalDate.now());
        factura.setAnio(LocalDate.now().getYear());
        factura.setCliente(cliente);

        // Generar número de factura
        generarNumeroFactura(factura);

        // Datos completos del cliente
        factura.setClienteNombre(
                cliente.getNombre() + " " + (cliente.getApellidos() != null ? cliente.getApellidos() : ""));
        factura.setClienteNif(clienteNif);
        factura.setClienteDireccion(clienteDireccion);
        factura.setClienteTelefono(cliente.getTelefono());
        factura.setClienteEmail(cliente.getEmail());

        // Añadir líneas desde las citas
        for (Long citaId : citaIds) {
            Cita cita = citaRepository.findById(citaId)
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));

            if (cita.getFacturada() != null && cita.getFacturada()) {
                throw new RuntimeException("La cita " + citaId + " ya está facturada");
            }

            LineaFactura linea = crearLineaDesdeCita(cita);
            factura.addLinea(linea);
        }

        // Guardar factura
        factura = facturaRepository.save(factura);

        // Marcar citas como facturadas
        for (Long citaId : citaIds) {
            Cita cita = citaRepository.findById(citaId).get();
            cita.setFacturada(true);
            cita.setFacturaId(factura.getId());
            citaRepository.save(cita);
        }

        log.info("Factura completa {} creada para cliente {}", factura.getNumero(), clienteId);
        return factura;
    }

    /**
     * Crear factura manual (sin citas asociadas)
     */
    @Transactional
    public Factura crearFacturaManual(TipoFactura tipo, String clienteNombre, String clienteNif,
            String clienteDireccion, String clienteTelefono, String clienteEmail,
            List<LineaFactura> lineas) {
        Factura factura = new Factura();
        factura.setTipo(tipo);
        factura.setFecha(LocalDate.now());
        factura.setAnio(LocalDate.now().getYear());

        // Generar número de factura
        generarNumeroFactura(factura);

        // Datos del cliente
        factura.setClienteNombre(clienteNombre);
        factura.setClienteNif(clienteNif);
        factura.setClienteDireccion(clienteDireccion);
        factura.setClienteTelefono(clienteTelefono);
        factura.setClienteEmail(clienteEmail);

        // Añadir líneas
        for (LineaFactura linea : lineas) {
            factura.addLinea(linea);
        }

        factura = facturaRepository.save(factura);
        log.info("Factura manual {} creada", factura.getNumero());
        return factura;
    }

    // ========================================
    // GESTIÓN DE PAGOS
    // ========================================

    /**
     * Marcar factura como pagada
     */
    @Transactional
    public Factura marcarComoPagada(Long id, MetodoPago metodoPago) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        factura.marcarComoPagada(metodoPago);
        factura = facturaRepository.save(factura);

        log.info("Factura {} marcada como pagada con {}", factura.getNumero(), metodoPago);
        return factura;
    }

    // ========================================
    // CONSULTAS
    // ========================================

    /**
     * Obtener facturas por estado
     */
    public List<Factura> obtenerPorEstado(EstadoFactura estado) {
        return facturaRepository.findByEstadoOrderByFechaDesc(estado);
    }

    /**
     * Obtener facturas pendientes
     */
    public List<Factura> obtenerPendientes() {
        return facturaRepository.findFacturasPendientes();
    }

    /**
     * Obtener facturas por cliente
     */
    public List<Factura> obtenerPorCliente(Long clienteId) {
        return facturaRepository.findByClienteIdOrderByFechaDesc(clienteId);
    }

    /**
     * Obtener facturas por rango de fechas
     */
    public List<Factura> obtenerPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return facturaRepository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin);
    }

    /**
     * Buscar facturas por texto
     */
    public List<Factura> buscar(String texto) {
        return facturaRepository.buscarPorTexto(texto);
    }

    /**
     * Obtener facturas de hoy
     */
    public List<Factura> obtenerDeHoy() {
        return facturaRepository.findByFechaOrderByCreatedAtDesc(LocalDate.now());
    }

    // ========================================
    // ESTADÍSTICAS
    // ========================================

    /**
     * Obtener resumen de facturación
     */
    public Map<String, Object> obtenerResumen() {
        Map<String, Object> resumen = new HashMap<>();

        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        LocalDate inicioAnio = hoy.withDayOfYear(1);
        LocalDate finAnio = hoy.withDayOfYear(hoy.lengthOfYear());

        // Total facturado mes actual
        BigDecimal totalMes = facturaRepository.sumTotalByFechaBetween(inicioMes, finMes);
        resumen.put("totalMes", totalMes);

        // Total facturado año actual
        BigDecimal totalAnio = facturaRepository.sumTotalByFechaBetween(inicioAnio, finAnio);
        resumen.put("totalAnio", totalAnio);

        // Total pendiente de cobro
        BigDecimal totalPendiente = facturaRepository.sumTotalPendiente();
        resumen.put("totalPendiente", totalPendiente);

        // Cobrado este mes
        BigDecimal cobradoMes = facturaRepository.sumTotalByEstadoAndFechaBetween(
                EstadoFactura.PAGADA, inicioMes, finMes);
        resumen.put("cobradoMes", cobradoMes);

        // Número de facturas pendientes
        long facturasPendientes = facturaRepository.countByEstado(EstadoFactura.PENDIENTE);
        resumen.put("facturasPendientes", facturasPendientes);

        // Número de facturas este mes
        long facturasMes = facturaRepository.countByMes(hoy.getYear(), hoy.getMonthValue());
        resumen.put("facturasMes", facturasMes);

        return resumen;
    }

    // ========================================
    // DATOS DEL EMISOR
    // ========================================

    /**
     * Obtener datos fiscales del emisor
     */
    public Map<String, String> obtenerDatosEmisor() {
        Map<String, String> datos = new HashMap<>();
        datos.put("nombre", EMISOR_NOMBRE);
        datos.put("nif", EMISOR_NIF);
        datos.put("direccion", EMISOR_DIRECCION);
        return datos;
    }

    // ========================================
    // MÉTODOS PRIVADOS
    // ========================================

    /**
     * Generar número de factura: YYYY/NNN
     */
    private void generarNumeroFactura(Factura factura) {
        Integer anio = factura.getAnio();
        Integer siguienteNumero = facturaRepository.findMaxNumeroSecuencialByAnio(anio)
                .map(max -> max + 1)
                .orElse(1);

        factura.setNumeroSecuencial(siguienteNumero);
        factura.setNumero(String.format("%d/%03d", anio, siguienteNumero));
    }

    /**
     * Crear línea de factura desde una cita
     */
    private LineaFactura crearLineaDesdeCita(Cita cita) {
        LineaFactura linea = new LineaFactura();
        linea.setCitaId(cita.getId());

        // Descripción del servicio
        String concepto = formatearConceptoServicio(cita.getTipoLavado());
        if (cita.getModeloVehiculo() != null && !cita.getModeloVehiculo().isEmpty()) {
            concepto += " - " + cita.getModeloVehiculo();
        }
        linea.setConcepto(concepto);

        // Precio (el precio del TipoLavado ya incluye IVA, hay que extraer la base)
        BigDecimal precioConIva = new BigDecimal(cita.getTipoLavado().getPrecio());
        BigDecimal precioSinIva = precioConIva.divide(
                BigDecimal.ONE.add(IVA_PORCENTAJE.divide(new BigDecimal("100"))),
                2, RoundingMode.HALF_UP);
        linea.setPrecioUnitario(precioSinIva);
        linea.setCantidad(1);
        linea.calcularSubtotal();

        return linea;
    }

    /**
     * Formatear nombre del servicio para la factura
     */
    private String formatearConceptoServicio(TipoLavado tipoLavado) {
        if (tipoLavado == null)
            return "Servicio de lavado";
        return tipoLavado.getDescripcion();
    }
}