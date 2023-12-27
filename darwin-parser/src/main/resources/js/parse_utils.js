/**
 * 创建抽链URL
 *
 * @param url 抽链URL
 * @param category 链接分类 内容页0，列表页1，图片视频资源2，直播流3
 * @param headers 链接抓取HTTP header信息
 * @param userDefinedMap 用户自定义数据
 * @return 成功返回抽链URL，否则抛出异常
 */
function createChildURL(url, category, headers, userDefinedMap) {
    if (category === undefined || category === null || category < 0 || category > 3) {
        throw '链接类型不符合预期[' + category + ']';
    }
    var childURL = {};
    childURL.url = url;
    childURL.category = category;
    if (typeof headers != 'undefined' && headers !== null) childURL.headers = headers;
    if (typeof userDefinedMap != 'undefined' && userDefinedMap !== null) childURL.userDefinedMap = userDefinedMap;
    return childURL;
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
 * @param childURLs 抽链列表
 * @param userDefinedMap 用户自定义数据
 * @return 成功解析响应
 */
function buildOK(fieldMap, childURLs, userDefinedMap) {
    var response = {};
    response.status = true;
    response.fieldMap = fieldMap;
    response.userDefinedMap = userDefinedMap;
    response.childURLs = childURLs;
    return response;
}