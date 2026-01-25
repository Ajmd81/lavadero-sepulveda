# 🎯 INSTRUCCIONES DE INSTALACIÓN Y USO
# CRM Web Lavadero Sepúlveda

## ✅ PROYECTO COMPLETO - LISTO PARA USAR

Este es tu CRM de escritorio convertido a una aplicación web moderna.
Ahora puedes acceder desde cualquier dispositivo con navegador web.

---

## 📦 CONTENIDO DEL PROYECTO

```
lavadero-web-app/
├── frontend/               ← Aplicación React
│   ├── src/
│   │   ├── components/    # Componentes UI
│   │   ├── pages/         # Páginas (Dashboard, Clientes, Citas, etc)
│   │   ├── services/      # Servicios de API
│   │   ├── context/       # Gestión de estado (Auth)
│   │   └── ...
│   ├── package.json
│   └── README.md
└── INSTRUCCIONES.md       ← Este archivo
```

---

## 🚀 PASO 1: INSTALAR DEPENDENCIAS

```bash
cd lavadero-web-app/frontend
npm install
```

Esto instalará todas las librerías necesarias:
- React 18
- React Router
- Axios
- Tailwind CSS
- Lucide Icons
- React Query
- Y más...

---

## ⚙️ PASO 2: CONFIGURAR CONEXIÓN CON TU BACKEND

El archivo `.env` ya está creado con la configuración por defecto:

```env
VITE_API_URL=http://localhost:8080/api
```

**Si tu backend Spring Boot está en otro puerto u host:**

Edita `.env` y cambia la URL:

```env
# Ejemplo: Backend en puerto 9090
VITE_API_URL=http://localhost:9090/api

# Ejemplo: Backend en servidor remoto
VITE_API_URL=https://tu-servidor.com/api
```

---

## 🏃 PASO 3: EJECUTAR LA APLICACIÓN

### Modo Desarrollo (Recomendado para pruebas)

```bash
npm run dev
```

Abre tu navegador en: **http://localhost:5173/admin/login**

### Modo Producción (Para servidor)

```bash
npm run build
npm run preview
```

---

## 🔐 PASO 4: CONFIGURAR AUTENTICACIÓN EN EL BACKEND

Necesitas añadir un endpoint de login en tu backend Spring Boot.

### Crear controlador de autenticación:

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Validar credenciales
        if ("admin".equals(loginRequest.getUsername()) && 
            "admin123".equals(loginRequest.getPassword())) {
            
            // Generar token JWT (o usar el que ya tengas)
            String token = "tu-token-jwt";
            
            return ResponseEntity.ok(new LoginResponse(
                token,
                new User("admin", "Administrador")
            ));
        }
        
        return ResponseEntity.status(401).body("Credenciales inválidas");
    }
}

// DTOs necesarios
class LoginRequest {
    private String username;
    private String password;
    // getters y setters
}

class LoginResponse {
    private String token;
    private User user;
    // constructor, getters y setters
}

