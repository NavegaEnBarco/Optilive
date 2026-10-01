# Subir OptiLive v0.3 a GitHub y generar el APK

Repositorio: `NavegaEnBarco/Optilive`

## Subida desde la web de GitHub
1. Descomprime `Optilive-v0.3.zip`.
2. Entra en el repositorio `NavegaEnBarco/Optilive`.
3. Usa **Add file > Upload files**.
4. Arrastra TODO el contenido de la carpeta descomprimida, incluida la carpeta `.github`.
5. Confirma con **Commit changes** sobre la rama principal.

## Generar el APK
Al subir los archivos, GitHub Actions intentará compilar automáticamente.
También se puede iniciar manualmente en **Actions > Build OptiLive Android APK > Run workflow**.

Cuando termine correctamente:
1. Abre la ejecución verde.
2. Baja hasta **Artifacts**.
3. Descarga `OptiLive-v0.3-debug-apk`.
4. Descomprime el artifact y encontrarás `app-debug.apk`.

El APK debug está firmado automáticamente por Android para pruebas y se puede instalar en un móvil Android permitiendo la instalación desde la fuente usada para descargarlo.
