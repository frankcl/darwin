package xin.manong.darwin.service.request;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import xin.manong.darwin.common.Constants;

import java.io.Serializable;
import java.util.List;

/**
 * 搜索请求
 *
 * @author frankcl
 * @date 2023-04-24 11:07:03
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequest implements Serializable {

    /**
     * 页码，从1开始
     */
    @JsonProperty("page_num")
    @QueryParam("page_num")
    public Integer pageNum = Constants.DEFAULT_PAGE_NUM;
    /**
     * 分页大小，默认20
     */
    @JsonProperty("page_size")
    @QueryParam("page_size")
    public Integer pageSize = Constants.DEFAULT_PAGE_SIZE;
    /**
     * 排序方式
     */
    @JsonProperty("order_by")
    @QueryParam("order_by")
    public String orderBy;

    /**
     * 应用ID列表字符串
     */
    @JsonProperty("app_ids")
    @QueryParam("app_ids")
    public String appIds;

    public List<String> appList;
    public List<OrderByRequest> orderByRequests;

    /**
     * 构建排序条件
     *
     * @param query 查询
     */
    public void prepareOrderBy(QueryWrapper<?> query) {
        if (orderByRequests == null || orderByRequests.isEmpty()) {
            query.orderBy(true, false, "create_time");
            return;
        }
        for (OrderByRequest orderByRequest : orderByRequests) {
            query.orderBy(true, orderByRequest.asc != null && orderByRequest.asc, orderByRequest.field);
        }
    }
}
