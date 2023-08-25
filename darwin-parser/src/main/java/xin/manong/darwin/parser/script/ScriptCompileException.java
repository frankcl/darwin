package xin.manong.darwin.parser.script;

/**
 * 脚本编译异常
 *
 * @author frankcl
 * @date 2023-08-25 11:14:17
 */
public class ScriptCompileException extends Exception {

    public ScriptCompileException() {
        super();
    }

    public ScriptCompileException(String message) {
        super(message);
    }

    public ScriptCompileException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
