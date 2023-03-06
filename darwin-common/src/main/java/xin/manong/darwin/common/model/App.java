package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * 应用信息
 *
 * @author frankcl
 * @date 2023-03-06 15:40:19
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class App implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    /**
     * 应用ID
     */
    @JSONField(name = "id")
    @JsonProperty("id")
    public Integer id;

    /**
     * 应用名称
     */
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 检测应用有效性
     * 1. 应用ID和名称不能为空
     *
     * @return 如果有效返回true，否则返回false
     */
    public boolean check() {
        if (id == null) {
            logger.error("app id is null");
            return false;
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("app name is empty");
            return false;
        }
        return true;
    }
}
