package xin.manong.darwin.parser.script;

import java.io.Serial;

/**
 * 脚本并发操作异常
 *
 * @author frankcl
 * @date 2023-11-08 11:14:17
 */
public class ScriptConcurrentException extends Exception {

    @Serial
    private static final long serialVersionUID = -800725212208689806L;

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
