import request from '@/utils/request'

// 视频截图
export function videoScreenshot(data) {
  return request({
    url: '/cms/video/screenshot',
    method: 'post',
    data: data
  })
}
