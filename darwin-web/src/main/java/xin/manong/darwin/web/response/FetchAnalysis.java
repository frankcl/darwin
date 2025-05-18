package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * 抓取分析
 *
 * @author frankcl
 * @date 2025-05-09 16:45:00
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchAnalysis {

    /**
     * 抓取总量
     */
    @JsonProperty("fetch_count")
    public Long fetchCount;
    /**
     * 抓取成功总数
     */
    @JsonProperty("fetch_success_count")
    public Long fetchSuccessCount;
    /**
     * 抓取成功率
     */
    @JsonProperty("fetch_success_ratio")
    public Double fetchSuccessRatio;
    /**
     * 抓取总量对比
     */
    @JsonProperty("proportion")
    public Double proportion;
}
