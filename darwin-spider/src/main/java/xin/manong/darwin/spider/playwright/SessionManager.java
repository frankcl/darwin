package xin.manong.darwin.spider.playwright;

import com.microsoft.playwright.BrowserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * 会话管理器
 *
 * @author frankcl
 * @date 2026-04-22 13:44:26
 */
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static final int DEFAULT_MAX_SESSION_NUM = 10;

    private final Semaphore semaphore;
    private final FeignBrowser browser;
    private final Map<String, Session> sessionMap;

    public SessionManager(FeignBrowser browser, int maxSessions) {
        if (maxSessions <= 0) maxSessions = DEFAULT_MAX_SESSION_NUM;
        semaphore = new Semaphore(maxSessions);
        sessionMap = new ConcurrentHashMap<>();
        this.browser = browser;
    }

    /**
     * 获取新会话
     *
     * @return 新会话
     */
    public Session acquire() {
        semaphore.acquireUninterruptibly();
        BrowserContext context = browser.newContext();
        Session session = Session.buildSession(context);
        Session prevSession = sessionMap.getOrDefault(session.getId(), null);
        closeSession(prevSession);
        sessionMap.put(session.getId(), session);
        return session;
    }

    /**
     * 释放会话
     *
     * @param id 会话ID
     */
    public void release(String id) {
        Session session = sessionMap.get(id);
        if (session == null) return;
        closeSession(session);
        semaphore.release();
    }

    /**
     * 释放会话
     *
     * @param session 会话
     */
    public void release(Session session) {
        if (session == null) return;
        if (sessionMap.get(session.getId()) != session) return;
        release(session.getId());
    }

    /**
     * 关闭会话
     *
     * @param session 会话
     */
    private void closeSession(Session session) {
        try {
            if (session == null) return;
            session.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
