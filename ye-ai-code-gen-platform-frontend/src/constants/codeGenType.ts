/**
 * 代码生成类型枚举
 */
export enum CodeGenTypeEnum {
  HTML = 'html',
  MULTI_FILE = 'multi_file',
  VUE_PROJECT = 'vue_project',
}

/**
 * 代码生成类型选项（供 antd Select 使用）
 * 对应后端 CodeGenTypeEnum
 */
export const CODE_GEN_TYPE_OPTIONS = [
  { label: '原生 HTML 模式', value: 'html' },
  { label: '原生多文件模式', value: 'multi_file' },
  { label: 'Vue 项目模式', value: 'vue_project' },
] as const

/**
 * 根据 value 获取 label
 */
export const getCodeGenTypeLabel = (value: string): string => {
  return CODE_GEN_TYPE_OPTIONS.find((item) => item.value === value)?.label ?? value
}
