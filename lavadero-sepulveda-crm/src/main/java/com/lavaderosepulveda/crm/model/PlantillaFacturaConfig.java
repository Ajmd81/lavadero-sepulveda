package com.lavaderosepulveda.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Configuración de la plantilla de facturas
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantillaFacturaConfig {

    // ========================================
    // DATOS DEL EMISOR
    // ========================================
    private String emisorNombre = "LAVADERO SEPÚLVEDA";
    private String emisorNif = "44372738L";
    private String emisorDireccion = "C/ Ingeniero Ruiz de Azua s/n Local 8";
    private String emisorCodigoPostal = "14006";
    private String emisorCiudad = "Córdoba";
    private String emisorProvincia = "Córdoba";
    private String emisorTelefono = "";
    private String emisorEmail = "";
    private String emisorWeb = "";

    // ========================================
    // LOGO
    // ========================================
    private String logoBase64 = ""; // Logo en Base64
    private String logoPath = ""; // Ruta al archivo de logo
    private int logoAncho = 150; // Ancho del logo en px
    private int logoAlto = 60; // Alto del logo en px

    // ========================================
    // COLORES (en formato hexadecimal)
    // ========================================
    private String colorPrimario = "#2196F3"; // Azul - cabecera
    private String colorSecundario = "#1976D2"; // Azul oscuro - acentos
    private String colorTexto = "#333333"; // Gris oscuro - texto principal
    private String colorTextoClaro = "#666666"; // Gris - texto secundario
    private String colorFondo = "#FFFFFF"; // Blanco - fondo
    private String colorFondoAlt = "#F5F5F5"; // Gris claro - filas alternas
    private String colorBorde = "#E0E0E0"; // Gris - bordes
    private String colorExito = "#4CAF50"; // Verde - pagado

    // ========================================
    // TEXTOS PERSONALIZABLES
    // ========================================
    private String tituloFactura = "FACTURA";
    private String tituloFacturaSimplificada = "FACTURA SIMPLIFICADA";

    // Pie de factura
    private String pieFactura = "";
    private String condicionesPago = "Pago al contado";
    private String cuentaBancaria = "";
    private String textoGracias = "Gracias por confiar en nosotros";

    // Textos legales
    private String textoIva = "IVA incluido según normativa vigente";
    private String textoProteccionDatos = "";

    // ========================================
    // OPCIONES DE DISEÑO
    // ========================================
    private boolean mostrarLogo = true;
    private boolean mostrarDatosContacto = true;
    private boolean mostrarCuentaBancaria = false;
    private boolean mostrarCondicionesPago = true;
    private boolean mostrarTextoGracias = true;
    private boolean mostrarMarcaAgua = false; // Marca de agua "PAGADA"
    private boolean usarFilasAlternas = true; // Colores alternos en tabla

    // Fuentes
    private String fuentePrincipal = "Helvetica";
    private int tamanoFuenteNormal = 10;
    private int tamanoFuenteTitulo = 18;
    private int tamanoFuenteSubtitulo = 12;

    // ========================================
    // CONSTRUCTORES
    // ========================================
    // Lombok @Data genera constructores requeridos, pero mantenemos el vacío
    // explícito si es necesario para frameworks de serialización
    public PlantillaFacturaConfig() {
    }

    /**
     * Obtener dirección completa formateada
     */
    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder();
        if (emisorDireccion != null && !emisorDireccion.isEmpty()) {
            sb.append(emisorDireccion);
        }
        if (emisorCodigoPostal != null && !emisorCodigoPostal.isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(emisorCodigoPostal);
        }
        if (emisorCiudad != null && !emisorCiudad.isEmpty()) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(emisorCiudad);
        }
        if (emisorProvincia != null && !emisorProvincia.isEmpty() && !emisorProvincia.equals(emisorCiudad)) {
            if (sb.length() > 0)
                sb.append(" (");
            sb.append(emisorProvincia).append(")");
        }
        return sb.toString();
    }
}
