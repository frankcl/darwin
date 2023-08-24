package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.URLRecord;

import java.io.Serializable;
import java.util.List;

/**
 * 执行计划请求
 *
 * @author frankcl
 * @date 2023-04-24 14:22:47
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecuteRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteRequest.class);

    /**
     * 计划ID
     */
    @JsonProperty("plan_id")
    public String planId;
    /**
     * 种子URL列表
     * 一次性计划：种子列表为空，使用原先种子列表
     * 周期性计划：忽略种子列表
     */
    @JsonProperty("seed_urls")
    public List<URLRecord> seedURLs;

    /**
     * 检测请求合法性
     *
     * @return 合法返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(planId)) {
            logger.error("plan id is empty");
            return false;
        }
        return true;
    }
}
