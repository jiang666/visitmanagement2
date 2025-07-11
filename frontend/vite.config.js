import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath, URL } from 'url';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)) 
    }
  },
  server: {
    port: 8284,
    proxy: {
      '/api': {
        // target: 'http://localhost:10086',
        target: 'http://127.0.0.1:10086',
        changeOrigin: true,
        // keep `/api` prefix so backend routes like `/api/users` work
        rewrite: (path) => path
      }
    }
  },
});
