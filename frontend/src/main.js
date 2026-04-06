import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router/main'
import './assets/css/main.css'
import '@/interceptors/axios'
const app = createApp(App)

// 1. Global Error Handler for Vue components
app.config.errorHandler = (err, vm, info) => {
  console.error('[Global Error]:', err)
  console.error('[Component]:', vm)
  console.error('[Info]:', info)
}

// 3. Router Error Handling (Robust Chunk Failure Recovery)
router.onError((error, to) => {
  const chunkFailedMessage = [
    'failed to fetch dynamically imported module',
    'Importing a module script failed',
    'Loading chunk',
    'Cannot find module'
  ];

  const isChunkLoadFailed = chunkFailedMessage.some(msg =>
    error.message?.toLowerCase().includes(msg.toLowerCase())
  );

  if (isChunkLoadFailed) {
    console.warn('[Router]: Dynamic import failed at route:', to.path, '. Retrying via full reload...');
    window.location.reload();
  } else {
    console.error('[Router Error]:', error);
  }
})

app.use(createPinia())
app.use(router)
app.mount('#app')

