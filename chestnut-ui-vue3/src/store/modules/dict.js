const useDictStore = defineStore(
  'dict',
  {
    state: () => ({
      dict: new Map()
    }),
    actions: {
      // 获取字典
      getDict(_key) {
        if (_key == null && _key == "") {
          return null
        }
        return this.dict.get(_key)
      },
      // 设置字典
      setDict(_key, value) {
        if (_key !== null && _key !== "") {
          this.dict.set(_key, value)
        }
      },
      // 删除字典
      removeDict(_key) {
        this.dict.delete(_key)
      },
      // 清空字典
      cleanDict() {
        this.dict = new Map()
      },
      // 初始字典
      initDict() {
      }
    }
  })

export default useDictStore
