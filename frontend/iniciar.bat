@echo off
setlocal

set "NODE_DIR=C:\Users\JOSE\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin"
set "PNPM_CMD=C:\Users\JOSE\.cache\codex-runtimes\codex-primary-runtime\dependencies\bin\fallback\pnpm.cmd"

if not exist "%NODE_DIR%\node.exe" (
  echo ERROR: No se encontro Node.js.
  echo Instala Node.js desde https://nodejs.org/ y vuelve a intentarlo.
  pause
  exit /b 1
)

if not exist "%PNPM_CMD%" (
  echo ERROR: No se encontro pnpm.
  pause
  exit /b 1
)

set "PATH=%NODE_DIR%;%PATH%"
cd /d "%~dp0"

if not exist "node_modules" (
  echo Instalando dependencias...
  call "%PNPM_CMD%" install
  if errorlevel 1 (
    echo No se pudieron instalar las dependencias.
    pause
    exit /b 1
  )
)

echo.
echo Iniciando el sistema de prestamos...
echo Abre en el navegador la direccion que aparezca debajo.
echo Para detener el servidor presiona Ctrl+C.
echo.
call "%PNPM_CMD%" run dev

endlocal
