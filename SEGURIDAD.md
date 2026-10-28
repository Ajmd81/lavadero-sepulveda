# Seguridad del backend — Lavadero Sepúlveda

Resumen de cómo está protegida la API tras la rama `security/hardening`.

## Mapa de rutas

### Públicas (sin JWT) — todas con rate limiting

| Método | Ruta | Uso | Límite específico |
|---|---|---|---|
| POST | `/api/auth/login` | Login CRM | 5/15 min y 10/h por IP · 10 fallos/30 min por usuario |
| POST | `/api/auth/logout` | Revocar token propio | global |
| POST | `/api/citas` | Reserva pública | 2/5 min y 5/h por IP. Sin JWT se ignoran `id`, `estado`, pagos y duración |
| POST | `/api/chatbot/message` | Chatbot web | 10/5 min y 30/h por IP |
| GET | `/api/citas/horarios-disponibles` | Formulario | global |
| GET | `/api/citas/disponibilidad-mes` · `/disponibilidad-mensual` | Calendario | global |
| GET | `/api/horarios`, `/api/tipos-lavado`, `/api/enums/**` | Datos públicos | global |
| GET | `/api/dias-cerrados/fechas-cerradas` | Calendario | global (rango ≤ 400 días) |
| GET | `/api/vehicle/classify`, `/classify-by-id`, `/brands-models` | Formulario | global |
| POST | `/guardar-cita` (Thymeleaf) | Reserva web | 2/5 min y 5/h por IP + honeypot + lista blanca de campos |

Límite global anónimo: 60 peticiones/min por IP (ráfaga máx. 20 en 5 s).

### Protegidas (JWT obligatorio)

Todo lo demás bajo `/api/**`. Límite global: 240/min y 6000/h por usuario, más 300/min por IP.
Sin token o con token caducado/revocado → `401` JSON.

### Rutas eliminadas

- `POST /api/citas/migrar-email`, `POST /api/citas/migrar-estado` (ALTER TABLE vía API)
- `/api/admin/migracion/**` (MigracionController)
- `GET /api/debug/buscar-modelo`

## Sesiones y tokens

- JWT de **30 min**, renovado con `/api/auth/refresh` mientras haya actividad; **máximo 8 h** desde el login.
- El frontend cierra la sesión tras **30 min sin actividad** y guarda el token en `sessionStorage`
  (cerrar el navegador = cerrar sesión).
- `logout` revoca el token de ese dispositivo (lista en memoria por `jti`).
- Cambiar contraseña o "Cerrar sesión en todos los dispositivos" incrementa `usuarios.token_version`
  → todos los tokens anteriores dejan de valer.
- El `sub` del token es el ID del usuario; los endpoints de perfil usan siempre el usuario del token.

## Variables de entorno (Railway)

| Variable | Obligatoria | Valor |
|---|---|---|
| `JWT_SECRET` | Sí | ≥ 64 bytes. Generar: `openssl rand -base64 64` |
| `APP_CORS_ALLOWED_ORIGINS` | No | Por defecto Vercel + www.lavaderosepulveda.es + dominio Railway. En local añadir `http://localhost:5173` |
| `JWT_EXPIRATION_MINUTES` | No | 30 |
| `JWT_MAX_SESSION_HOURS` | No | 8 |
| `CLIENT_IP_HEADER` | No | `X-Real-IP` (Railway). Con Cloudflare delante: `CF-Connecting-IP` |

## Pendiente / limitaciones conocidas

- Rate limiting y tokens revocados viven en memoria: válido con 1 réplica. Con varias → Redis.
- Tras un reinicio, un token revocado por logout vuelve a valer hasta su caducidad (máx. 30 min).
- El token sigue siendo legible por JavaScript (sessionStorage). La protección completa frente a XSS
  sería una cookie `HttpOnly; Secure; SameSite` servida desde el mismo dominio que el frontend.
- Panel Thymeleaf `/admin/**`: usa form login con sesión, pero la app es `STATELESS`; además
  `GET /admin/eliminar-cita/{id}` y `GET /admin/recordatorios/**` modifican datos por GET.
  Si ya no se usa, conviene eliminarlo.
