/**
 * 解析文本
 *
 * 支持url-parse进行URL解析
 *   const parseURL = require('url-parse')
 *   const URL = parseURL('https://darwin.manong.xin', true)
 *
 * 支持xmldom进行HTML解析
 *   const { DOMParser } = require('@xmldom/xmldom')
 *   const parser = new DOMParser()
 *   request.text = normalizeHTML(request.text)
 *   const doc = parser.parseFromString(request.text, 'text/xml')
 *
 * 支持xpath进行HTML数据抽取
 *   const xpath = require('xpath')
 *   const nodes = xpath.select('//div', doc)
 *
 * 可使用buildChild(url:String, headers:Map?, customMap:Map?)方法构建子链接
 * 可使用successResponse(fieldMap:Map, children:Array?, customMap:Map?)方法生成成功响应
 * 可使用errorResponse(msg:String)方法生成错误响应
 * 可使用normalizeHTML(html:String)对HTML进行正规化，符合XML规范
 * 可使用console.log()进行调试日志打印
 *
 * URL数据结构
 * {
 *   url: URL链接,
 *   headers: HTTP请求头, 默认为空
 *   httpRequest: HTTP请求方式, 支持POST和GET, 默认GET,
 *   postMediaType: POST数据类型, 支持JSON和FORM, 默认JSON,
 *   requestBody: POST请求体,
 *   allowRepeat: 是否允许重复抓取, 布尔值, 默认false,
 *   allowDispatch: 是否允许分发, 布尔值, 默认true,
 *   fieldMap: 结构化数据
 *   customMap: 自定义数据
 * }
 *
 * 解析请求Request数据结构
 * {
 *   url: 请求URL,
 *   text: 抓取文本,
 *   redirectURL: 重定向URL,
 *   customMap:自定义数据
 * }
 *
 * 解析响应Response数据结构
 * {
 *   fieldMap: 结构化字段,
 *   children: 抽链结果,
 *   customMap: 自定义数据
 * }
 *
 * @param Request 解析请求
 * @return Response 解析响应
 */
function parse(request) {
  /**
   * TODO 在这里编写解析逻辑
   */
  return undefined;
}