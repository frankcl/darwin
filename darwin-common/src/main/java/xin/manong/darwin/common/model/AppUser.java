package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 应用用户关系信息
 *
 * @author frankcl
 * @date 2023-10-20 15:40:19
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "app_user", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUser extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(AppUser.class);

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
     * 用户ID
     */
    @TableField(value = "user_id")
    @JSONField(name = "user_id")
    @JsonProperty("user_id")
    public String userId;

    /**
     * 用户真实姓名
     */
    @TableField(value = "nick_name")
    @JSONField(name = "nick_name")
    @JsonProperty("nick_name")
    public String nickName;

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AppUser appUser)) return false;
        return Objects.equals(appUser.appId, appId) &&
                Objects.equals(appUser.userId, userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, userId);
    }

    /**
     * 检测有效性
     *
     * @return 如果有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(userId)) {
            logger.error("User id is empty");
            return false;
        }
        if (StringUtils.isEmpty(nickName)) {
            logger.error("Nick name is empty");
            return false;
        }
        if (appId == null) {
            logger.error("App id is null");
            return false;
        }
        return true;
    }
}
