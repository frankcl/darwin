package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import xin.manong.darwin.common.model.URLRecord;

import java.util.List;
import java.util.Map;

/**
 * 调试成功结果
 *
 * @author frankcl
 * @date 2025-04-07 14:27:38
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DebugSuccess extends DebugResponse {

    /**
     * 结构化数据
     */
    @JsonProperty("field_map")
    public Map<String, Object> fieldMap;
    /**
     * 用户透传数据
     */
    @JsonProperty("custom_map")
    public Map<String, Object> customMap;
    /**
     * 抽链列表
     */
    @JsonProperty("children")
    public List<URLRecord> children;

    public DebugSuccess() {
        super(true);
    }

    public DebugSuccess(Map<String, Object> fieldMap, List<URLRecord> children,
                        Map<String, Object> customMap) {
        super(true);
        this.fieldMap = fieldMap;
        this.children = children;
        this.customMap = customMap;
    }
}
