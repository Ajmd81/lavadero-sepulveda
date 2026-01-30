import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    // Aumentar el límite de warning de chunk size a 1 MB
    chunkSizeWarningLimit: 1024,
    // Configurar rollup para mejor code splitting
    rollupOptions: {
      output: {
        // Separar vendors para mejor caching
        manualChunks: {
          'vendor': [
            'react',
            'react-dom',
            'react-router-dom'
          ],
          'api': [
            './src/services/api.js',
            './src/services/facturaService.js',
            './src/services/citaService.js',
            './src/services/clienteService.js',
            './src/services/gastoService.js',
            './src/services/proveedorService.js',
            './src/services/facturaRecibidaService.js',
          ]
        }
      }
    }
  }
})
