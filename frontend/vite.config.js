import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    // En desarrollo, todo lo que empiece por /api se lo paso al backend (puerto 8080).
    // Así el frontend llama a /api/contactos sin escribir la URL completa y no hay problemas de CORS.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
