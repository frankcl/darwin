package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.io.Serial;

/**
 * 调试错误信息
 *
 * @author frankcl
 * @date 2025-04-07 14:25:52
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DebugError extends DebugResponse {

    @Serial
    private static final long serialVersionUID = -8601831605209873672L;
    /**
     * 错误信息
     */
    @JsonProperty("message")
    public String message;
    /**
     * 异常堆栈
     */
    @JsonProperty("stack_trace")
    public String stackTrace;

    public DebugError(String message, String stackTrace) {
        super(false);
        this.message = message;
        this.stackTrace = stackTrace;
    }
}
