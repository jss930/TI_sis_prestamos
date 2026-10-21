# Frontend — Prototipo legacy (aún no integrado)

> **Aviso:** esta carpeta es el prototipo React del proyecto anterior. **No consume la API
> actual** (`/api/v1`) y varias de sus pantallas usan datos mock. Se conserva como
> referencia visual para la fase 3. No lo uses para probar el backend del corte 1.
>
> La integración real será una migración a React + TypeScript + Vite, con un cliente HTTP
> que hable con `/api/v1` y maneje los errores `ProblemDetail` (`codigo`).

Interfaz React sencilla para gestionar libros, laptops, lentes inteligentes y material deportivo.

## Ejecutar

```bash
npm install
npm run dev
```

Copia `.env.example` como `.env` si necesitas cambiar la URL del backend.

El frontend captura información, realiza peticiones y muestra respuestas. Las reglas sobre límites, sanciones, disponibilidad y plazos pertenecen al backend. Mientras se conecta la API, la interfaz usa `src/data/datosDemo.js`; las peticiones preparadas están en `src/services/api.js`.
