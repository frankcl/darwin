package xin.manong.darwin.parser.script;

import java.io.Serial;

/**
 * 脚本编译异常
 *
 * @author frankcl
 * @date 2023-08-25 11:14:17
 */
public class CompileException extends Exception {

    @Serial
    private static final long serialVersionUID = 1329383290367096360L;

    public CompileException() {
        super();
    }

    public CompileException(String message) {
        super(message);
    }

    public CompileException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
