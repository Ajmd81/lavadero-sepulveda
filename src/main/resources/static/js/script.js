// Funcionalidad para cargar horarios disponibles
document.addEventListener('DOMContentLoaded', function() {
    const fechaInput = document.getElementById('fecha');
    const horaSelect = document.getElementById('hora');

    if (fechaInput && horaSelect) {
        // Establecer fecha mínima como hoy
        const hoy = new Date().toISOString().split('T')[0];
        fechaInput.min = hoy;

        // Cargar horarios disponibles cuando cambie la fecha
        fechaInput.addEventListener('change', function() {
            if (this.value) {
                cargarHorariosDisponibles(this.value);
            }
        });

        // Si hay una fecha seleccionada al cargar la página, cargar horarios
        if (fechaInput.value) {
            cargarHorariosDisponibles(fechaInput.value);
        }
    }
});

function cargarHorariosDisponibles(fecha) {
    const horaSelect = document.getElementById('hora');
    const selectedHora = horaSelect.value; // Guardar la hora seleccionada actualmente

    fetch(`/horarios-disponibles?fecha=${fecha}`)
        .then(response => response.json())
        .then(horarios => {
            // Limpiar opciones actuales
            horaSelect.innerHTML = '<option value="">-- Seleccione una hora --</option>';

            // Agregar nuevas opciones
            horarios.forEach(hora => {
                const option = document.createElement('option');
                option.value = hora;
                option.textContent = hora.substring(0, 5); // Mostrar solo HH:MM

                // Si esta era la hora seleccionada anteriormente, seleccionarla de nuevo
                if (hora === selectedHora) {
                    option.selected = true;
                }

                horaSelect.appendChild(option);
            });

            // Si no hay horarios disponibles, mostrar mensaje
            if (horarios.length === 0) {
                const option = document.createElement('option');
                option.value = "";
                option.textContent = "No hay horarios disponibles";
                option.disabled = true;
                horaSelect.appendChild(option);
            }
        })
        .catch(error => {
            console.error('Error al cargar horarios:', error);
            horaSelect.innerHTML = '<option value="">Error al cargar horarios</option>';
        });
}