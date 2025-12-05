# Refactorización del Servicio de Clasificación de Vehículos

Este documento detalla la refactorización realizada para mover la lógica de clasificación de vehículos desde una base de datos en memoria (hardcodeada en el código) a la base de datos principal de la aplicación (MySQL).

## Resumen de Cambios

El objetivo de esta refactorización fue mejorar la mantenibilidad, escalabilidad y flexibilidad del sistema de clasificación de vehículos. Anteriormente, la lista de más de 300 modelos de vehículos y sus categorías estaba definida directamente en la clase `VehicleClassificationService.java`, lo que hacía que cualquier actualización requiriera una modificación del código fuente y un nuevo despliegue de la aplicación.

La nueva implementación utiliza dos tablas en la base de datos para almacenar esta información y un servicio refactorizado que consulta la base de datos.

## Nuevo Esquema de la Base de Datos

Se han añadido dos nuevas tablas a la base de datos:

1.  `vehicle_categories`
    *   Almacena las diferentes categorías de vehículos.
    *   **Columnas:**
        *   `id` (BIGINT, PK, Auto-Increment): Identificador único.
        *   `name` (VARCHAR, Unique): Nombre interno de la categoría (ej. `turismo`, `todoterreno`).
        *   `description` (VARCHAR): Descripción legible para el usuario (ej. `Turismo`, `Todoterreno Grande`).

2.  `vehicle_models`
    *   Almacena los modelos de vehículos y su categoría asociada.
    *   **Columnas:**
        *   `id` (BIGINT, PK, Auto-Increment): Identificador único.
        *   `name` (VARCHAR): Nombre del modelo (ej. `golf`, `passat`, `qashqai`).
        *   `category_id` (BIGINT, FK): Referencia a la tabla `vehicle_categories`.

## Nuevos Archivos Creados

Se han creado los siguientes archivos para implementar la nueva lógica:

### 1. Entidades JPA (`/src/main/java/com/lavaderosepulveda/app/model/`)

*   `VehicleCategory.java`: Entidad que mapea la tabla `vehicle_categories`.
*   `VehicleModel.java`: Entidad que mapea la tabla `vehicle_models` y gestiona la relación `ManyToOne` con `VehicleCategory`.

### 2. Repositorios JPA (`/src/main/java/com/lavaderosepulveda/app/repository/`)

*   `VehicleCategoryRepository.java`: Repositorio para realizar operaciones CRUD sobre las categorías.
*   `VehicleModelRepository.java`: Repositorio para los modelos. Incluye una consulta personalizada (`findFirstByNameContainingNormalized`) para buscar modelos de forma flexible, ignorando mayúsculas, minúsculas y espacios.

### 3. Inicializador de Datos (`/src/main/java/com/lavaderosepulveda/app/config/`)

*   `DataInitializer.java`: Un componente `CommandLineRunner` que se ejecuta al arrancar la aplicación. Su función es:
    1.  Verificar si la tabla de categorías está vacía.
    2.  Si está vacía, puebla las tablas `vehicle_categories` y `vehicle_models` con todos los datos que antes estaban en el `VehicleClassificationService`.
    *   **Esto asegura que los datos se migran automáticamente a la base de datos en el primer despliegue sin necesidad de scripts manuales.**

## Servicio Refactorizado

La clase `VehicleClassificationService.java` ha sido completamente reescrita:

*   **Eliminación de Datos Hardcodeados**: Se ha eliminado el mapa `vehicleDatabase` con más de 300 entradas.
*   **Inyección de Repositorios**: Ahora inyecta `VehicleModelRepository` y `VehicleCategoryRepository` para acceder a la base de datos.
*   **Lógica Simplificada**: El método `classifyVehicle` ahora normaliza el nombre del modelo y utiliza el `modelRepository` para buscar la categoría en la base de datos. La lógica es más limpia, simple y eficiente.
*   **Mantenimiento**: El servicio ya no necesita ser modificado para añadir nuevos vehículos. Las actualizaciones se hacen directamente en la base de datos.

## Cómo Funciona Ahora

1.  **Primer Arranque**: Al iniciar la aplicación por primera vez, el `DataInitializer` detecta que las tablas de vehículos están vacías y las llena con los datos por defecto.
2.  **Clasificación de un Vehículo**: Cuando un usuario introduce un modelo de vehículo en el formulario, la aplicación llama al `VehicleClassificationService`.
3.  **Consulta a la Base de Datos**: El servicio busca el modelo en la tabla `vehicle_models`.
4.  **Respuesta**: Si encuentra el modelo, devuelve la categoría asociada. Si no, utiliza un sistema de detección por palabras clave como método alternativo.

Esta refactorización hace que el sistema sea mucho más profesional, robusto y fácil de mantener a largo plazo.
