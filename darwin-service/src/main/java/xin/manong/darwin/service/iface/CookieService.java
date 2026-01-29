package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.URLRecord;

import java.util.Map;

/**
 * Cookie服务
 *
 * @author frankcl
 * @date 2026-01-29 09:38:39
 */
public interface CookieService {

    /**
     * 根据数据获取Cookie
     *
     * @param record 抓取数据
     * @return Cookie
     */
    String getCookie(URLRecord record);

    /**
     * 根据key获取Cookie
     *
     * @param key Cookie key
     * @return Cookie
     */
    String getCookie(String key);

    /**
     * 设置Cookie
     *
     * @param key Cookie key
     * @param cookie Cookie
     */
    void setCookie(String key, String cookie);

    /**
     * 获取Cookie设置
     *
     * @return Cookie设置
     */
    Map<String, String> cookieMap();

    /**
     * 设置Cookie
     *
     * @param cookieMap cookie设置
     */
    void cookieMap(Map<String, String> cookieMap);
}
