package com.lavaderosepulveda.app.model;

import jakarta.persistence.*;

/**
 * Configuración de la plantilla de facturas
 * Se guarda en base de datos para persistencia
 */
@Entity
@Table(name = "config_plantilla_factura")
public class PlantillaFacturaConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================
    // DATOS DEL EMISOR
    // ========================================
    @Column(name = "emisor_nombre")
    private String emisorNombre = "LAVADERO SEPÚLVEDA";
    
    @Column(name = "emisor_nif")
    private String emisorNif = "44372738L";
    
    @Column(name = "emisor_direccion")
    private String emisorDireccion = "C/ Ingeniero Ruiz de Azua s/n Local 8";
    
    @Column(name = "emisor_codigo_postal")
    private String emisorCodigoPostal = "14006";
    
    @Column(name = "emisor_ciudad")
    private String emisorCiudad = "Córdoba";
    
    @Column(name = "emisor_provincia")
    private String emisorProvincia = "Córdoba";
    
    @Column(name = "emisor_telefono")
    private String emisorTelefono = "";
    
    @Column(name = "emisor_email")
    private String emisorEmail = "";
    
    @Column(name = "emisor_web")
    private String emisorWeb = "";

    // ========================================
    // LOGO
    // ========================================
    @Column(name = "logo_base64", columnDefinition = "LONGTEXT")
    private String logoBase64 = "";
    
    @Column(name = "logo_ancho")
    private Integer logoAncho = 150;
    
    @Column(name = "logo_alto")
    private Integer logoAlto = 60;

    // ========================================
    // COLORES
    // ========================================
    @Column(name = "color_primario")
    private String colorPrimario = "#2196F3";
    
    @Column(name = "color_secundario")
    private String colorSecundario = "#1976D2";
    
    @Column(name = "color_texto")
    private String colorTexto = "#333333";
    
    @Column(name = "color_texto_claro")
    private String colorTextoClaro = "#666666";
    
    @Column(name = "color_fondo")
    private String colorFondo = "#FFFFFF";
    
    @Column(name = "color_fondo_alt")
    private String colorFondoAlt = "#F5F5F5";
    
    @Column(name = "color_borde")
    private String colorBorde = "#E0E0E0";
    
    @Column(name = "color_exito")
    private String colorExito = "#4CAF50";

    // ========================================
    // TEXTOS
    // ========================================
    @Column(name = "titulo_factura")
    private String tituloFactura = "FACTURA";
    
    @Column(name = "titulo_factura_simplificada")
    private String tituloFacturaSimplificada = "FACTURA SIMPLIFICADA";
    
    @Column(name = "pie_factura", columnDefinition = "TEXT")
    private String pieFactura = "";
    
    @Column(name = "condiciones_pago")
    private String condicionesPago = "Pago al contado";
    
    @Column(name = "cuenta_bancaria")
    private String cuentaBancaria = "";
    
    @Column(name = "texto_gracias")
    private String textoGracias = "Gracias por confiar en nosotros";
    
    @Column(name = "texto_iva")
    private String textoIva = "IVA incluido según normativa vigente";

    // ========================================
    // OPCIONES
    // ========================================
    @Column(name = "mostrar_logo")
    private Boolean mostrarLogo = true;
    
    @Column(name = "mostrar_datos_contacto")
    private Boolean mostrarDatosContacto = true;
    
    @Column(name = "mostrar_cuenta_bancaria")
    private Boolean mostrarCuentaBancaria = false;
    
    @Column(name = "mostrar_condiciones_pago")
    private Boolean mostrarCondicionesPago = true;
    
    @Column(name = "mostrar_texto_gracias")
    private Boolean mostrarTextoGracias = true;
    
    @Column(name = "mostrar_marca_agua")
    private Boolean mostrarMarcaAgua = false;
    
    @Column(name = "usar_filas_alternas")
    private Boolean usarFilasAlternas = true;

    // ========================================
    // CONSTRUCTORES
    // ========================================
    public PlantillaFacturaConfig() {
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getLogoBase64() { return logoBase64; }
    public void setLogoBase64(String logoBase64) { this.logoBase64 = logoBase64; }

    public Integer getLogoAncho() { return logoAncho; }
    public void setLogoAncho(Integer logoAncho) { this.logoAncho = logoAncho; }

    public Integer getLogoAlto() { return logoAlto; }
    public void setLogoAlto(Integer logoAlto) { this.logoAlto = logoAlto; }

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

    public Boolean getMostrarLogo() { return mostrarLogo; }
    public void setMostrarLogo(Boolean mostrarLogo) { this.mostrarLogo = mostrarLogo; }

    public Boolean getMostrarDatosContacto() { return mostrarDatosContacto; }
    public void setMostrarDatosContacto(Boolean mostrarDatosContacto) { this.mostrarDatosContacto = mostrarDatosContacto; }

    public Boolean getMostrarCuentaBancaria() { return mostrarCuentaBancaria; }
    public void setMostrarCuentaBancaria(Boolean mostrarCuentaBancaria) { this.mostrarCuentaBancaria = mostrarCuentaBancaria; }

    public Boolean getMostrarCondicionesPago() { return mostrarCondicionesPago; }
    public void setMostrarCondicionesPago(Boolean mostrarCondicionesPago) { this.mostrarCondicionesPago = mostrarCondicionesPago; }

    public Boolean getMostrarTextoGracias() { return mostrarTextoGracias; }
    public void setMostrarTextoGracias(Boolean mostrarTextoGracias) { this.mostrarTextoGracias = mostrarTextoGracias; }

    public Boolean getMostrarMarcaAgua() { return mostrarMarcaAgua; }
    public void setMostrarMarcaAgua(Boolean mostrarMarcaAgua) { this.mostrarMarcaAgua = mostrarMarcaAgua; }

    public Boolean getUsarFilasAlternas() { return usarFilasAlternas; }
    public void setUsarFilasAlternas(Boolean usarFilasAlternas) { this.usarFilasAlternas = usarFilasAlternas; }

    /**
     * Dirección completa formateada
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
        return sb.toString();
    }
}
