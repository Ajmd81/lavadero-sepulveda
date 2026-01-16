package com.lavaderosepulveda.crm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para el Modelo 347 - Declaración anual de operaciones con terceras personas
 * Se declaran operaciones que superen los 3.005,06 € anuales (IVA incluido)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Modelo347DTO {

    // ========================================
    // IDENTIFICACIÓN DEL DECLARANTE
    // ========================================
    private String ejercicio;           // Año (ej: "2024")
    private String nif;
    private String nombreRazonSocial;
    private String domicilioFiscal;
    private String telefono;
    
    // ========================================
    // RESUMEN DE LA DECLARACIÓN
    // ========================================
    
    // Número total de personas o entidades declaradas
    private int numDeclarados;
    
    // Importe total de las operaciones
    private BigDecimal importeTotal;
    
    // Número de registros de clientes
    private int numClientes;
    
    // Importe total de ventas (clientes)
    private BigDecimal importeTotalVentas;
    
    // Número de registros de proveedores
    private int numProveedores;
    
    // Importe total de compras (proveedores)
    private BigDecimal importeTotalCompras;
    
    // ========================================
    // LISTADO DE DECLARADOS
    // ========================================
    
    @Builder.Default
    private List<Declarado347> declarados = new ArrayList<>();
    
    // ========================================
    // DATOS ADICIONALES
    // ========================================
    
    // Umbral mínimo para declarar (3.005,06 €)
    public static final BigDecimal UMBRAL_MINIMO = new BigDecimal("3005.06");
    
    // Tipo de declaración: "C" Complementaria, "S" Sustitutiva, "" Normal
    private String tipoDeclaracion;
    
    // Fecha de presentación
    private String fechaPresentacion;
    
    /**
     * Calcula los totales de la declaración
     */
    public void calcularTotales() {
        this.numDeclarados = declarados.size();
        this.importeTotal = BigDecimal.ZERO;
        this.numClientes = 0;
        this.importeTotalVentas = BigDecimal.ZERO;
        this.numProveedores = 0;
        this.importeTotalCompras = BigDecimal.ZERO;
        
        for (Declarado347 declarado : declarados) {
            if (declarado.getImporteAnual() != null) {
                importeTotal = importeTotal.add(declarado.getImporteAnual());
                
                if ("V".equals(declarado.getClaveOperacion())) {
                    // Venta (cliente)
                    numClientes++;
                    importeTotalVentas = importeTotalVentas.add(declarado.getImporteAnual());
                } else if ("C".equals(declarado.getClaveOperacion())) {
                    // Compra (proveedor)
                    numProveedores++;
                    importeTotalCompras = importeTotalCompras.add(declarado.getImporteAnual());
                }
            }
        }
    }
    
    /**
     * Añade un declarado si supera el umbral
     */
    public boolean addDeclaradoSiSupera(Declarado347 declarado) {
        if (declarado.getImporteAnual() != null && 
            declarado.getImporteAnual().abs().compareTo(UMBRAL_MINIMO) >= 0) {
            declarados.add(declarado);
            return true;
        }
        return false;
    }
    
    /**
     * Clase interna para cada persona/entidad declarada
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Declarado347 {
        
        // NIF del declarado
        private String nif;
        
        // NIF del representante (si procede)
        private String nifRepresentante;
        
        // Nombre o razón social
        private String nombreRazonSocial;
        
        // Código provincia
        private String codigoProvincia;
        
        // Código país (si extranjero)
        private String codigoPais;
        
        // Clave de operación
        // "A" = Compras
        // "B" = Ventas
        // "C" = Pagos por cuenta de terceros
        // etc.
        private String claveOperacion;
        
        // Importe anual de las operaciones (con IVA)
        private BigDecimal importeAnual;
        
        // Operación seguro (S/N)
        private boolean operacionSeguro;
        
        // Arrendamiento local negocio (S/N)
        private boolean arrendamientoLocal;
        
        // Importe percibido en metálico
        private BigDecimal importeMetalico;
        
        // Importe percibido por transmisiones de inmuebles
        private BigDecimal importeInmuebles;
        
        // ========================================
        // DESGLOSE TRIMESTRAL
        // ========================================
        private BigDecimal importeTrimestre1;
        private BigDecimal importeTrimestre2;
        private BigDecimal importeTrimestre3;
        private BigDecimal importeTrimestre4;
        
        // Número de operaciones
        private int numOperaciones;
        
        /**
         * Calcula el importe anual a partir de los trimestres
         */
        public void calcularImporteAnual() {
            this.importeAnual = BigDecimal.ZERO;
            if (importeTrimestre1 != null) importeAnual = importeAnual.add(importeTrimestre1);
            if (importeTrimestre2 != null) importeAnual = importeAnual.add(importeTrimestre2);
            if (importeTrimestre3 != null) importeAnual = importeAnual.add(importeTrimestre3);
            if (importeTrimestre4 != null) importeAnual = importeAnual.add(importeTrimestre4);
        }
        
        /**
         * Verifica si debe declararse (supera umbral)
         */
        public boolean debeDeclararse() {
            return importeAnual != null && 
                   importeAnual.abs().compareTo(Modelo347DTO.UMBRAL_MINIMO) >= 0;
        }
    }
}
