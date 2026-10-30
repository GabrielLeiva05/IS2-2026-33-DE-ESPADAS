import react, { reactCompilerPreset } from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig } from 'vite'

// URL del backend Spring Boot
const BACKEND = 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] }),
    // Tailwind v4 necesita este plugin para procesar `@import "tailwindcss"` (sin el, no hay estilos)
    tailwindcss(),
  ],
  server: {
    port: 5173,
    // Proxy: el navegador habla SIEMPRE con localhost:5173 y Vite reenvia al backend.
    // Asi no hay problemas de CORS y el login con Google vuelve al mismo origen.
    proxy: {
      '/api': BACKEND,
      '/oauth2': BACKEND,
      '/login/oauth2': BACKEND,
    },
  },
  build: {
    // `npm run build` deja el frontend dentro del backend para servirlo desde http://localhost:8080
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true,
  },
})
