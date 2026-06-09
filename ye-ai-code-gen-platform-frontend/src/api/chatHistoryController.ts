// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 DELETE /chatHistory/admin/delete/${param0} */
export async function deleteByAppIdByAdmin(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteByAppIdByAdminParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/chatHistory/admin/delete/${param0}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatHistory/admin/list/page */
export async function listChatHistoryByAdmin(
  body: API.ChatHistoryQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageChatHistory>('/chatHistory/admin/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /chatHistory/has/history */
export async function hasHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.hasHistoryParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/chatHistory/has/history', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatHistory/list/page */
export async function listChatHistoryByPage(
  body: API.ChatHistoryQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageChatHistory>('/chatHistory/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
