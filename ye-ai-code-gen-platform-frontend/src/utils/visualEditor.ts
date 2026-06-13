/**
 * 选中元素信息
 */
export interface ElementInfo {
  tagName: string
  id: string
  className: string
  textContent: string
  selector: string
  pagePath?: string
}

/** 注入 iframe 的高亮样式 */
const INJECTED_STYLE = `
.ve-hover {
  outline: 2px dashed #1890ff !important;
  outline-offset: 2px !important;
  background-color: rgba(24, 144, 255, 0.06) !important;
  cursor: pointer !important;
}
.ve-selected {
  outline: 3px solid #D32F2F !important;
  outline-offset: 2px !important;
  background-color: rgba(211, 47, 47, 0.08) !important;
}
`

/** 注入 iframe 的交互脚本 */
const INJECTED_SCRIPT = `
(function() {
  if (window.__veInjected__) return;
  window.__veInjected__ = true;

  var hoveredEl = null;
  var selectedEl = null;

  function getSelector(el) {
    if (el.id) return '#' + el.id;
    var parts = [];
    var current = el;
    while (current && current !== document.body && current !== document.documentElement) {
      var tag = current.tagName.toLowerCase();
      if (current.id) {
        parts.unshift('#' + current.id);
        break;
      }
      var cls = '';
      if (typeof current.className === 'string') {
        cls = current.className.replace(/ve-(hover|selected)/g, '').trim();
      }
      if (cls) {
        parts.unshift(tag + '.' + cls.split(/\\s+/).join('.'));
      } else {
        parts.unshift(tag);
      }
      current = current.parentElement;
    }
    return parts.join(' > ');
  }

  function getElInfo(el) {
    return {
      tagName: el.tagName,
      id: el.id || '',
      className: (typeof el.className === 'string' ? el.className : '').replace(/ve-(hover|selected)/g, '').trim(),
      textContent: (el.textContent || '').trim().substring(0, 200),
      selector: getSelector(el)
    };
  }

  function notifyParent(type, payload) {
    window.parent.postMessage({ type: type, payload: payload }, '*');
  }

  document.addEventListener('mouseover', function(e) {
    var el = e.target;
    if (el === document.body || el === document.documentElement) return;
    if (el === selectedEl) return;
    if (hoveredEl && hoveredEl !== el) {
      hoveredEl.classList.remove('ve-hover');
    }
    hoveredEl = el;
    el.classList.add('ve-hover');
  }, true);

  document.addEventListener('mouseout', function(e) {
    var el = e.target;
    el.classList.remove('ve-hover');
    if (hoveredEl === el) hoveredEl = null;
  }, true);

  document.addEventListener('click', function(e) {
    e.preventDefault();
    e.stopPropagation();
    var el = e.target;
    if (el === document.body || el === document.documentElement) return;

    if (el === selectedEl) {
      // 取消选中
      el.classList.remove('ve-selected');
      selectedEl = null;
      notifyParent('VE_CLEAR', {});
    } else {
      // 取消之前的选中
      if (selectedEl) {
        selectedEl.classList.remove('ve-selected');
      }
      // 选中新元素
      el.classList.add('ve-selected');
      el.classList.remove('ve-hover');
      selectedEl = el;
      notifyParent('VE_SELECT', getElInfo(el));
    }
  }, true);

  window.addEventListener('message', function(e) {
    if (!e.data || !e.data.type) return;
    if (e.data.type === 'VE_CLEAR') {
      if (selectedEl) {
        selectedEl.classList.remove('ve-selected');
        selectedEl = null;
      }
    }
  });
})();
`

export class VisualEditor {
  private iframe: HTMLIFrameElement | null = null
  private isEditMode = false
  private onElementSelected: (elementInfo: ElementInfo) => void
  private onElementCleared: (() => void) | null = null

  constructor(options: {
    onElementSelected: (elementInfo: ElementInfo) => void
    onElementCleared?: () => void
  }) {
    this.onElementSelected = options.onElementSelected
    this.onElementCleared = options.onElementCleared || null
  }

  /** 初始化 iframe 引用 */
  init(iframe: HTMLIFrameElement) {
    this.iframe = iframe
  }

  /** 切换编辑模式，返回新的编辑状态 */
  toggleEditMode(): boolean {
    this.isEditMode = !this.isEditMode
    if (this.isEditMode) {
      this.inject()
    } else {
      this.removeInjection()
      this.onElementCleared?.()
    }
    return this.isEditMode
  }

  /** 处理来自 iframe 的 postMessage */
  handleIframeMessage(event: MessageEvent) {
    if (!event.data || typeof event.data.type !== 'string') return
    const { type, payload } = event.data

    if (type === 'VE_SELECT') {
      this.onElementSelected({
        tagName: payload.tagName || '',
        id: payload.id || '',
        className: payload.className || '',
        textContent: payload.textContent || '',
        selector: payload.selector || '',
      })
    } else if (type === 'VE_CLEAR') {
      this.onElementCleared?.()
    }
  }

  /** 清除选中元素 */
  clearSelection() {
    this.postToIframe({ type: 'VE_CLEAR' })
    this.onElementCleared?.()
  }

  /** iframe 重新加载后的处理 */
  onIframeLoad() {
    if (this.isEditMode) {
      this.inject()
    }
  }

  /** 向 iframe 发送消息 */
  private postToIframe(data: unknown) {
    if (this.iframe?.contentWindow) {
      this.iframe.contentWindow.postMessage(data, '*')
    }
  }

  /** 向 iframe 注入样式和脚本 */
  private inject() {
    if (!this.iframe) {
      console.warn('[VisualEditor] inject: iframe is null')
      return
    }
    try {
      const doc = this.iframe.contentDocument || this.iframe.contentWindow?.document
      if (!doc) {
        console.warn('[VisualEditor] inject: contentDocument is null (可能是跨域导致)')
        return
      }
      if (!doc.head) {
        console.warn('[VisualEditor] inject: doc.head is null')
        return
      }
      if (!doc.body) {
        console.warn('[VisualEditor] inject: doc.body is null, 等待 DOM 就绪后重试')
        setTimeout(() => this.inject(), 200)
        return
      }

      if (!doc.querySelector('style[data-ve]')) {
        const style = doc.createElement('style')
        style.setAttribute('data-ve', 'true')
        style.textContent = INJECTED_STYLE
        doc.head.appendChild(style)
        console.log('[VisualEditor] 样式注入成功')
      }

      if (!doc.querySelector('script[data-ve]')) {
        const script = doc.createElement('script')
        script.setAttribute('data-ve', 'true')
        script.textContent = INJECTED_SCRIPT
        doc.body.appendChild(script)
        console.log('[VisualEditor] 脚本注入成功')
      }
    } catch (e) {
      console.error('[VisualEditor] 注入失败（跨域或权限不足）:', e)
    }
  }

  /** 移除 iframe 内注入的内容和所有高亮类名 */
  private removeInjection() {
    if (!this.iframe) return
    try {
      const doc = this.iframe.contentDocument || this.iframe.contentWindow?.document
      if (!doc) return

      doc.querySelectorAll('[data-ve]').forEach((el) => el.remove())
      doc.querySelectorAll('.ve-hover, .ve-selected').forEach((el) => {
        el.classList.remove('ve-hover', 've-selected')
      })
      if (this.iframe.contentWindow) {
        ;(this.iframe.contentWindow as Window & { __veInjected__?: boolean }).__veInjected__ = false
      }
      console.log('[VisualEditor] 注入已移除')
    } catch (e) {
      console.error('[VisualEditor] 移除注入失败:', e)
    }
  }
}