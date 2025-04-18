package xin.manong.darwin.queue.multi;

import java.io.Serial;

/**
 * 多级队列操作异常
 *
 * @author frankcl
 * @date 2023-03-07 17:06:35
 */
public class MultiQueueException extends Exception {

    @Serial
    private static final long serialVersionUID = 807954019898367377L;

    public MultiQueueException(String message) {
        super(message);
    }

    public MultiQueueException(Throwable t) {
        super(t);
    }

    public MultiQueueException(String message, Throwable t) {
        super(message, t);
    }
}
