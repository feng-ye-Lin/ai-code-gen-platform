import axios from 'axios'
import { message } from 'ant-design-vue'
import JSONbig from 'json-bigint'

// 大整数统一按字符串保存，避免超过 Number.MAX_SAFE_INTEGER 的 id 精度丢失
const JSONBigString = JSONbig({ storeAsString: true, useNativeBigInt: false })

// 创建 Axios 实例
const myAxios = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 60000,
  withCredentials: true,
  transformResponse: [
    (data) => {
      if (typeof data === 'string') {
        try {
          return JSONBigString.parse(data)
        } catch {
          // 非 JSON 响应（例如纯文本/空响应）兜底：原样返回
          return data
        }
      }
      return data
    },
  ],
})

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // Do something before request is sent
    return config
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 未登录
    if (data && data.code === 40100) {
      if (
        !String(response.request?.responseURL || '').includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    return Promise.reject(error)
  },
)

export default myAxios
