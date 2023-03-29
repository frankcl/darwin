package xin.manong.darwin.common.parser;

import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.model.URLRecord;

import java.util.List;
import java.util.Map;

/**
 * 解析响应
 *
 * @author frankcl
 * @date 2023-03-16 15:15:22
 */
public class ParseResponse {

    /**
     * 解析响应构建器
     */
    public static class Builder {

        private ParseResponse template;

        public Builder() {
            template = new ParseResponse();
        }

        public Builder status(boolean status) {
            template.status = status;
            return this;
        }

        public Builder message(String message) {
            template.message = message;
            return this;
        }

        public Builder followLinks(List<URLRecord> followLinks) {
            template.followLinks = followLinks;
            return this;
        }

        public Builder structureMap(Map<String, Object> structureMap) {
            template.structureMap = structureMap;
            return this;
        }

        public Builder userDefinedMap(Map<String, Object> userDefinedMap) {
            template.userDefinedMap = userDefinedMap;
            return this;
        }

        public ParseResponse build() {
            ParseResponse response = new ParseResponse();
            response.status = template.status;
            response.message = template.message;
            response.followLinks = template.followLinks;
            response.structureMap = template.structureMap;
            response.userDefinedMap = template.userDefinedMap;
            return response;
        }
    }

    /**
     * 解析状态
     */
    public boolean status;
    /**
     * 错误信息
     */
    public String message;
    /**
     * 结构化数据字段
     */
    public Map<String, Object> structureMap;
    /**
     * 用户透传数据
     */
    public Map<String, Object> userDefinedMap;
    /**
     * 抽链结果
     */
    public List<URLRecord> followLinks;

    /**
     * 构建错误响应
     *
     * @param message 错误信息
     * @return 错误响应
     */
    public static ParseResponse buildErrorResponse(String message) {
        return new Builder().status(false).message(StringUtils.isEmpty(message) ? "" : message).build();
    }

    /**
     * 构建抽链响应
     *
     * @param followLinks 抽链列表
     * @return 抽链响应
     */
    public static ParseResponse buildFollowLinkResponse(List<URLRecord> followLinks) {
        return new Builder().status(true).followLinks(followLinks).build();
    }

    /**
     * 构建结构化响应
     *
     * @param structureMap 结构化信息
     * @param userDefinedMap 用户透传信息
     * @return 结构化响应
     */
    public static ParseResponse buildStructureResponse(Map<String, Object> structureMap,
                                                       Map<String, Object> userDefinedMap) {
        return new Builder().status(true).structureMap(structureMap).userDefinedMap(userDefinedMap).build();
    }
}
