import './assets/main.css'
import 'element-plus/dist/index.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(router)

// 确保路由准备就绪后再挂载
router.isReady().then(() => {
  app.mount('#app')
}).catch((err) => {
  console.error('Router initialization failed:', err)
  app.mount('#app')
})