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

/**
 * 应用秘钥
 *
 * @author frankcl
 * @date 2025-10-16 14:52:04
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "app_secret", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppSecret extends BaseModel {

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Integer id;

    /**
     * 应用ID
     */
    @TableField(value = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public Integer appId;

    /**
     * 名称
     */
    @TableField(value = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * Access key
     */
    @TableField(value = "access_key")
    @JSONField(name = "access_key")
    @JsonProperty("access_key")
    public String accessKey;

    /**
     * Secret key
     */
    @TableField(value = "secret_key")
    @JSONField(name = "secret_key")
    @JsonProperty("secret_key")
    public String secretKey;
}
