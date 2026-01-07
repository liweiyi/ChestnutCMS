// 使用 Vite 的 import.meta.glob 替代 Webpack 的 require.context
const modules = import.meta.glob('../../../assets/icons/svg/*.svg')

// 从路径中提取文件名
const svgIcons = Object.keys(modules).map(path => {
  const match = path.match(/\/([^/]+)\.svg$/)
  return match ? match[1] : ''
}).filter(name => name)

export default svgIcons
