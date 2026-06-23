import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '')

  return {
    plugins: [vue()],
    server: {
      proxy: {
        '/gms-api': {
          target: 'https://gms.ssafy.io',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/gms-api/, '/gmsapi/api.openai.com'),
          configure: (proxy) => {
            proxy.on('proxyReq', (proxyRequest) => {
              if (env.GMS_KEY) proxyRequest.setHeader('Authorization', `Bearer ${env.GMS_KEY}`)
            })
          },
        },
      },
    },
  }
})
