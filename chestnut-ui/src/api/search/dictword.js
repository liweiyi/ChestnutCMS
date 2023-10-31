import request from '@/utils/request'

// 查询词库新词列表
export function getDictWords(params) {
  return request({
    url: '/search/dict',
    method: 'get',
    params: params
  })
}
// 添加词库新词
export function addDictWord(words) {
  return request({
    url: '/search/dict',
    method: 'post',
    data: words
  })
}

// 删除词库新词
export function deleteDictWords(dictWordIds) {
  return request({
    url: '/search/dict',
    method: 'delete',
    data: dictWordIds
  })
}

// 分词测试
export function wordAnalyze(data) {
  return request({
    url: '/search/dict/analyze',
    method: 'post',
    data: data
  })
}