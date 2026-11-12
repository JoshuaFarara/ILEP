import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// In development, the editor runs on Vite (port 5173) and forwards API calls to the service on 8080.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
  build: {
    outDir: "dist",
    sourcemap: false,
    // CodeMirror and React together are about 225 KB gzipped; one bundle is fine for this app.
    chunkSizeWarningLimit: 800,
  },
});
