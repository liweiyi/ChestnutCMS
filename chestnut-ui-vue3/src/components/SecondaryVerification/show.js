import { createApp, ref, h, onMounted, nextTick } from 'vue'
import ElementPlus from 'element-plus'
import SecondaryVerification from './index.vue'
import { i18n } from '@/i18n'
import store from '@/store'

let pendingPromise = null

export function showSecondaryVerification() {
  if (pendingPromise) {
    return pendingPromise
  }

  const container = document.createElement('div')
  document.body.appendChild(container)

  pendingPromise = new Promise((resolve, reject) => {
    const app = createApp({
      setup() {
        const compRef = ref(null)

        onMounted(() => {
          nextTick(() => {
            compRef.value.open().then(token => {
              resolve(token)
              cleanup()
            }).catch(() => {
              reject(new Error('cancelled'))
              cleanup()
            })
          })
        })

        return () => h(SecondaryVerification, { ref: compRef })
      }
    })

    app.use(i18n)
    app.use(store)
    app.use(ElementPlus)
    app.mount(container)

    function cleanup() {
      pendingPromise = null
      setTimeout(() => {
        app.unmount()
        if (container.parentNode) {
          container.parentNode.removeChild(container)
        }
      }, 300)
    }
  })

  return pendingPromise
}
