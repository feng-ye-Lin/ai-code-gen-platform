import { marked } from 'marked'
import hljs from 'highlight.js'

// 配置 marked 使用 highlight.js 进行代码高亮
marked.setOptions({
  highlight(code: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true,
})

/**
 * 将 Markdown 文本渲染为 HTML
 */
export function renderMarkdown(content: string): string {
  return marked.parse(content) as string
}
