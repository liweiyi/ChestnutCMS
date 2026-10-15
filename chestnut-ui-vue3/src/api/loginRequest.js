export function createLoginRequest(type, data) {
  return {
    url: '/login',
    headers: { isToken: false },
    method: 'post',
    params: { type },
    data
  }
}
