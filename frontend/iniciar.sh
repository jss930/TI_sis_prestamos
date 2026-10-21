#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")"

if ! command -v node >/dev/null 2>&1; then
  echo "ERROR: No se encontro Node.js en PATH."
  echo "Instala Node.js 18 o superior desde https://nodejs.org/ y vuelve a intentarlo."
  exit 1
fi

if command -v pnpm >/dev/null 2>&1; then
  PKG_MANAGER=pnpm
elif command -v npm >/dev/null 2>&1; then
  PKG_MANAGER=npm
else
  echo "ERROR: No se encontro pnpm ni npm en PATH."
  echo "Instala Node.js y vuelve a intentarlo."
  exit 1
fi

if [ ! -d node_modules ]; then
  echo "Instalando dependencias con $PKG_MANAGER..."
  "$PKG_MANAGER" install
fi

echo
echo "Iniciando el frontend (React + TypeScript + Vite)..."
echo "Abre la URL que muestre Vite."
echo "Para detener el servidor presiona Ctrl+C."
echo
exec "$PKG_MANAGER" run dev
