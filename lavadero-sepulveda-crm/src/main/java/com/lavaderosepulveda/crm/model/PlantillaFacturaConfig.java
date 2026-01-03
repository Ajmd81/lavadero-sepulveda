package com.lavaderosepulveda.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Configuración de la plantilla de facturas
 */
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
    private String logoPath = "";   // Ruta al archivo de logo
    private int logoAncho = 150;    // Ancho del logo en px
    private int logoAlto = 60;      // Alto del logo en px

    // ========================================
    // COLORES (en formato hexadecimal)
    // ========================================
    private String colorPrimario = "#2196F3";      // Azul - cabecera
    private String colorSecundario = "#1976D2";    // Azul oscuro - acentos
    private String colorTexto = "#333333";         // Gris oscuro - texto principal
    private String colorTextoClaro = "#666666";    // Gris - texto secundario
    private String colorFondo = "#FFFFFF";         // Blanco - fondo
    private String colorFondoAlt = "#F5F5F5";      // Gris claro - filas alternas
    private String colorBorde = "#E0E0E0";         // Gris - bordes
    private String colorExito = "#4CAF50";         // Verde - pagado

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
    private boolean mostrarMarcaAgua = false;     // Marca de agua "PAGADA"
    private boolean usarFilasAlternas = true;     // Colores alternos en tabla
    
    // Fuentes
    private String fuentePrincipal = "Helvetica";
    private int tamanoFuenteNormal = 10;
    private int tamanoFuenteTitulo = 18;
    private int tamanoFuenteSubtitulo = 12;

    // ========================================
    // CONSTRUCTORES
    // ========================================
    public PlantillaFacturaConfig() {
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    // Emisor
    public String getEmisorNombre() { return emisorNombre; }
    public void setEmisorNombre(String emisorNombre) { this.emisorNombre = emisorNombre; }

    public String getEmisorNif() { return emisorNif; }
    public void setEmisorNif(String emisorNif) { this.emisorNif = emisorNif; }

    public String getEmisorDireccion() { return emisorDireccion; }
    public void setEmisorDireccion(String emisorDireccion) { this.emisorDireccion = emisorDireccion; }

    public String getEmisorCodigoPostal() { return emisorCodigoPostal; }
    public void setEmisorCodigoPostal(String emisorCodigoPostal) { this.emisorCodigoPostal = emisorCodigoPostal; }

    public String getEmisorCiudad() { return emisorCiudad; }
    public void setEmisorCiudad(String emisorCiudad) { this.emisorCiudad = emisorCiudad; }

    public String getEmisorProvincia() { return emisorProvincia; }
    public void setEmisorProvincia(String emisorProvincia) { this.emisorProvincia = emisorProvincia; }

    public String getEmisorTelefono() { return emisorTelefono; }
    public void setEmisorTelefono(String emisorTelefono) { this.emisorTelefono = emisorTelefono; }

    public String getEmisorEmail() { return emisorEmail; }
    public void setEmisorEmail(String emisorEmail) { this.emisorEmail = emisorEmail; }

    public String getEmisorWeb() { return emisorWeb; }
    public void setEmisorWeb(String emisorWeb) { this.emisorWeb = emisorWeb; }

    // Logo
    public String getLogoBase64() { return logoBase64; }
    public void setLogoBase64(String logoBase64) { this.logoBase64 = logoBase64; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public int getLogoAncho() { return logoAncho; }
    public void setLogoAncho(int logoAncho) { this.logoAncho = logoAncho; }

    public int getLogoAlto() { return logoAlto; }
    public void setLogoAlto(int logoAlto) { this.logoAlto = logoAlto; }

    // Colores
    public String getColorPrimario() { return colorPrimario; }
    public void setColorPrimario(String colorPrimario) { this.colorPrimario = colorPrimario; }

    public String getColorSecundario() { return colorSecundario; }
    public void setColorSecundario(String colorSecundario) { this.colorSecundario = colorSecundario; }

    public String getColorTexto() { return colorTexto; }
    public void setColorTexto(String colorTexto) { this.colorTexto = colorTexto; }

    public String getColorTextoClaro() { return colorTextoClaro; }
    public void setColorTextoClaro(String colorTextoClaro) { this.colorTextoClaro = colorTextoClaro; }

    public String getColorFondo() { return colorFondo; }
    public void setColorFondo(String colorFondo) { this.colorFondo = colorFondo; }

    public String getColorFondoAlt() { return colorFondoAlt; }
    public void setColorFondoAlt(String colorFondoAlt) { this.colorFondoAlt = colorFondoAlt; }

    public String getColorBorde() { return colorBorde; }
    public void setColorBorde(String colorBorde) { this.colorBorde = colorBorde; }

    public String getColorExito() { return colorExito; }
    public void setColorExito(String colorExito) { this.colorExito = colorExito; }

    // Textos
    public String getTituloFactura() { return tituloFactura; }
    public void setTituloFactura(String tituloFactura) { this.tituloFactura = tituloFactura; }

    public String getTituloFacturaSimplificada() { return tituloFacturaSimplificada; }
    public void setTituloFacturaSimplificada(String tituloFacturaSimplificada) { this.tituloFacturaSimplificada = tituloFacturaSimplificada; }

    public String getPieFactura() { return pieFactura; }
    public void setPieFactura(String pieFactura) { this.pieFactura = pieFactura; }

    public String getCondicionesPago() { return condicionesPago; }
    public void setCondicionesPago(String condicionesPago) { this.condicionesPago = condicionesPago; }

    public String getCuentaBancaria() { return cuentaBancaria; }
    public void setCuentaBancaria(String cuentaBancaria) { this.cuentaBancaria = cuentaBancaria; }

    public String getTextoGracias() { return textoGracias; }
    public void setTextoGracias(String textoGracias) { this.textoGracias = textoGracias; }

    public String getTextoIva() { return textoIva; }
    public void setTextoIva(String textoIva) { this.textoIva = textoIva; }

    public String getTextoProteccionDatos() { return textoProteccionDatos; }
    public void setTextoProteccionDatos(String textoProteccionDatos) { this.textoProteccionDatos = textoProteccionDatos; }

    // Opciones
    public boolean isMostrarLogo() { return mostrarLogo; }
    public void setMostrarLogo(boolean mostrarLogo) { this.mostrarLogo = mostrarLogo; }

    public boolean isMostrarDatosContacto() { return mostrarDatosContacto; }
    public void setMostrarDatosContacto(boolean mostrarDatosContacto) { this.mostrarDatosContacto = mostrarDatosContacto; }

    public boolean isMostrarCuentaBancaria() { return mostrarCuentaBancaria; }
    public void setMostrarCuentaBancaria(boolean mostrarCuentaBancaria) { this.mostrarCuentaBancaria = mostrarCuentaBancaria; }

    public boolean isMostrarCondicionesPago() { return mostrarCondicionesPago; }
    public void setMostrarCondicionesPago(boolean mostrarCondicionesPago) { this.mostrarCondicionesPago = mostrarCondicionesPago; }

    public boolean isMostrarTextoGracias() { return mostrarTextoGracias; }
    public void setMostrarTextoGracias(boolean mostrarTextoGracias) { this.mostrarTextoGracias = mostrarTextoGracias; }

    public boolean isMostrarMarcaAgua() { return mostrarMarcaAgua; }
    public void setMostrarMarcaAgua(boolean mostrarMarcaAgua) { this.mostrarMarcaAgua = mostrarMarcaAgua; }

    public boolean isUsarFilasAlternas() { return usarFilasAlternas; }
    public void setUsarFilasAlternas(boolean usarFilasAlternas) { this.usarFilasAlternas = usarFilasAlternas; }

    // Fuentes
    public String getFuentePrincipal() { return fuentePrincipal; }
    public void setFuentePrincipal(String fuentePrincipal) { this.fuentePrincipal = fuentePrincipal; }

    public int getTamanoFuenteNormal() { return tamanoFuenteNormal; }
    public void setTamanoFuenteNormal(int tamanoFuenteNormal) { this.tamanoFuenteNormal = tamanoFuenteNormal; }

    public int getTamanoFuenteTitulo() { return tamanoFuenteTitulo; }
    public void setTamanoFuenteTitulo(int tamanoFuenteTitulo) { this.tamanoFuenteTitulo = tamanoFuenteTitulo; }

    public int getTamanoFuenteSubtitulo() { return tamanoFuenteSubtitulo; }
    public void setTamanoFuenteSubtitulo(int tamanoFuenteSubtitulo) { this.tamanoFuenteSubtitulo = tamanoFuenteSubtitulo; }

    /**
     * Obtener dirección completa formateada
     */
    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder();
        if (emisorDireccion != null && !emisorDireccion.isEmpty()) {
            sb.append(emisorDireccion);
        }
        if (emisorCodigoPostal != null && !emisorCodigoPostal.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(emisorCodigoPostal);
        }
        if (emisorCiudad != null && !emisorCiudad.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(emisorCiudad);
        }
        if (emisorProvincia != null && !emisorProvincia.isEmpty() && !emisorProvincia.equals(emisorCiudad)) {
            if (sb.length() > 0) sb.append(" (");
            sb.append(emisorProvincia).append(")");
        }
        return sb.toString();
    }
}
