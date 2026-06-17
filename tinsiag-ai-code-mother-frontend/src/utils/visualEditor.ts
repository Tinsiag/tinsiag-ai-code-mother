export interface ElementInfo {
  tagName: string
  id: string
  className: string
  textContent: string
  selector: string
  pagePath: string
  rect: {
    top: number
    left: number
    width: number
    height: number
  }
}

export interface VisualEditorOptions {
  onElementSelected?: (elementInfo: ElementInfo) => void
  onElementHover?: (elementInfo: ElementInfo) => void
}

type IframeMessage = {
  type?: string
  data?: {
    elementInfo?: ElementInfo
  }
}

const VISUAL_EDIT_SCRIPT_ID = 'visual-edit-script'

export const appendElementInfoToPrompt = (prompt: string, elementInfo: ElementInfo | null) => {
  if (!elementInfo) {
    return prompt
  }

  let elementContext = '\n\n选中元素信息：'
  if (elementInfo.pagePath) {
    elementContext += `\n- 页面路径: ${elementInfo.pagePath}`
  }
  elementContext += `\n- 标签: ${elementInfo.tagName.toLowerCase()}\n- 选择器: ${elementInfo.selector}`
  if (elementInfo.textContent) {
    elementContext += `\n- 当前内容: ${elementInfo.textContent.substring(0, 100)}`
  }

  return `${prompt}${elementContext}`
}

/**
 * 可视化编辑器工具类
 * 负责管理 iframe 内的可视化编辑功能
 */
export class VisualEditor {
  private iframe: HTMLIFrameElement | null = null
  private isEditMode = false
  private options: VisualEditorOptions

  constructor(options: VisualEditorOptions = {}) {
    this.options = options
  }

  init(iframe: HTMLIFrameElement) {
    this.iframe = iframe
  }

  enableEditMode() {
    if (!this.iframe) {
      return false
    }
    this.isEditMode = true
    window.setTimeout(() => {
      this.injectEditScript()
    }, 300)
    return true
  }

  disableEditMode() {
    this.isEditMode = false
    this.sendMessageToIframe({
      type: 'TOGGLE_EDIT_MODE',
      editMode: false,
    })
    this.sendMessageToIframe({
      type: 'CLEAR_ALL_EFFECTS',
    })
  }

  toggleEditMode() {
    if (this.isEditMode) {
      this.disableEditMode()
    } else {
      this.enableEditMode()
    }
    return this.isEditMode
  }

  syncState() {
    if (!this.isEditMode) {
      this.sendMessageToIframe({
        type: 'CLEAR_ALL_EFFECTS',
      })
    }
  }

  clearSelection() {
    this.sendMessageToIframe({
      type: 'CLEAR_SELECTION',
    })
  }

  onIframeLoad() {
    if (this.isEditMode) {
      window.setTimeout(() => {
        this.injectEditScript()
      }, 500)
      return
    }

    window.setTimeout(() => {
      this.syncState()
    }, 500)
  }

  handleIframeMessage(event: MessageEvent) {
    const sourceWindow = this.iframe?.contentWindow
    if (!sourceWindow || event.source !== sourceWindow) {
      return
    }
    if (!event.data || typeof event.data !== 'object') {
      return
    }

    const { type, data } = event.data as IframeMessage
    switch (type) {
      case 'ELEMENT_SELECTED':
        if (data?.elementInfo) {
          this.options.onElementSelected?.(data.elementInfo)
        }
        break
      case 'ELEMENT_HOVER':
        if (data?.elementInfo) {
          this.options.onElementHover?.(data.elementInfo)
        }
        break
    }
  }

  private sendMessageToIframe(message: Record<string, unknown>) {
    if (this.iframe?.contentWindow) {
      this.iframe.contentWindow.postMessage(message, '*')
    }
  }

