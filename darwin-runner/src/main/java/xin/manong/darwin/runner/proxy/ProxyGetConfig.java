package xin.manong.darwin.runner.proxy;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代理获取配置
 *
 * @author frankcl
 * @date 2026-02-06 14:38:57
 */
@Data
public class ProxyGetConfig {

    private static final Logger logger = LoggerFactory.getLogger(ProxyGetConfig.class);

    private static final int DEFAULT_BATCH_SIZE = 1;
    private static final long DEFAULT_EXPIRED_INTERVAL_MS = 120000L;

    public int batchSize = DEFAULT_BATCH_SIZE;
    public long expiredIntervalMs = DEFAULT_EXPIRED_INTERVAL_MS;
    public String baseURL;
    public String appId;
    public String appSecret;
    public String username;
    public String password;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (batchSize <= 0) batchSize = DEFAULT_BATCH_SIZE;
        if (expiredIntervalMs <= 0) expiredIntervalMs = DEFAULT_EXPIRED_INTERVAL_MS;
        if (StringUtils.isEmpty(baseURL)) {
            logger.error("baseURL is empty");
            return false;
        }
        if (StringUtils.isEmpty(appId)) {
            logger.error("appId is empty");
            return false;
        }
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("appSecret is empty");
            return false;
        }
        if (StringUtils.isEmpty(username)) {
            logger.error("username is empty");
            return false;
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("password is empty");
            return false;
        }
        if (baseURL.endsWith("/")) baseURL = baseURL.substring(0, baseURL.length() - 1);
        return true;
    }
}
