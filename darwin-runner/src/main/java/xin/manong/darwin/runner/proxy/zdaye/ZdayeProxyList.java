package xin.manong.darwin.runner.proxy.zdaye;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * 代理列表
 *
 * @author frankcl
 * @date 2026-02-09 14:07:00
 */
public class ZdayeProxyList {

    @JSONField(name = "count")
    public Integer count;
    @JSONField(name = "proxy_list")
    public List<ZdayeProxy> proxyList;
}
