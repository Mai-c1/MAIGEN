import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ArcoVue from '@arco-design/web-vue'
import ArcoVueIcon from '@arco-design/web-vue/es/icon'
import '@arco-design/web-vue/dist/arco.css'
import './style.css'
import App from './App.vue'
import router from './router'
import { permission } from './directives/permission'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ArcoVue)
app.use(ArcoVueIcon)

// 注册全局指令
app.directive('permission', permission)

// 默认开启暗黑模式
document.body.setAttribute('arco-theme', 'dark')

app.mount('#app')
