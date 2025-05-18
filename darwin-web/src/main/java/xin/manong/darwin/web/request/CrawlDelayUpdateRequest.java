package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.io.Serializable;

/**
 * 默认抓取间隔更新请求
 *
 * @author frankcl
 * @date 2025-04-01 10:52:10
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrawlDelayUpdateRequest implements Serializable {

    /**
     * 默认抓取间隔
     */
    @JsonProperty("default_crawl_delay")
    public Long defaultCrawlDelay;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (defaultCrawlDelay == null || defaultCrawlDelay <= 0) throw new BadRequestException("默认抓取间隔必须大于0");
    }
}
