import { viteMockServe } from 'vite-plugin-mock'

export default function createMockPlugin(env) {
    return viteMockServe({
    	mockPath: 'mock/', // 设置模拟数据的存储文件夹
      supportTs: false, // 禁用ts文件模块支持
      watchFiles: false, // 禁用文件监视
      logger: false, // 禁用控制台日志
      localEnabled: true, //设置是否启用本地mock文件
      prodEnabled: false //设置打包是否启用 mock 功能
    })
}
