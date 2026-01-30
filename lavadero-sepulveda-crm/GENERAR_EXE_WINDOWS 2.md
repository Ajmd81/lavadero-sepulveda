# Generar Ejecutable para Windows

Como estamos en macOS, no podemos generar directamente un .exe con jpackage. Sin embargo, aquí te proporciono dos opciones:

## Opción 1: Generar desde Windows (Recomendado)

Si tienes acceso a una máquina Windows:

```bash
# En Windows PowerShell o CMD
cd "C:\ruta\a\lavadero-sepulveda-crm"

# Limpiar y compilar
mvn clean package -DskipTests

# Generar el ejecutable
jpackage --input target --name "Lavadero Sepulveda CRM" ^
  --main-jar lavadero-sepulveda-crm-1.0.0.jar ^
  --main-class com.lavaderosepulveda.crm.LavaderoSepulvedaCRMApplication ^
  --type exe --app-version 1.0.0 --vendor "Lavadero Sepulveda" ^
  --icon src/main/resources/images/logo_crm.png ^
  --java-options "-Xmx2048m" ^
  --win-menu --win-menu-group "Lavadero Sepulveda"
```

El ejecutable aparecerá en el directorio raíz como: `Lavadero Sepulveda CRM-1.0.0.exe`

## Opción 2: Crear Instalador MSI (Windows)

Para crear un instalador profesional:

```bash
jpackage --input target --name "Lavadero Sepulveda CRM" ^
  --main-jar lavadero-sepulveda-crm-1.0.0.jar ^
  --main-class com.lavaderosepulveda.crm.LavaderoSepulvedaCRMApplication ^
  --type msi --app-version 1.0.0 --vendor "Lavadero Sepulveda" ^
  --icon src/main/resources/images/logo_crm.png ^
  --java-options "-Xmx2048m" ^
  --win-menu --win-menu-group "Lavadero Sepulveda"
```

## Opción 3: JAR Portable (Funciona en cualquier Windows)

Alternativamente, desde macOS puedes usar el JAR directamente:

```bash
java -jar target/lavadero-sepulveda-crm-1.0.0.jar
```

Este JAR ya está compilado y puede ejecutarse en cualquier máquina Windows que tenga Java 17+ instalado.

## Requisitos para Windows

- Java 17 o superior
- Al menos 2GB de RAM disponible
- Acceso administrativo (solo para instalar)

## Ubicación del Logo

El logo utilizado es: `src/main/resources/images/logo_crm.png`

Asegúrate de que esta imagen sea de alta calidad (256x256px mínimo) para que se vea bien en Windows.
