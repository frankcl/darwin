package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 脚本调试响应
 *
 * @author frankcl
 * @date 2024-01-05 14:40:04
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DebugResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -6474593265113659642L;
    /**
     * 是否成功
     */
    @JsonProperty("success")
    public boolean success;
    /**
     * 调试日志
     */
    @JsonProperty("debug_log")
    public String debugLog;

    public DebugResponse(boolean success) {
        this.success = success;
    }
}
