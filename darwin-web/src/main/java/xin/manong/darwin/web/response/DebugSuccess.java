package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import xin.manong.darwin.common.model.URLRecord;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = -4982638994956229570L;
    /**
     * 结构化数据
     */
    @JsonProperty("field_map")
    public Map<String, Object> fieldMap;
    /**
     * 用户透传数据
     */
    @JsonProperty("user_defined_map")
    public Map<String, Object> userDefinedMap;
    /**
     * 抽链列表
     */
    @JsonProperty("child_urls")
    public List<URLRecord> childURLs;

    public DebugSuccess(Map<String, Object> fieldMap, List<URLRecord> childURLs,
                        Map<String, Object> userDefinedMap) {
        super(true);
        this.fieldMap = fieldMap;
        this.childURLs = childURLs;
        this.userDefinedMap = userDefinedMap;
    }
}
