import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Dev proxy: the frontend calls /api/*, Vite forwards it to the Spring Boot backend.
// (Run the backend separately: java -jar app/target/superbowlrun-app-0.0.1-SNAPSHOT.jar)
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
