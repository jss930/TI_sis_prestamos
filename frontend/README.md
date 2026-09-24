# Frontend — Sistema de préstamos

Interfaz React sencilla para gestionar libros, laptops, lentes inteligentes y material deportivo.

## Ejecutar

```bash
npm install
npm run dev
```

Copia `.env.example` como `.env` si necesitas cambiar la URL del backend.

El frontend captura información, realiza peticiones y muestra respuestas. Las reglas sobre límites, sanciones, disponibilidad y plazos pertenecen al backend. Mientras se conecta la API, la interfaz usa `src/data/datosDemo.js`; las peticiones preparadas están en `src/services/api.js`.
