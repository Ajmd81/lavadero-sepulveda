# Proyecto Integrado Lavadero Sepúlveda

---

## 1. **Descripción del Proyecto**

El **Sistema Integral de Reservas – Lavadero Sepúlveda** es una plataforma multiplataforma que facilita la gestión de citas y la administración de los servicios en un lavadero de vehículos. Consiste en una aplicación web y una versión móvil que permiten a los usuarios realizar reservas de servicios de manera eficiente, mientras que los administradores gestionan las citas y supervisan el rendimiento del lavadero. El sistema ofrece funcionalidades como:

- **Reservas online** 24/7.
- **Gestión de citas** para clientes y servicios realizados.
- **Estadísticas y reportes** de ocupación y demanda.
- **Accesibilidad** desde plataformas web y móviles.

🛠️ **Tecnologías Principales**

- **Backend:** Spring Boot 3.x, Spring Data JPA, MySQL  
- **Frontend Web:** Thymeleaf, HTML5, CSS3, JS  
- **Móvil:** Android Studio (Java, Retrofit)   
- **DevOps:** Docker, Maven, Git

---
## 2. 🎨 Detalles de la Interfaz de Usuario

### Sistema de Clasificación de Vehículos
El formulario de reservas incluye un sistema inteligente de clasificación:

// Clasificación automática basada en modelo de vehículo
- Turismo → Toggle Sedán/Ranchera
- Monovolumen → Servicios específicos
- Todoterreno → Categorías Grande/Pequeño
- Furgonetas → Clasificación por tamaño

### Componentes UI Personalizados
- Toggle de vehículos: Botones con dimensiones fijas (200x50px)
- Estados visuales: Normal, Hover, Seleccionado
- Responsive design: Adaptación automática en móviles
- Feedback de carga: Indicadores de estado durante clasificación
- Validación en tiempo real: Verificación de disponibilidad instantánea

### Paleta de Colores
`color-primary: #0099ff;`

`color-warning: #ffc107;`

`color-warning-light: #fff3cd;`

`color-warning-text: #856404;`

---

## 3. **Información sobre Despliegue**

### **Requisitos Previos**

### Aplicación Web:

* **Navegadores soportados**
* **Java 17**
* **Programas para crear BD y lanzar la APP:** MySQL Workbench | IDE (IntelliJ, Eclipse, VS Code...)

### Aplicación Móvil:

* **Sistema Operativo:** Android
* **Conexión a internet para sincronizar datos.**
* **Programas para emular y crear el puente:** Android Studio y Railway

### Instalación:

·Aplicación Móvil:
    
       Seguir instrucciones del manual de instalacion.

      
·Aplicación de Escritorio:
    
      Seguir instrucciones del manual de instalacion.
  
---

## 4. **Link de despliegue y Apk de la Aplicación Android** 

## ***https://lavadero-sepulveda-production.up.railway.app***
## [Aplicacion Android](app-movil/app-debug.apk) 


## 👨‍💻 **Autores**

**Antonio Jesús Martínez Díaz**  
**Rocío Zuara Jiménez**  
**Lorena Barea Rot**  
📚 *2º DAM — I.E.S. Gran Capitán*  
📆 *Curso 2025/2026*

---

## 📜 **Licencia**
Este proyecto se distribuye bajo la licencia **MIT**.  
Consulta el archivo [LICENSE](./LICENSE) para más información.
