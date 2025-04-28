/**
 * 创建抽链URL
 *
 * @param url 抽链URL
 * @param category 链接分类 内容页0，列表页1，图片视频资源2，直播流3
 * @param headers 链接抓取HTTP header信息
 * @param customMap 用户自定义数据
 * @return 成功返回抽链URL，否则抛出异常
 */
function buildChild(url, category, headers, customMap) {
    if (category === undefined || category === null || category < 0 || category > 3) {
        throw '链接类型不符合预期:' + category;
    }
    var child = { url: url, category: category };
    if (typeof headers !== 'undefined' && headers !== null) child.headers = headers;
    if (typeof customMap !== 'undefined' && customMap !== null) child.customMap = customMap;
    return child;
}

/**
 * 构建失败解析响应
 *
 * @param message 错误信息
 * @return 失败解析响应
 */
function buildError(message) {
    var response = {};
    response.status = false;
    response.message = message;
    return response;
}

/**
 * 构建成功解析响应
 *
 * @param fieldMap 结构化数据
 * @param children 抽链列表
 * @param customMap 用户自定义数据
 * @return 成功解析响应
 */
function buildOK(fieldMap, children, customMap) {
    var response = {};
    response.status = true;
    response.fieldMap = fieldMap;
    response.customMap = customMap;
    if (children) response.children = children;
    return response;
}