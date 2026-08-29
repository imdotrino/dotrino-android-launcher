import { createApp } from 'vue'
import { registerSW } from 'virtual:pwa-register'
import '@dotrino/topbar'   // trae marca + idioma + <dotrino-support> (§5)
import App from './App.vue'
import './style.css'

const updateSW = registerSW({ immediate: true })
setInterval(() => updateSW(), 30 * 60 * 1000)

createApp(App).mount('#app')
