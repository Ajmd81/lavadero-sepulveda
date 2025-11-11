/**
 * Cookie Banner JavaScript
 * Gestiona el consentimiento de cookies y privacidad
 */

// Verificar si el usuario ya ha dado su consentimiento
function checkCookieConsent() {
    const consent = getCookie('cookieConsent');
    if (!consent) {
        showCookieBanner();
    } else {
        // Si ya aceptó, cargar servicios opcionales
        if (consent === 'accepted') {
            loadOptionalServices();
        }
    }
}

// Mostrar el banner de cookies
function showCookieBanner() {
    const banner = document.getElementById('cookieBanner');
    const overlay = document.getElementById('pageOverlay');

    if (banner && overlay) {
        // Pequeño delay para que se vea la animación
        setTimeout(() => {
            banner.classList.add('show');
            overlay.classList.add('show');

            // Prevenir scroll del body cuando está activo
            document.body.style.overflow = 'hidden';
        }, 1500); // Mostrar después de 1.5 segundos
    }
}

// Ocultar el banner de cookies
function hideCookieBanner() {
    const banner = document.getElementById('cookieBanner');
    const overlay = document.getElementById('pageOverlay');

    if (banner && overlay) {
        banner.classList.remove('show');
        overlay.classList.remove('show');

        // Restaurar scroll del body
        document.body.style.overflow = '';
    }
}

// Aceptar cookies
function acceptCookies() {
    // Guardar preferencias
    setCookie('cookieConsent', 'accepted', 365);
    setCookie('cookiePreferences', 'all', 365);
    setCookie('cookieTimestamp', new Date().toISOString(), 365);

    // Ocultar banner
    hideCookieBanner();

    // Cargar servicios opcionales
    loadOptionalServices();

    // Mostrar mensaje de confirmación (opcional)
    showToast('✓ Preferencias guardadas. Gracias por tu confianza.', 'success');

    console.log('Cookies aceptadas - Cargando servicios opcionales...');
}

// Rechazar cookies no esenciales
function declineCookies() {
    // Guardar preferencias
    setCookie('cookieConsent', 'declined', 365);
    setCookie('cookiePreferences', 'essential', 365);
    setCookie('cookieTimestamp', new Date().toISOString(), 365);

    // Ocultar banner
    hideCookieBanner();

    // Mostrar mensaje de confirmación (opcional)
    showToast('Solo se usarán cookies esenciales.', 'info');

    console.log('Cookies rechazadas - Solo cookies esenciales');
}

// Mostrar más información (abrir política de privacidad)
function showMoreInfo() {
    // Abrir política en nueva ventana
    window.open('/policy', '_blank');
}

// Funciones auxiliares para manejo de cookies
function setCookie(name, value, days) {
    const expires = new Date();
    expires.setTime(expires.getTime() + (days * 24 * 60 * 60 * 1000));
    const secure = location.protocol === 'https:' ? ';Secure' : '';
    document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/;SameSite=Lax${secure}`;
}

function getCookie(name) {
    const nameEQ = name + "=";
    const ca = document.cookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1, c.length);
        }
        if (c.indexOf(nameEQ) === 0) {
            return c.substring(nameEQ.length, c.length);
        }
    }
    return null;
}

function deleteCookie(name) {
    document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;`;
}

// Cargar servicios opcionales (Analytics, etc.)
function loadOptionalServices() {
    const consent = getCookie('cookieConsent');

    if (consent === 'accepted') {
        // Cargar Google Analytics
        loadGoogleAnalytics();

        // Cargar otros servicios opcionales
        loadFacebookPixel();
        loadOtherServices();

        console.log('Servicios opcionales cargados');
    }
}

// Cargar Google Analytics (solo si está aceptado)
function loadGoogleAnalytics() {
    // Reemplaza 'GA_MEASUREMENT_ID' con tu ID real
    const GA_ID = 'GA_MEASUREMENT_ID';

    if (GA_ID !== 'GA_MEASUREMENT_ID') {
        // Cargar script de Google Analytics
        const script = document.createElement('script');
        script.async = true;
        script.src = `https://www.googletagmanager.com/gtag/js?id=${GA_ID}`;
        document.head.appendChild(script);

        // Configurar Google Analytics
        script.onload = function() {
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', GA_ID, {
                'anonymize_ip': true,
                'cookie_flags': 'SameSite=Lax;Secure'
            });
        };

        console.log('Google Analytics cargado');
    }
}

// Cargar Facebook Pixel (ejemplo)
function loadFacebookPixel() {
    // Solo cargar si tienes Facebook Pixel configurado
    // const FACEBOOK_PIXEL_ID = 'TU_FACEBOOK_PIXEL_ID';
    // Implementar carga de Facebook Pixel aquí
    console.log('Facebook Pixel listo para cargar');
}

// Cargar otros servicios
function loadOtherServices() {
    // Aquí puedes cargar otros servicios como:
    // - Hotjar
    // - Crisp Chat
    // - Otros scripts de terceros
    console.log('Otros servicios listos para cargar');
}

// Mostrar toast de notificación (opcional)
function showToast(message, type = 'info') {
    // Crear elemento toast
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#28a745' : '#17a2b8'};
        color: white;
        padding: 12px 20px;
        border-radius: 4px;
        z-index: 10000;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        transform: translateX(100%);
        transition: transform 0.3s ease;
    `;
    toast.textContent = message;

    // Añadir al DOM
    document.body.appendChild(toast);

    // Mostrar con animación
    setTimeout(() => {
        toast.style.transform = 'translateX(0)';
    }, 100);

    // Ocultar después de 3 segundos
    setTimeout(() => {
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 3000);
}

// Función para resetear cookies (útil para testing)
function resetCookieConsent() {
    deleteCookie('cookieConsent');
    deleteCookie('cookiePreferences');
    deleteCookie('cookieTimestamp');
    location.reload();
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    checkCookieConsent();
});

// Función global para acceder desde la consola (útil para testing)
window.cookieBanner = {
    show: showCookieBanner,
    hide: hideCookieBanner,
    accept: acceptCookies,
    decline: declineCookies,
    reset: resetCookieConsent,
    getConsent: () => getCookie('cookieConsent')
};