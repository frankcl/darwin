package xin.manong.darwin.parser.script;

/**
 * 脚本并发操作异常
 *
 * @author frankcl
 * @date 2023-11-08 11:14:17
 */
public class ScriptConcurrentException extends Exception {

    public ScriptConcurrentException() {
        super();
    }

    public ScriptConcurrentException(String message) {
        super(message);
    }

    public ScriptConcurrentException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
