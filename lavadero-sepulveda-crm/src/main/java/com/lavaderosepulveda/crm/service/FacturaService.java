package com.lavaderosepulveda.crm.service;

import com.lavaderosepulveda.crm.model.dto.*;
import com.lavaderosepulveda.crm.model.entity.*;
import com.lavaderosepulveda.crm.model.enums.*;
import com.lavaderosepulveda.crm.repository.FacturaRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class FacturaService {
    
    private static FacturaService instance;
    
    private final FacturaRepository facturaRepository;
    private final PDFService pdfService;
    private final EmailService emailService;
    private final WhatsAppService whatsappService;
    
    private FacturaService() {
        this.facturaRepository = new FacturaRepository();
        this.pdfService = PDFService.getInstance();
        this.emailService = EmailService.getInstance();
        this.whatsappService = WhatsAppService.getInstance();
    }
    
    public static synchronized FacturaService getInstance() {
        if (instance == null) {
            instance = new FacturaService();
        }
        return instance;
    }
    
    public Factura crearFactura(Factura factura) {
        // Generar número de factura si no existe
        if (factura.getNumeroFactura() == null || factura.getNumeroFactura().isEmpty()) {
            factura.setNumeroFactura(generarNumeroFactura(factura.getSerieFactura()));
        }
        
        // Calcular totales
        factura.calcularTotales();
        
        // Guardar factura
        Factura facturaGuardada = facturaRepository.save(factura);
        log.info("Factura creada: {}", facturaGuardada.getNumeroFactura());
        
        return facturaGuardada;
    }
    
    public Factura crearFacturaDesdeita(Cita cita) {
        Factura factura = new Factura();
        factura.setCliente(cita.getCliente());
        factura.setCita(cita);
        factura.setFechaFactura(LocalDate.now());
        
        // Crear líneas de factura a partir de los servicios de la cita
        for (Servicio servicio : cita.getServicios()) {
            LineaFactura linea = new LineaFactura();
            linea.setServicio(servicio);
            linea.setConcepto(servicio.getNombre());
            linea.setCantidad(1);
            linea.setPrecioUnitario(servicio.getPrecio());
            linea.setIva(servicio.getIva());
            factura.agregarLinea(linea);
        }
        
        return crearFactura(factura);
    }
    
    public Factura actualizarFactura(Factura factura) {
        factura.calcularTotales();
        return facturaRepository.save(factura);
    }
    
    public void marcarComoPagada(Long facturaId, LocalDate fechaPago, String formaPago) {
        facturaRepository.findById(facturaId).ifPresent(factura -> {
            factura.setPagada(true);
            factura.setFechaPago(fechaPago);
            factura.setFormaPago(formaPago);
            facturaRepository.save(factura);
            log.info("Factura {} marcada como pagada", factura.getNumeroFactura());
        });
    }
    
    public boolean enviarFacturaPorEmail(Long facturaId) {
        return facturaRepository.findById(facturaId).map(factura -> {
            // Generar PDF si no existe
            File pdfFile = pdfService.generarFacturaPDF(factura);
            
            if (pdfFile == null) {
                log.error("No se pudo generar el PDF de la factura");
                return false;
            }
            
            factura.setRutaPdf(pdfFile.getAbsolutePath());
            
            // Enviar por email
            boolean enviado = emailService.enviarFactura(factura, pdfFile);
            
            if (enviado) {
                factura.setEnviadaEmail(true);
                factura.setFechaEnvioEmail(LocalDateTime.now());
                facturaRepository.save(factura);
            }
            
            return enviado;
        }).orElse(false);
    }
    
    public boolean enviarFacturaPorWhatsApp(Long facturaId) {
        return facturaRepository.findById(facturaId).map(factura -> {
            // Generar PDF si no existe
            File pdfFile;
            if (factura.getRutaPdf() != null) {
                pdfFile = new File(factura.getRutaPdf());
            } else {
                pdfFile = pdfService.generarFacturaPDF(factura);
                if (pdfFile != null) {
                    factura.setRutaPdf(pdfFile.getAbsolutePath());
                }
            }
            
            if (pdfFile == null || !pdfFile.exists()) {
                log.error("No se pudo encontrar el PDF de la factura");
                return false;
            }
            
            // Enviar por WhatsApp
            boolean enviado = whatsappService.enviarFactura(factura, pdfFile);
            
            if (enviado) {
                factura.setEnviadaWhatsapp(true);
                factura.setFechaEnvioWhatsapp(LocalDateTime.now());
                facturaRepository.save(factura);
            }
            
            return enviado;
        }).orElse(false);
    }
    
    public boolean enviarFacturaAutomatica(Long facturaId, boolean porEmail, boolean porWhatsApp) {
        boolean exitoEmail = true;
        boolean exitoWhatsApp = true;
        
        if (porEmail) {
            exitoEmail = enviarFacturaPorEmail(facturaId);
        }
        
        if (porWhatsApp) {
            exitoWhatsApp = enviarFacturaPorWhatsApp(facturaId);
        }
        
        return exitoEmail && exitoWhatsApp;
    }
    
    public File generarPDF(Long facturaId) {
        return facturaRepository.findById(facturaId)
            .map(pdfService::generarFacturaPDF)
            .orElse(null);
    }
    
    public List<Factura> buscarFacturas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio != null && fechaFin != null) {
            return facturaRepository.findByFechaRange(fechaInicio, fechaFin);
        }
        return facturaRepository.findAll();
    }
    
    public List<Factura> obtenerFacturasPendientes() {
        return facturaRepository.findPendientesPago();
    }
    
    public List<Factura> obtenerFacturasVencidas() {
        return facturaRepository.findVencidas();
    }
    
    public List<Factura> obtenerFacturasCliente(Long clienteId) {
        return facturaRepository.findByClienteId(clienteId);
    }
    
    public Double obtenerTotalFacturado(LocalDate fechaInicio, LocalDate fechaFin) {
        return facturaRepository.getTotalFacturadoEnPeriodo(fechaInicio, fechaFin);
    }
    
    public Double obtenerTotalPendienteCobro() {
        return facturaRepository.getTotalPendienteCobro();
    }
    
    private String generarNumeroFactura(String serie) {
        int anio = LocalDate.now().getYear();
        Long siguiente = facturaRepository.getNextNumeroFactura(serie);
        return String.format("%s-%d-%04d", serie, anio, siguiente);
    }
    
    public void eliminarFactura(Long facturaId) {
        facturaRepository.deleteById(facturaId);
        log.info("Factura eliminada: {}", facturaId);
    }
}
