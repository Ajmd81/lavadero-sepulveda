import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    chunkSizeWarningLimit: 600,
    rollupOptions: {
      output: {
        manualChunks: {
          // React core — se cachea bien, cambia poco
          'vendor-react': [
            'react',
            'react-dom',
            'react-router-dom'
          ],
          // Librerías pesadas separadas — solo cargan cuando se necesitan
          'vendor-charts': ['recharts'],
          'vendor-pdf':    ['jspdf'],
          'vendor-excel':  ['xlsx'],
          // Tus servicios — separados del bundle de componentes
          'services': [
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