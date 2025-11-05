package xin.manong.darwin.client;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端配置
 *
 * @author frankcl
 * @date 2025-11-05 16:49:54
 */
@Data
public class DarwinClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(DarwinClientConfig.class);

    public String serverURL;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(serverURL)) {
            logger.error("Server URL is not config");
            return false;
        }
        if (serverURL.endsWith("/")) serverURL = serverURL.substring(0, serverURL.length() - 1);
        return true;
    }
}
