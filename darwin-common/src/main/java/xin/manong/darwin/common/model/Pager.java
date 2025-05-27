package xin.manong.darwin.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页对象
 *
 * @author frankcl
 * @date 2022-09-21 11:26:50
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pager<T> implements Serializable {

    /**
     * 页码：从1开始
     */
    @JsonProperty("page_num")
    public Long pageNum;
    /**
     * 分页数量
     */
    @JsonProperty("page_size")
    public Long pageSize;
    /**
     * 总数
     */
    @JsonProperty("total")
    public Long total;
    /**
     * 数据列表
     */
    @JsonProperty("records")
    public List<T> records;

    /**
     * 创建空分页
     *
     * @param pageNum 页码
     * @param pageSize 分页数量
     * @return 空分页
     * @param <T> 数据类型
     */
    public static <T> Pager<T> empty(long pageNum, long pageSize) {
        Pager<T> pager = new Pager<>();
        pager.pageNum = pageNum > 0 ? pageNum : 1L;
        pager.pageSize = pageSize > 0 ? pageSize : 10L;
        pager.total = 0L;
        pager.records = new ArrayList<>();
        return pager;
    }
}
