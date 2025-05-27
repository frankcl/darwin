/**
 * 正规化HTML
 *
 * @param html HTML
 * @returns 正规化HTML
 */
const normalizeHTML = html => {
    const JavaScript = Java.type('xin.manong.darwin.parser.script.js.JavaScript')
    return JavaScript.normalizeHTML(html)
}

/**
 * 创建子链接
 *
 * @param url 链接URL
 * @param headers HTTP头信息
 * @param customMap 自定义数据
 * @return 子链接
 */
const buildChild = (url, headers = undefined, customMap = undefined) => {
    const child = { url: url }
    if (headers) child.headers = headers
    if (customMap) child.customMap = customMap
    return child
}

/**
 * 构建失败解析响应
 *
 * @param message 错误信息
 * @return 失败解析响应
 */
const errorResponse = message => {
    return {
        status: false,
        message: message
    }
}

/**
 * 构建成功解析响应
 *
 * @param fieldMap 结构化数据
 * @param children 抽链列表
 * @param customMap 用户自定义数据
 * @return 成功解析响应
 */
function successResponse(fieldMap, children = [], customMap = {}) {
    return {
        status: true,
        fieldMap: fieldMap,
        children: children,
        customMap: customMap
    }
}