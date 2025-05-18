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
import xin.manong.darwin.common.model.handler.JSONDashboardValueListHandler;
import xin.manong.darwin.common.model.json.MapDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 * 趋势数据
 * 统计单位：小时
 *
 * @author frankcl
 * @date 2025-04-23 15:44:39
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "trend", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Trend extends BaseModel {

    /**
     * 代理ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Integer id;

    /**
     * 统计维度
     */
    @TableField(value = "`key`")
    @JSONField(name = "key")
    @JsonProperty("key")
    public String key;

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
    @TableField(value = "`values`", typeHandler = JSONDashboardValueListHandler.class)
    @JSONField(name = "values", deserializeUsing = MapDeserializer.class)
    @JsonProperty("values")
    public List<TrendValue<?>> values = new ArrayList<>();

    public Trend() {}
    public Trend(String key, Integer category,
                 List<TrendValue<?>> values) {
        this.key = key;
        this.category = category;
        this.values = values;
    }
}
