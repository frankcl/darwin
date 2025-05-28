/**
 * 支持Jsoup进行DOM解析
 *   Document document = Jsoup.parse(request.text, request.url);
 *
 * 支持fastjson进行JSON解析
 *   JSONArray response = JSONArray.parseArray(request.text);
 *   JSONObject response = JSONObject.parseObject(request.text);
 *
 * 可使用ParseResponse.buildOK()方法生成成功解析响应
 *   buildOK(Map<String, Object>fieldMap, List<URLRecord> children, Map<String, Object> customMap)
 * 可使用ParseResponse.buildError()方法生成错误解析响应
 *   buildError(String message)
 * 可通过如下方法打印日志，支持info, warn和error方法(底层为slf4j实现，支持可变参数日志打印)
 *   logger.info("this is a test log: {}", message);
 *
 * 解析请求ParseRequest数据结构
 * {
 *   url: 请求URL,
 *   redirectURL: 重定向URL,
 *   text: 抓取文本,
 *   customMap: 自定义数据
 * }
 *
 * 解析响应ParseResponse数据结构
 * {
 *   message: 错误信息,
 *   fieldMap: 结构化数据,
 *   children: 抽链列表,
 *   customMap: 自定义数据
 * }
 *
 * @param request 解析请求
 * @return 解析响应
 */
ParseResponse parse(ParseRequest request) {
    /**
     * TODO 在这里编写解析逻辑
     */
    return null;
}
