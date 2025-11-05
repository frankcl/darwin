package xin.manong.darwin.common.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 计划执行请求
 *
 * @author frankcl
 * @date 2025-10-16 14:30:49
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanExecuteRequest extends AuthenticateRequest {

    @JsonProperty("plan_id")
    public String planId;

    @JsonProperty("seeds")
    public List<SeedRequest> seeds;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        super.check();
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("计划ID为空");
        if (seeds == null || seeds.isEmpty()) throw new BadRequestException("种子列表为空");
        for (SeedRequest seedRequest : seeds) {
            seedRequest.planId = planId;
            seedRequest.check();
        }
    }
}
