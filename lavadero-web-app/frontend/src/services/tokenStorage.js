// ─── Almacenamiento de la sesión ──────────────────────────────────────────────
// Se usa sessionStorage (no localStorage): al cerrar el navegador o la pestaña la
// sesión desaparece. Así nadie se queda "logueado" en el ordenador del lavadero.
//
// Nota: sessionStorage sigue siendo accesible desde JavaScript, así que lo que de verdad
// protege frente a robo de token es: no tener XSS, tokens cortos (30 min) y la revocación
// en el backend (logout / cambio de contraseña).

const TOKEN_KEY = 'authToken';
const USER_KEY = 'user';
const EXPIRES_KEY = 'authExpiresAt';

// Limpiar restos de la versión anterior, que guardaba el token en localStorage indefinidamente
try {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
} catch {
  /* navegador sin storage */
}

export const tokenStorage = {
  getToken: () => sessionStorage.getItem(TOKEN_KEY),

  getUser: () => {
    const raw = sessionStorage.getItem(USER_KEY);
    try {
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  },

  /** epoch millis en que caduca el token, o 0 si no se conoce */
  getExpiresAt: () => Number(sessionStorage.getItem(EXPIRES_KEY) || 0),

  guardar: ({ token, user, expiresAt }) => {
    if (token) sessionStorage.setItem(TOKEN_KEY, token);
    if (user) sessionStorage.setItem(USER_KEY, JSON.stringify(user));
    if (expiresAt) sessionStorage.setItem(EXPIRES_KEY, String(expiresAt));
  },

  limpiar: () => {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    sessionStorage.removeItem(EXPIRES_KEY);
  },

  hayToken: () => !!sessionStorage.getItem(TOKEN_KEY),
};
