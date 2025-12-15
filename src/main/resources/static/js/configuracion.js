/**
 * configuracion.js
 * Lógica para la página de configuración
 */

// Cargar configuración al iniciar
document.addEventListener('DOMContentLoaded', function() {
    loadConfiguration();
});

// Función para cargar la configuración guardada
function loadConfiguration() {
    // Modo Oscuro
    const darkMode = localStorage.getItem('darkMode') === 'true';
    document.getElementById('darkModeToggle').checked = darkMode;
    if (darkMode) {
        document.documentElement.classList.add('dark-theme');
        document.body.classList.add('dark-theme');
    }

    // Tamaño de texto
    const fontSize = localStorage.getItem('fontSize') || 'medium';
    document.getElementById('fontSizeSelect').value = fontSize;
    applyFontSize(fontSize);

    // Vista compacta
    const compactView = localStorage.getItem('compactView') === 'true';
    document.getElementById('compactViewToggle').checked = compactView;
    if (compactView) {
        document.documentElement.classList.add('compact-view');
        document.body.classList.add('compact-view');
    }

    // Notificaciones
    document.getElementById('emailNotificationsToggle').checked =
        localStorage.getItem('emailNotifications') !== 'false';
    document.getElementById('soundNotificationsToggle').checked =
        localStorage.getItem('soundNotifications') === 'true';
    document.getElementById('reminderNotificationsToggle').checked =
        localStorage.getItem('reminderNotifications') !== 'false';

    // Auto-refresh
    document.getElementById('autoRefreshToggle').checked =
        localStorage.getItem('autoRefresh') === 'true';
    const refreshInterval = localStorage.getItem('refreshInterval') || '60';
    document.getElementById('refreshIntervalSelect').value = refreshInterval;

    // Horarios
    document.getElementById('openingTime').value =
        localStorage.getItem('openingTime') || '09:00';
    document.getElementById('closingTime').value =
        localStorage.getItem('closingTime') || '19:00';
    const appointmentDuration = localStorage.getItem('appointmentDuration') || '30';
    document.getElementById('appointmentDurationSelect').value = appointmentDuration;

    // Privacidad
    document.getElementById('searchHistoryToggle').checked =
        localStorage.getItem('searchHistory') !== 'false';
    document.getElementById('rememberFiltersToggle').checked =
        localStorage.getItem('rememberFilters') !== 'false';
}

// Toggle de modo oscuro con efecto inmediato
document.getElementById('darkModeToggle').addEventListener('change', function() {
    if (this.checked) {
        document.documentElement.classList.add('dark-theme');
        document.body.classList.add('dark-theme');
    } else {
        document.documentElement.classList.remove('dark-theme');
        document.body.classList.remove('dark-theme');
    }
});

// Aplicar tamaño de fuente
function applyFontSize(size) {
    document.documentElement.classList.remove('font-small', 'font-medium', 'font-large');
    document.body.classList.remove('font-small', 'font-medium', 'font-large');
    document.documentElement.classList.add('font-' + size);
    document.body.classList.add('font-' + size);
}

document.getElementById('fontSizeSelect').addEventListener('change', function() {
    applyFontSize(this.value);
});

// Vista compacta con efecto inmediato
document.getElementById('compactViewToggle').addEventListener('change', function() {
    if (this.checked) {
        document.documentElement.classList.add('compact-view');
        document.body.classList.add('compact-view');
    } else {
        document.documentElement.classList.remove('compact-view');
        document.body.classList.remove('compact-view');
    }
});

// Función para guardar toda la configuración
function saveConfiguration() {
    // Apariencia
    localStorage.setItem('darkMode', document.getElementById('darkModeToggle').checked);
    localStorage.setItem('fontSize', document.getElementById('fontSizeSelect').value);
    localStorage.setItem('compactView', document.getElementById('compactViewToggle').checked);

    // Notificaciones
    localStorage.setItem('emailNotifications', document.getElementById('emailNotificationsToggle').checked);
    localStorage.setItem('soundNotifications', document.getElementById('soundNotificationsToggle').checked);
    localStorage.setItem('reminderNotifications', document.getElementById('reminderNotificationsToggle').checked);

    // Auto-refresh
    localStorage.setItem('autoRefresh', document.getElementById('autoRefreshToggle').checked);
    localStorage.setItem('refreshInterval', document.getElementById('refreshIntervalSelect').value);

    // Horarios
    localStorage.setItem('openingTime', document.getElementById('openingTime').value);
    localStorage.setItem('closingTime', document.getElementById('closingTime').value);
    localStorage.setItem('appointmentDuration', document.getElementById('appointmentDurationSelect').value);

    // Privacidad
    localStorage.setItem('searchHistory', document.getElementById('searchHistoryToggle').checked);
    localStorage.setItem('rememberFilters', document.getElementById('rememberFiltersToggle').checked);

    // Mostrar mensaje de éxito
    showSaveMessage();
}

// Función para restablecer valores por defecto
function resetToDefaults() {
    if (confirm('¿Estás seguro de que quieres restablecer todas las configuraciones a los valores por defecto?')) {
        localStorage.clear();
        location.reload();
    }
}

// Mostrar mensaje de guardado
function showSaveMessage() {
    const message = document.getElementById('saveMessage');
    message.classList.add('show');
    setTimeout(() => {
        message.classList.remove('show');
    }, 3000);
}