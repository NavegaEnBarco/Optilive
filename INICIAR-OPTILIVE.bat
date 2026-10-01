@echo off
setlocal
title OptiLive GPS Server
cd /d "%~dp0"

echo ==========================================
echo          OPTILIVE GPS SERVER
echo ==========================================
echo.

where node >nul 2>&1
if errorlevel 1 (
  echo ERROR: Node.js no esta instalado o no esta en PATH.
  echo Instala Node.js y vuelve a ejecutar este archivo.
  pause
  exit /b 1
)

if not exist node_modules (
  echo Primera ejecucion: instalando dependencias...
  call npm install
  if errorlevel 1 (
    echo ERROR instalando dependencias.
    pause
    exit /b 1
  )
)

for /f "tokens=2 delims=:" %%A in ('ipconfig ^| findstr /c:"IPv4"') do (
  for /f "tokens=*" %%B in ("%%A") do set "IP=%%B"
)

if not defined IP set "IP=localhost"

echo.
echo Servidor local: http://localhost:3000
echo Direccion para el movil: http://%IP%:3000
echo.
echo Abriendo mapa OptiLive...
start "" "http://localhost:3000"

echo.
echo IMPORTANTE: deja esta ventana abierta durante el seguimiento.
echo Para detener OptiLive pulsa Ctrl+C o cierra esta ventana.
echo.
call npm start
pause
