import { debounce } from '@/utils'
import { onMounted, onActivated, onBeforeUnmount, onDeactivated } from 'vue'

export default function useResize(chartRef) {
  let sidebarElm = null
  let resizeHandler = null

  const sidebarResizeHandler = (e) => {
    if (e.propertyName === 'width') {
      resizeHandler()
    }
  }

  const initListener = () => {
    resizeHandler = debounce(() => {
      resize()
    }, 100)
    window.addEventListener('resize', resizeHandler)

    sidebarElm = document.getElementsByClassName('sidebar-container')[0]
    sidebarElm && sidebarElm.addEventListener('transitionend', sidebarResizeHandler)
  }

  const destroyListener = () => {
    window.removeEventListener('resize', resizeHandler)
    resizeHandler = null

    sidebarElm && sidebarElm.removeEventListener('transitionend', sidebarResizeHandler)
  }

  const resize = () => {
    const chart = chartRef.value
    chart && chart.resize()
  }

  onMounted(() => {
    initListener()
  })

  onActivated(() => {
    if (!resizeHandler) {
      // avoid duplication init
      initListener()
    }

    // when keep-alive chart activated, auto resize
    resize()
  })

  onBeforeUnmount(() => {
    destroyListener()
  })

  onDeactivated(() => {
    destroyListener()
  })

  return {
    resize
  }
}
