package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.io.Serializable;

/**
 * 缺省最大并发数更新请求
 *
 * @author frankcl
 * @date 2025-04-01 10:52:10
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConcurrencyUpdateRequest implements Serializable {

    /**
     * 缺省并发数
     */
    @JsonProperty("default_concurrency")
    public Integer defaultConcurrency;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (defaultConcurrency == null || defaultConcurrency <= 0) throw new BadRequestException("缺省并发数必须大于0");
    }
}
