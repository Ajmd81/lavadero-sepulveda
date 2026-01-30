# Ejecutar Lavadero Sepulveda CRM en macOS

## ⚡ Solución Rápida (3 pasos)

### Paso 0: Verificar tu versión de Java
```bash
java -version
```

### Paso 1: Descargar JavaFX SDK (versión que coincida con tu Java)
1. Ve a **https://gluonhq.com/products/javafx/**
2. Descarga **JavaFX 25** si tienes Java 25, o **JavaFX SDK 21.0.1** si tienes Java 21
3. Extrae el ZIP en tu carpeta Home:
   - Para Java 25: `~/javafx-sdk-25`
   - Para Java 21: `~/javafx-sdk-21.0.1`

**Verificar que está bien:**
```bash
# Para Java 25
ls ~/javafx-sdk-25/lib

# Para Java 21
ls ~/javafx-sdk-21.0.1/lib
# Deberías ver: javafx-controls-*.jar, javafx-fxml-*.jar, etc.
```

### Paso 2: Ejecutar el instalador

```bash
cd ~/Desktop/LavaderoCompleto/lavadero-sepulveda/lavadero-sepulveda-crm
./setup-complete.sh
```

**Eso es todo.** El script se encargará de compilar y ejecutar la aplicación.

---

## 🎯 ¿Por qué funciona así?

Los JDKs estándar (OpenJDK, Temurin, etc.) **NO incluyen JavaFX por defecto**. OpenJFX lo proporciona por separado. Indicándole a Java dónde están los módulos de JavaFX, puede cargarlos y todo funciona.

---

## 📋 Alternativa: Línea de Comando Manual

Si prefieres máximo control, ejecuta directamente:

```bash
cd ~/Desktop/LavaderoCompleto/lavadero-sepulveda/lavadero-sepulveda-crm

# Compilar
mvn clean package -DskipTests

# Ejecutar para Java 25
java \
  --module-path ~/javafx-sdk-25/lib \
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web \
  -XstartOnFirstThread \
  -Xmx2048m \
  -jar target/lavadero-sepulveda-crm-1.0.0.jar

# O para Java 21
java \
  --module-path ~/javafx-sdk-21.0.1/lib \
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web \
  -XstartOnFirstThread \
  -Xmx2048m \
  -jar target/lavadero-sepulveda-crm-1.0.0.jar
```

---

## 🐛 Solucionar Problemas

### "java: command not found"
Instala Java 25 o 21:
```bash
brew install temurin@25
# o
brew install temurin@21
```

### "Module javafx.controls not found"
JavaFX SDK no está en el sitio correcto o versión incorrecta.
```bash
# Verifica que existe para Java 25:
ls ~/javafx-sdk-25/lib/javafx-controls-25.jar

# O para Java 21:
ls ~/javafx-sdk-21.0.1/lib/javafx-controls-21.0.1.jar

# Si no existe, descárgalo nuevamente desde:
# https://gluonhq.com/products/javafx/
```

### La aplicación se abre pero con errores
Ejecuta en la terminal para ver los logs:
```bash
./setup-complete.sh 2>&1 | tee app.log
```

### Error de permisos "Permission denied"
```bash
chmod +x setup-complete.sh run-macos.sh
```

---

## 📦 Crear un DMG Instalador (Opcional)

Una vez que funcione todo, puedes crear un instalador `.dmg`:

```bash
cd ~/Desktop/LavaderoCompleto/lavadero-sepulveda/lavadero-sepulveda-crm
chmod +x build-macos.sh
./build-macos.sh
```

Esto generará `Lavadero-Sepulveda-CRM.dmg` que puedes distribuir.

---

## ✅ Checklist

- [ ] Verifiqué mi versión de Java (java -version)
- [ ] Descargué JavaFX SDK que coincida con mi versión de Java
- [ ] Extraje el ZIP en `~/javafx-sdk-25` o `~/javafx-sdk-21.0.1`
- [ ] Ejecuté `./setup-complete.sh`
- [ ] La aplicación se abrió correctamente

Si algo falla, comparte el error exacto que ves.
