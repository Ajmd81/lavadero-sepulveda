/**
 * theme-loader.js
 * Script que se carga en todas las páginas de admin para aplicar
 * automáticamente las preferencias guardadas del usuario
 */

(function() {
    'use strict';

    // Aplicar SOLO a documentElement inmediatamente (antes de que body exista)
    function applyThemeToHTML() {
        const darkMode = localStorage.getItem('darkMode') === 'true';
        if (darkMode) {
            document.documentElement.classList.add('dark-theme');
        }

        const fontSize = localStorage.getItem('fontSize') || 'medium';
        document.documentElement.classList.add('font-' + fontSize);

        const compactView = localStorage.getItem('compactView') === 'true';
        if (compactView) {
            document.documentElement.classList.add('compact-view');
        }
    }

    // Aplicar a BODY cuando esté disponible
    function applyThemeToBody() {
        if (!document.body) return;

        const darkMode = localStorage.getItem('darkMode') === 'true';
        if (darkMode) {
            document.body.classList.add('dark-theme');
        }

        const fontSize = localStorage.getItem('fontSize') || 'medium';
        document.body.classList.add('font-' + fontSize);

        const compactView = localStorage.getItem('compactView') === 'true';
        if (compactView) {
            document.body.classList.add('compact-view');
        }
    }

    // Aplicar a HTML inmediatamente para evitar parpadeo
    applyThemeToHTML();

    // Aplicar a BODY cuando esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', applyThemeToBody);
    } else {
        applyThemeToBody();
    }

    // Auto-refresh de citas (si está habilitado)
    document.addEventListener('DOMContentLoaded', function() {
        const autoRefresh = localStorage.getItem('autoRefresh') === 'true';
        const refreshInterval = parseInt(localStorage.getItem('refreshInterval') || '60') * 1000;

        if (autoRefresh && window.location.pathname.includes('/admin/listado-citas')) {
            setInterval(function() {
                location.reload();
            }, refreshInterval);
        }
    });

})();