package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

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
    /**
     * 标准输出
     */
    @JsonProperty("stdout")
    public String stdout;
    /**
     * 标准出错
     */
    @JsonProperty("stderr")
    public String stderr;

    public DebugResponse(boolean success) {
        this.success = success;
    }
}
