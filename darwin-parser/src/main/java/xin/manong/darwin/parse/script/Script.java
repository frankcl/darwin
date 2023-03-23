package xin.manong.darwin.parse.script;

import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;

/**
 * 脚本接口
 *
 * @author frankcl
 * @date 2023-03-16 19:55:36
 */
public abstract class Script {

    protected Long id;
    protected String scriptMD5;

    public Script() {
    }

    public Script(Long id, String scriptMD5) {
        this.id = id;
        this.scriptMD5 = scriptMD5;
    }

    /**
     * 执行解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    public abstract ParseResponse execute(ParseRequest request);

    /**
     * 关闭脚本
     */
    public abstract void close();

    /**
     * 获取ID
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取脚本MD5签名
     *
     * @return 脚本MD5签名
     */
    public String getScriptMD5() {
        return scriptMD5;
    }
}
