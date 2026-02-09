package xin.manong.darwin.runner.proxy.zdaye;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 站大爷HTTP响应
 *
 * @author frankcl
 * @date 2026-02-09 09:36:50
 */
public class ZdayeResponse<T> {

    @JSONField(name = "code")
    public int code;
    @JSONField(name = "msg")
    public String message;
    @JSONField(name = "data")
    public T data;
}
