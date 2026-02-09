package xin.manong.darwin.runner.proxy.zdaye;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 站大爷代理
 *
 * @author frankcl
 * @date 2026-02-09 14:05:05
 */
public class ZdayeProxy {

    @JSONField(name = "ip")
    public String ip;
    @JSONField(name = "port")
    public Integer port;
    @JSONField(name = "adr")
    public String addr;
    @JSONField(name = "timeout")
    public Integer timeout;
    @JSONField(name = "cometime")
    public Integer cometime;
}
