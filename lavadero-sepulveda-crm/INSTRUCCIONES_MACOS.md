# Solución para ejecutar Lavadero Sepulveda CRM en macOS

Si la aplicación no se abre desde el DMG, usa estos comandos alternativos:

## Opción 1: Ejecutar directamente desde Terminal

```bash
cd "/Applications/Lavadero Sepulveda CRM.app/Contents"
java -XstartOnFirstThread -Xmx2048m -jar lavadero-sepulveda-crm-1.0.0.jar
```

## Opción 2: Crear un alias ejecutable

Crea un script `run-crm.sh` en tu carpeta home:

```bash
#!/bin/bash
java -XstartOnFirstThread -Xmx2048m -jar "/Applications/Lavadero Sepulveda CRM.app/Contents/lavadero-sepulveda-crm-1.0.0.jar"
```

Luego hazlo ejecutable:
```bash
chmod +x ~/run-crm.sh
```

Y ejecútalo:
```bash
~/run-crm.sh
```

## Opción 3: Usar el JAR directamente

Si descargaste el proyecto desde GitHub:

```bash
cd /ruta/al/proyecto
java -XstartOnFirstThread -Xmx2048m -jar target/lavadero-sepulveda-crm-1.0.0.jar
```

## Requisitos

- Java 17 o superior
- Mínimo 2GB de RAM disponible
- Conexión de red (para la API)

## Solución de problemas

### "No se puede abrir la aplicación"
- Abre Terminal y ejecuta el comando desde la Opción 1

### "Error: faltan componentes de JavaFX"
- Asegúrate de tener Java 17+ instalado
- Intenta con: `java -version`

### "Connection refused"
- Verifica que la API esté corriendo en la URL configurada
- Por defecto: `http://localhost:8080/api`

