package xin.manong.darwin.parser.script;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 脚本接口
 *
 * @author frankcl
 * @date 2023-03-16 19:55:36
 */
public abstract class Script implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Script.class);

    @Setter
    @Getter
    protected String key;
    /**
     * 引用计数
     */
    protected AtomicInteger referenceCount;
    /**
     * 读写锁
     */
    protected ReentrantReadWriteLock readWriteLock;

    public Script(String key) {
        this.key = key;
        referenceCount = new AtomicInteger(0);
        readWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * 执行解析
     *
     * @param request 解析请求
     * @return 解析响应
     * @throws Exception 异常
     */
    public abstract ParseResponse doExecute(ParseRequest request) throws Exception;

    /**
     * 关闭脚本，释放资源
     *
     * @throws IOException I/O异常
     */
    public abstract void doClose() throws IOException;

    /**
     * 获取标准输出
     *
     * @return 标准输出
     */
    public String getStdout() {
        return null;
    }

    /**
     * 获取标准错误
     *
     * @return 标准错误
     */
    public String getStderr() {
        return null;
    }

    /**
     * 执行解析
     *
     * @param request 解析请求
     * @return 解析响应
     * @throws ConcurrentException 并发异常
     */
    public ParseResponse execute(ParseRequest request) throws ConcurrentException {
        try {
            readWriteLock.readLock().lock();
            increaseReference();
            return doExecute(request);
        } catch (ConcurrentException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            ParseResponse response = ParseResponse.buildError(String.format("执行脚本异常:%s", e.getMessage()));
            response.stdout = getStdout();
            response.stderr = getStderr();
            return response;
        } finally {
            decreaseReference();
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * 关闭脚本，释放资源
     */
    @Override
    public void close() {
        try {
            readWriteLock.writeLock().lock();
            doClose();
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * 增加引用计数
     */
    public void increaseReference() {
        referenceCount.incrementAndGet();
    }

    /**
     * 减少引用计数
     */
    public void decreaseReference() {
        referenceCount.decrementAndGet();
    }

    /**
     * 当前引用计数
     *
     * @return 当前引用计数
     */
    public int currentReferenceCount() {
        return referenceCount.get();
    }
}
