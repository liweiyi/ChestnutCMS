// A cancelled WASM task cannot be interrupted from inside the codec. Terminating
// its worker also releases the codec heap and prevents late results being used.
export class ImageOptimizerClient {
  constructor(onStage = () => {}) {
    this.onStage = onStage
    this.sequence = 0
    this.pending = new Map()
  }

  ensureWorker() {
    if (this.worker) return
    const worker = new Worker(new URL('./worker.js', import.meta.url), { type: 'module' })
    this.worker = worker
    worker.onmessage = ({ data }) => {
      if (this.worker !== worker) return
      const pending = this.pending.get(data.id)
      if (!pending) return
      if (data.type === 'stage') {
        this.onStage(data.stage, data.progress)
        return
      }
      this.pending.delete(data.id)
      if (data.type === 'error') {
        pending.reject(Object.assign(new Error(data.message), { code: data.code }))
      } else {
        pending.resolve(data)
      }
    }
    worker.onerror = (event) => {
      if (this.worker !== worker) return
      const error = Object.assign(new Error(event.message || 'Image worker failed'), { code: 'PROCESS_FAILED' })
      this.cancel(error)
    }
  }

  run(type, source, format, options) {
    this.ensureWorker()
    const id = ++this.sequence
    const buffer = source.slice(0)
    return new Promise((resolve, reject) => {
      this.pending.set(id, { resolve, reject })
      try {
        this.worker.postMessage({ id, type, buffer, format, options }, [buffer])
      } catch (error) {
        this.pending.delete(id)
        reject(error)
      }
    })
  }

  cancel(error = Object.assign(new Error('Cancelled'), { name: 'AbortError' })) {
    this.worker?.terminate()
    this.worker = null
    for (const pending of this.pending.values()) pending.reject(error)
    this.pending.clear()
  }
}
