package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * URL状态
 *
 * @author frankcl
 * @date 2025-04-13 16:49:58
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLStatus {

    /**
     * 状态代码
     */
    @JsonProperty("code")
    public int code;

    /**
     * 状态名称
     */
    @JsonProperty("name")
    public String name;

    public URLStatus() {
    }

    public URLStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
