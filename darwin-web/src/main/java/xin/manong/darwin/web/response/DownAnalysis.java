package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * 下载信息
 *
 * @author frankcl
 * @date 2025-05-09 16:45:00
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DownAnalysis {

    /**
     * 平均下载时间
     */
    @JsonProperty("avg_down_time")
    public Long avgDownTime;
    /**
     * 平均内容长度
     */
    @JsonProperty("avg_content_length")
    public Long avgContentLength;
}
