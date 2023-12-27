package xin.manong.darwin.parser.script;

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
public abstract class Script {

    private static final Logger logger = LoggerFactory.getLogger(Script.class);

    protected static final String METHOD_PARSE = "parse";

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
     * @throws Exception
     */
    public abstract ParseResponse doExecute(ParseRequest request) throws Exception;

    /**
     * 关闭脚本，释放资源
     *
     * @throws IOException
     */
    public abstract void doClose() throws IOException;

    /**
     * 执行解析
     *
     * @param request 解析请求
     * @return 解析响应
     * @throws ScriptConcurrentException
     */
    public ParseResponse execute(ParseRequest request) throws ScriptConcurrentException {
        try {
            readWriteLock.readLock().lock();
            increaseReference();
            return doExecute(request);
        } catch (ScriptConcurrentException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ParseResponse.buildError(String.format("执行脚本异常[%s]", e.getMessage()));
        } finally {
            decreaseReference();
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * 关闭脚本，释放资源
     */
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
     *
     * @return 当前引用计数
     */
    public int increaseReference() {
        return referenceCount.incrementAndGet();
    }

    /**
     * 减少引用计数
     *
     * @return 当前引用计数
     */
    public int decreaseReference() {
        return referenceCount.decrementAndGet();
    }

    /**
     * 当前引用计数
     *
     * @return 当前引用计数
     */
    public int currentReferenceCount() {
        return referenceCount.get();
    }

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
