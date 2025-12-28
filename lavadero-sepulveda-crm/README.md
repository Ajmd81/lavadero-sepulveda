# Lavadero Sepúlveda - CRM Desktop

Sistema CRM de escritorio desarrollado en JavaFX para la gestión integral de un lavadero de coches.

## 🚀 Características Principales

### ✅ Gestión de Clientes
- Registro completo de clientes con datos personales y de vehículos
- Seguimiento de estadísticas: citas completadas, canceladas, no presentaciones
- Histórico de facturación por cliente
- Identificación de clientes VIP y problemáticos

### 📅 Gestión de Citas
- Calendario de citas con múltiples estados
- Asignación de servicios a citas
- Control de tiempos (llegada, inicio, fin)
- Gestión de no presentaciones
- Recordatorios automáticos

### 💰 Facturación
- Generación automática de facturas desde citas
- Numeración automática de facturas
- Generación de PDF con diseño profesional
- **Envío automático por Email y WhatsApp**
- Control de pagos y vencimientos
- Múltiples formas de pago

### 📊 Dashboard
- Métricas en tiempo real
- Top 10 clientes por facturación
- Clientes con más no presentaciones
- Estadísticas de citas por estado
- Facturación del día, mes y año

### 📈 Contabilidad
- Reportes mensuales y anuales
- Desglose por cliente y servicio
- Exportación a Excel
- Cálculo automático de IVA

## 🛠️ Tecnologías Utilizadas

- **Java 17+**
- **JavaFX 21** - Interfaz de usuario
- **Hibernate 6** - ORM para persistencia
- **H2 Database** - Base de datos embebida
- **iText 8** - Generación de PDF
- **Apache POI** - Exportación Excel
- **JavaMail** - Envío de emails
- **OkHttp** - Cliente HTTP para WhatsApp API
- **Lombok** - Reducción de código boilerplate
- **SLF4J + Logback** - Logging

## 📋 Requisitos Previos

- Java JDK 17 o superior
- Maven 3.6+
- (Opcional) Cuenta SMTP para envío de emails
- (Opcional) WhatsApp Business API para envío de mensajes

## 🔧 Instalación y Configuración

### 1. Clonar el repositorio o descomprimir los archivos

```bash
cd lavadero-sepulveda-crm
```

### 2. Compilar el proyecto

```bash
mvn clean install
```

### 3. Ejecutar la aplicación

```bash
mvn javafx:run
```

O crear un JAR ejecutable:

```bash
mvn clean package
java -jar target/lavadero-sepulveda-crm-1.0.0.jar
```

## ⚙️ Configuración de Servicios

### Configuración de Email

Para habilitar el envío automático de facturas por email, configura los siguientes parámetros en el código o a través de la interfaz de configuración:

```java
EmailService emailService = EmailService.getInstance();
emailService.configurarSmtp(
    "smtp.gmail.com",           // Host SMTP
    "587",                       // Puerto
    "tu-email@gmail.com",        // Usuario
    "tu-contraseña-app",         // Contraseña de aplicación
    "tu-email@gmail.com"         // Email remitente
);
```

**Nota para Gmail:** Necesitarás crear una "Contraseña de aplicación" en tu cuenta de Google.

### Configuración de WhatsApp Business API

Para habilitar el envío automático por WhatsApp:

```java
WhatsAppService whatsAppService = WhatsAppService.getInstance();
whatsAppService.configurarApi(
    "TU_PHONE_NUMBER_ID",        // ID del número de teléfono
    "TU_ACCESS_TOKEN"            // Token de acceso de Meta
);
```

**Opciones de proveedores:**
- Meta WhatsApp Business API (Facebook)
- Twilio
- MessageBird
- Vonage

## 📁 Estructura del Proyecto

```
lavadero-sepulveda-crm/
├── src/main/java/
│   └── com/lavaderosepulveda/crm/
│       ├── model/              # Entidades JPA
│       ├── repository/         # Repositorios de datos
│       ├── service/            # Lógica de negocio
│       ├── controller/         # Controladores JavaFX
│       ├── config/             # Configuración
│       └── util/               # Utilidades
├── src/main/resources/
│   ├── fxml/                   # Vistas FXML
│   ├── css/                    # Estilos CSS
│   └── META-INF/               # persistence.xml
├── data/                       # Base de datos H2 (generada)
├── facturas/                   # PDFs generados (generada)
└── pom.xml
```

## 💾 Base de Datos

La aplicación utiliza H2 Database en modo embebido. La base de datos se crea automáticamente en la carpeta `./data/` al ejecutar la aplicación por primera vez.

Para acceder a la consola H2:
- URL: `jdbc:h2:file:./data/lavadero_crm`
- Usuario: `sa`
- Contraseña: (vacía)

## 📧 Automatización de Facturas

### Envío por Email

El sistema puede enviar facturas automáticamente por email con:
- Factura en PDF adjunta
- Cuerpo del mensaje personalizado
- Registro de envíos en la base de datos

### Envío por WhatsApp

Integración con WhatsApp Business API para:
- Envío de facturas en PDF
- Recordatorios de citas
- Confirmaciones de reserva
- Mensajes personalizados

### Configurar Envío Automático

En el módulo de facturación, al generar una factura:

```java
// Enviar por ambos canales
facturaService.enviarFacturaAutomatica(facturaId, true, true);

// Solo por email
facturaService.enviarFacturaPorEmail(facturaId);

// Solo por WhatsApp
facturaService.enviarFacturaPorWhatsApp(facturaId);
```

## 🎨 Personalización

### Modificar colores y estilos

Edita `src/main/resources/css/styles.css`:

```css
.root {
    -fx-primary-color: #2196F3;  /* Color principal */
    -fx-accent-color: #4CAF50;    /* Color de acento */
}
```

### Personalizar formato de facturas

Edita `PDFService.java` para cambiar:
- Logo de la empresa
- Datos de contacto
- Formato de factura
- Condiciones de pago

## 🔍 Funcionalidades Futuras

- [ ] Integración con Google Calendar
- [ ] App móvil complementaria
- [ ] Sistema de promociones y descuentos
- [ ] Programa de fidelización
- [ ] Integración con pasarelas de pago
- [ ] Generación de modelos fiscales
- [ ] Multi-sucursal
- [ ] API REST para integraciones

## 🐛 Solución de Problemas

### La aplicación no inicia

1. Verificar versión de Java: `java -version`
2. Verificar que Maven haya compilado correctamente
3. Revisar logs en consola

### Errores de base de datos

1. Eliminar carpeta `./data/`
2. La base de datos se regenerará automáticamente

### Problemas con envío de emails

1. Verificar configuración SMTP
2. Comprobar que el puerto no esté bloqueado
3. Para Gmail, usar contraseña de aplicación, no contraseña normal

### Problemas con WhatsApp

1. Verificar que la API key sea válida
2. Comprobar formato de números de teléfono (+34XXXXXXXXX)
3. Verificar límites de mensajería del plan

## 📞 Soporte

Para soporte y consultas sobre el proyecto, contactar a través de los issues del repositorio.

## 📝 Licencia

Este proyecto es de código propietario para uso interno de Lavadero Sepúlveda.

## 👥 Autor

Antonio - Desarrollo completo del sistema CRM

---

**Versión:** 1.0.0  
**Última actualización:** Diciembre 2024
