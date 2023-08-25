package xin.manong.darwin.parser.script;

import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseResponse;

/**
 * 脚本接口
 *
 * @author frankcl
 * @date 2023-03-16 19:55:36
 */
public abstract class Script {

    protected String key;

    public Script(String key) {
        this.key = key;
    }

    /**
     * 执行解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    public abstract ParseResponse execute(ParseRequest request);

    /**
     * 关闭脚本，释放资源
     */
    public abstract void close();

    /**
     * 获取key
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置key
     *
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }
}
