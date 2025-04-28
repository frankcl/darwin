package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xin.manong.darwin.common.model.handler.JSONListDashboardValueTypeHandler;
import xin.manong.darwin.common.model.json.MapDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页大盘统计数据
 *
 * @author frankcl
 * @date 2025-04-23 15:44:39
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "dashboard", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dashboard extends BaseModel {

    /**
     * 代理ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Integer id;

    /**
     * 小时
     */
    @TableField(value = "hour")
    @JSONField(name = "hour")
    @JsonProperty("hour")
    public String hour;

    /**
     * 统计类型
     */
    @TableField(value = "category")
    @JSONField(name = "category")
    @JsonProperty("category")
    public Integer category;

    /**
     * 统计结果
     */
    @TableField(value = "`values`", typeHandler = JSONListDashboardValueTypeHandler.class)
    @JSONField(name = "values", deserializeUsing = MapDeserializer.class)
    @JsonProperty("values")
    public List<DashboardValue<Integer>> values = new ArrayList<>();

    public Dashboard() {}
    public Dashboard(String hour, Integer category,
                     List<DashboardValue<Integer>> values) {
        this.hour = hour;
        this.category = category;
        this.values = values;
    }
}
