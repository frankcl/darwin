package xin.manong.darwin.common.parser;

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
    public List<LinkURL> followLinks;
}