  private injectEditScript() {
    if (!this.iframe) {
      return
    }

    const waitForIframeLoad = () => {
      try {
        const document = this.iframe?.contentDocument
        if (!this.iframe?.contentWindow || !document) {
          window.setTimeout(waitForIframeLoad, 100)
          return
        }

        if (document.getElementById(VISUAL_EDIT_SCRIPT_ID)) {
          this.sendMessageToIframe({
            type: 'TOGGLE_EDIT_MODE',
            editMode: true,
          })
          return
        }

        const scriptElement = document.createElement('script')
        scriptElement.id = VISUAL_EDIT_SCRIPT_ID
        scriptElement.textContent = this.generateEditScript()
        ;(document.head || document.documentElement).appendChild(scriptElement)
      } catch {
        // 同源访问失败或注入失败时保持静默，避免影响正常对话流程
      }
    }

    waitForIframeLoad()
  }

  private generateEditScript() {
    return `
      (function() {
        if (window.__VISUAL_EDITOR_INJECTED__) {
          window.postMessage({ type: 'TOGGLE_EDIT_MODE', editMode: true }, '*');
          return;
        }
        window.__VISUAL_EDITOR_INJECTED__ = true;

        var isEditMode = true;
        var currentHoverElement = null;
        var currentSelectedElement = null;
        var eventListenersAdded = false;

        function injectStyles() {
          if (document.getElementById('edit-mode-styles')) return;
          var style = document.createElement('style');
          style.id = 'edit-mode-styles';
          style.textContent = [
            '.edit-hover {',
            '  outline: 2px dashed #1890ff !important;',
            '  outline-offset: 2px !important;',
            '  cursor: crosshair !important;',
            '  transition: outline 0.2s ease !important;',
            '}',
            '.edit-selected {',
            '  outline: 3px solid #0958d9 !important;',
            '  outline-offset: 2px !important;',
            '  cursor: default !important;',
            '}',
            'body.visual-edit-mode, body.visual-edit-mode * {',
            '  cursor: crosshair !important;',
            '}',
            '#edit-tip {',
            '  position: fixed;',
            '  top: 20px;',
            '  right: 20px;',
            '  z-index: 2147483647;',
            '  padding: 12px 16px;',
            '  color: #fff;',
            '  font-size: 14px;',
            '  line-height: 1.5;',
            '  background: #1890ff;',
            '  border-radius: 6px;',
            '  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);',
            '}',
          ].join('');
          document.head.appendChild(style);
        }

        function getClassName(element) {
          if (!element.className) return '';
          if (typeof element.className === 'string') return element.className;
          return element.className.baseVal || '';
        }

        function escapeSelectorPart(value) {
          if (window.CSS && window.CSS.escape) {
            return window.CSS.escape(value);
          }
          return String(value).replace(/[^a-zA-Z0-9_-]/g, '\\\\$&');
        }

        function getVisibleClassNames(element) {
          return getClassName(element)
            .split(/\\s+/)
            .filter(function(className) {
              return className && className !== 'edit-hover' && className !== 'edit-selected';
            });
        }

        function generateSelector(element) {
          var path = [];
          var current = element;
          var depth = 0;

          while (current && current.nodeType === 1 && current !== document.body && depth < 8) {
            var selector = current.tagName.toLowerCase();
            if (current.id) {
              path.unshift(selector + '#' + escapeSelectorPart(current.id));
              break;
            }

            var classNames = getVisibleClassNames(current);
            if (classNames.length > 0) {
              selector += '.' + classNames.map(escapeSelectorPart).join('.');
            }

            var parent = current.parentElement;
            if (parent) {
              var sameTagSiblings = Array.prototype.filter.call(parent.children, function(child) {
                return child.tagName === current.tagName;
              });
              if (sameTagSiblings.length > 1) {
                selector += ':nth-of-type(' + (sameTagSiblings.indexOf(current) + 1) + ')';
              }
            }

            path.unshift(selector);
            current = current.parentElement;
            depth += 1;
          }

          return path.join(' > ');
        }

        function normalizeText(text) {
          return (text || '').replace(/\\s+/g, ' ').trim().substring(0, 100);
        }

        function getElementInfo(element) {
          var rect = element.getBoundingClientRect();
          var pagePath = window.location.pathname + window.location.search + window.location.hash;
          return {
            tagName: element.tagName,
            id: element.id || '',
            className: getVisibleClassNames(element).join(' '),
            textContent: normalizeText(element.textContent),
            selector: generateSelector(element),
            pagePath: pagePath,
            rect: {
              top: rect.top,
              left: rect.left,
              width: rect.width,
              height: rect.height
            }
          };
        }

        function isEditableTarget(target) {
          return target &&
            target.nodeType === 1 &&
            target !== document.body &&
            target !== document.documentElement &&
            target.tagName !== 'SCRIPT' &&
            target.tagName !== 'STYLE' &&
            target.id !== 'edit-tip' &&
            target.id !== 'visual-edit-script' &&
            target.id !== 'edit-mode-styles';
        }

        function clearHoverEffect() {
          if (currentHoverElement) {
            currentHoverElement.classList.remove('edit-hover');
            currentHoverElement = null;
          }
        }

        function clearSelectedEffect() {
          var selected = document.querySelectorAll('.edit-selected');
          selected.forEach(function(element) {
            element.classList.remove('edit-selected');
          });
          currentSelectedElement = null;
        }

        function showEditTip() {
          if (document.getElementById('edit-tip')) return;
          var tip = document.createElement('div');
          tip.id = 'edit-tip';
          tip.innerHTML = '编辑模式已开启<br/>悬浮查看元素，点击选中元素';
          document.body.appendChild(tip);
          window.setTimeout(function() {
            if (tip.parentNode) {
              tip.remove();
            }
          }, 3000);
        }

        function mouseoverHandler(event) {
          if (!isEditMode) return;
          var target = event.target;
          if (!isEditableTarget(target) || target === currentHoverElement || target === currentSelectedElement) {
            return;
          }

          clearHoverEffect();
          target.classList.add('edit-hover');
          currentHoverElement = target;
          try {
            window.parent.postMessage({
              type: 'ELEMENT_HOVER',
              data: { elementInfo: getElementInfo(target) }
            }, '*');
          } catch (error) {}
        }

        function mouseoutHandler(event) {
          if (!isEditMode) return;
          var target = event.target;
          if (target === currentHoverElement && (!event.relatedTarget || !target.contains(event.relatedTarget))) {
            clearHoverEffect();
          }
        }

        function clickHandler(event) {
          if (!isEditMode) return;

          event.preventDefault();
          event.stopPropagation();
          if (event.stopImmediatePropagation) {
            event.stopImmediatePropagation();
          }

          var target = event.target;
          if (!isEditableTarget(target)) return;

          clearSelectedEffect();
          clearHoverEffect();
          target.classList.add('edit-selected');
          currentSelectedElement = target;

          try {
            window.parent.postMessage({
              type: 'ELEMENT_SELECTED',
              data: { elementInfo: getElementInfo(target) }
            }, '*');
          } catch (error) {}
        }

        function addEventListeners() {
          if (eventListenersAdded || !document.body) return;
          document.body.addEventListener('mouseover', mouseoverHandler, true);
          document.body.addEventListener('mouseout', mouseoutHandler, true);
          document.body.addEventListener('click', clickHandler, true);
          eventListenersAdded = true;
        }

        function enableEditMode() {
          isEditMode = true;
          injectStyles();
          addEventListeners();
          document.body.classList.add('visual-edit-mode');
          showEditTip();
        }

        function disableEditMode() {
          isEditMode = false;
          clearHoverEffect();
          clearSelectedEffect();
          document.body.classList.remove('visual-edit-mode');
          var tip = document.getElementById('edit-tip');
          if (tip) tip.remove();
        }

        window.addEventListener('message', function(event) {
          var data = event.data || {};
          switch (data.type) {
            case 'TOGGLE_EDIT_MODE':
              if (data.editMode) {
                enableEditMode();
              } else {
                disableEditMode();
              }
              break;
            case 'CLEAR_SELECTION':
              clearSelectedEffect();
              break;
            case 'CLEAR_ALL_EFFECTS':
              disableEditMode();
              break;
          }
        });

        enableEditMode();
      })();
    `
  }
}
