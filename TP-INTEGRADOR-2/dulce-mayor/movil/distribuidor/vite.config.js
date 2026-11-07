import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'
import { fileURLToPath } from 'node:url'

// El dev server habla con la API a través del proxy, así que la app funciona sin configurar
// VITE_API_URL. El puerto 5174 está incluido en el CORS por defecto del backend.
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, fileURLToPath(new URL('.', import.meta.url)), '')
  return {
    plugins: [react()],
    server: {
      port: 5174,
      strictPort: false,
      proxy: {
        '/api': {
          target: env.VITE_API_PROXY_TARGET || 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
  }
})

