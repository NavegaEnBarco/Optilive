# OptiLive v0.3

MVP para seguimiento GPS de regatistas Optimist mediante teléfonos Android.

## Datos del perfil
- Número de vela
- Nombre
- Apellidos
- Categoría
- Club

## Funcionamiento actual
- GPS Android de alta precisión con actualizaciones aproximadas cada 2 segundos.
- Servicio en primer plano para mantener el seguimiento con la pantalla apagada.
- Envío HTTP de telemetría al servidor OptiLive.
- Cola local básica si falla la conexión y reintento posterior.
- Servidor Node.js con WebSocket.
- Mapa web para visualizar barcos conectados.
- GitHub Actions para compilar un APK debug instalable.

## Estructura
- `android-tracker/`: aplicación Android Kotlin.
- `server/`: servidor Node.js.
- `server/public/`: mapa web.
- `docs/`: arquitectura.
- `.github/workflows/android-apk.yml`: compilación automática del APK.

## Estado
- Conexión de escritura ChatGPT ↔ GitHub verificada.

## Próximos hitos
1. Generar y validar el primer APK.
2. Sustituir la cola básica por Room/SQLite.
3. Configuración segura del servidor y autenticación.
4. QR móvil-barco/regatista.
5. Gestión de regatas y sesiones.
6. Roles para organización, entrenadores y familias.
7. Reproducción histórica de recorridos.
8. Controles de privacidad y consentimiento para menores.
