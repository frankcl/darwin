package xin.manong.darwin.common.model;

/**
 * 抓取URL分类
 * 1. 文本类型：HTML、JSON或文本
 * 2. 资源类型：图片、视频或PDF文档等
 * 3. 流类型：流媒体，如M3U8链接地址等
 *
 * @author frankcl
 * @date 2023-03-06 14:45:09
 */
public enum URLCategory {

    TEXT, RESOURCE, STREAM
}
