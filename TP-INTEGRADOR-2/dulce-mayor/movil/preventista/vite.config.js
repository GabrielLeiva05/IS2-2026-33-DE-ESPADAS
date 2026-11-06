import react from '@vitejs/plugin-react'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vite'

// El código compartido con la app del distribuidor vive en ../comun (alias @comun).
// "dedupe" obliga a resolver React, el router, Axios y Leaflet desde esta app, para que haya una sola copia.
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@comun': fileURLToPath(new URL('../comun/src', import.meta.url)) },
    dedupe: ['react', 'react-dom', 'react-router', 'axios', 'leaflet', 'react-leaflet'],
  },
  server: { port: 5173, strictPort: true, fs: { allow: ['..'] } },
  preview: { port: 5173 },
  build: { chunkSizeWarningLimit: 700 }, // React + Leaflet en un solo paquete
})
