/**
 * 代码生成类型枚举
 */
export enum CodeGenTypeEnum {
  HTML = 'html',
  MULTI_FILE = 'multi_file',
  VUE_PROJECT = 'vue_project',
}

/**
 * 代码生成类型枚举
 * 对应后端 CodeGenTypeEnum
 */
export const CODE_GEN_TYPE_OPTIONS = [
  [CodeGenTypeEnum.HTML, { label: '原生 HTML 模式', value: 'html' }],
  [CodeGenTypeEnum.MULTI_FILE, { label: '原生多文件模式', value: 'multi_file' }],
  [CodeGenTypeEnum.VUE_PROJECT, { label: 'Vue 项目模式', value: 'vue_project' }],
] as const

/**
 * 根据 value 获取 label
 */
export const getCodeGenTypeLabel = (value: string): string => {
  return CODE_GEN_TYPE_OPTIONS.find((item) => item.value === value)?.label ?? value
}
