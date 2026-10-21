@echo off
setlocal
cd /d "%~dp0"

where node >nul 2>nul
if errorlevel 1 (
  echo ERROR: No se encontro Node.js en PATH.
  echo Instala Node.js 18 o superior desde https://nodejs.org/ y vuelve a intentarlo.
  pause
  exit /b 1
)

set "PKG_MANAGER="
where pnpm >nul 2>nul
if not errorlevel 1 set "PKG_MANAGER=pnpm"

if not defined PKG_MANAGER (
  where npm >nul 2>nul
  if not errorlevel 1 set "PKG_MANAGER=npm"
)

if not defined PKG_MANAGER (
  echo ERROR: No se encontro pnpm ni npm en PATH.
  echo Instala Node.js y vuelve a intentarlo.
  pause
  exit /b 1
)

if not exist "node_modules" (
  echo Instalando dependencias con %PKG_MANAGER%...
  call %PKG_MANAGER% install
  if errorlevel 1 (
    echo No se pudieron instalar las dependencias.
    pause
    exit /b 1
  )
)

echo.
echo Iniciando el frontend (React + TypeScript + Vite)...
echo Abre la URL que muestre Vite.
echo Para detener el servidor presiona Ctrl+C.
echo.
call %PKG_MANAGER% run dev

endlocal
