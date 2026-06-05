/**
 * 代码生成类型枚举
 * 对应后端 CodeGenTypeEnum
 */
export const CODE_GEN_TYPE_OPTIONS = [
  { label: '原生 HTML 模式', value: 'html' },
  { label: '原生多文件模式', value: 'multi_file' },
] as const

/**
 * 根据 value 获取 label
 */
export const getCodeGenTypeLabel = (value: string): string => {
  return CODE_GEN_TYPE_OPTIONS.find((item) => item.value === value)?.label ?? value
}