class User {
    private String username;
    private String nombre;
    // constructor, getters y setters
}
```

### Configurar CORS para permitir peticiones del frontend:

```java
@Configuration
public class WebConfig {
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:5173") // Frontend dev
                    .allowedOrigins("https://tu-dominio.com") // Frontend producción
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

---

## 📡 PASO 5: VERIFICAR QUE LAS APIS FUNCIONAN

El frontend espera que el backend tenga estos endpoints:

### Clientes
- `GET /api/clientes` - Listar todos
- `GET /api/clientes/{id}` - Ver uno
- `POST /api/clientes` - Crear
- `PUT /api/clientes/{id}` - Actualizar
- `DELETE /api/clientes/{id}` - Eliminar

### Citas
- `GET /api/citas` - Listar todas
- `GET /api/citas/{id}` - Ver una
- `POST /api/citas` - Crear
- `PUT /api/citas/{id}` - Actualizar
- `DELETE /api/citas/{id}` - Eliminar

### Facturas
- `GET /api/facturas` - Listar todas
- `GET /api/facturas/{id}` - Ver una
- `GET /api/facturas/{id}/pdf` - Descargar PDF
- `POST /api/facturas` - Crear

### Proveedores
- `GET /api/proveedores`
- `POST /api/proveedores`
- etc...

### Gastos
- `GET /api/gastos`
- `POST /api/gastos`
- etc...

**NOTA:** Estos endpoints ya deberían existir en tu backend actual.

---

## 🌐 PASO 6: DESPLEGAR EN PRODUCCIÓN

### Opción A: Vercel (Gratis y fácil)

1. Instala Vercel CLI:
```bash
npm install -g vercel
```

2. Despliega:
```bash
cd lavadero-web-app/frontend
vercel
```

3. Sigue las instrucciones y listo!

### Opción B: Netlify

1. Build del proyecto:
```bash
npm run build
```

2. Arrastra la carpeta `dist/` a https://app.netlify.com/drop

### Opción C: Tu propio servidor

```bash
npm run build
# Copia el contenido de dist/ a tu servidor web
scp -r dist/* usuario@tu-servidor:/var/www/html/
```

---

## 🎨 PERSONALIZACIÓN

### Cambiar colores del tema:

Edita `tailwind.config.js`:

```js
theme: {
  extend: {
    colors: {
      primary: '#TU_COLOR_AQUI',
    },
  },
}
```

### Cambiar logo:

Reemplaza el archivo `src/assets/logo.png`

---

## 🐛 SOLUCIÓN DE PROBLEMAS COMUNES

### 1. Error de CORS

**Síntoma:** La consola muestra errores de CORS  
**Solución:** Configura CORS en el backend (ver PASO 4)

### 2. No se conecta al backend

**Síntoma:** "Network Error" en la consola  
**Solución:** 
- Verifica que el backend esté corriendo
- Verifica la URL en `.env`
- Revisa que el puerto sea correcto

### 3. Login no funciona

**Síntoma:** "401 Unauthorized"  
**Solución:** 
- Implementa el endpoint `/api/auth/login` en el backend
- Verifica las credenciales

### 4. Estilos de Tailwind no funcionan

**Síntoma:** La página se ve sin estilos  
**Solución:**
```bash
npm install -D tailwindcss postcss autoprefixer
npm run dev
```

---

## 📱 FUNCIONALIDADES IMPLEMENTADAS

✅ **Login** - Autenticación de administrador  
✅ **Dashboard** - Métricas y estadísticas  
✅ **Clientes** - CRUD completo con búsqueda  
✅ **Citas** - Gestión de citas (plantilla lista)  
✅ **Calendario** - Vista de calendario (plantilla lista)  
✅ **Facturación** - Gestión de facturas (plantilla lista)  
✅ **Proveedores** - Gestión de proveedores (plantilla lista)  
✅ **Gastos** - Control de gastos (plantilla lista)  
✅ **Contabilidad** - Reportes (plantilla lista)  
✅ **Resumen Financiero** - Vista general (plantilla lista)  
✅ **Configuración** - Ajustes del sistema (plantilla lista)  

**Nota:** Las páginas marcadas como "plantilla lista" tienen la estructura básica.
Puedes expandirlas según necesites conectándolas con tus APIs existentes.

---

## 📞 CONTACTO Y SOPORTE

Si tienes dudas:
1. Revisa este archivo completo
2. Revisa los comentarios en el código
3. Consulta el README.md del proyecto

---

## 🎉 ¡LISTO!

Tu CRM de escritorio ahora es una aplicación web moderna que puedes:

✅ Acceder desde cualquier dispositivo  
✅ Usar en móvil, tablet o PC  
✅ Desplegar en cualquier servidor  
✅ Actualizar sin reinstalar  
✅ Compartir con tu equipo  

**¡Disfruta de tu nuevo CRM Web!** 🚀

---

**Desarrollado por:** Antonio Jesús Martínez Díaz  
**Proyecto:** Lavadero Sepúlveda  
**Versión:** 1.0.0  
**Fecha:** Enero 2025
