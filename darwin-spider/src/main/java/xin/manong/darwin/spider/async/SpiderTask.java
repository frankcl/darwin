package xin.manong.darwin.spider.async;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.spider.function.Spider;
import xin.manong.darwin.spider.function.SpiderFactory;
import xin.manong.weapon.base.log.JSONLogger;

/**
 * 爬虫任务
 * 抓取URL记录
 *
 * @author frankcl
 * @date 2023-03-24 15:36:56
 */
public class SpiderTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SpiderTask.class);

    protected SpiderRecord spiderRecord;
    protected SpiderFactory spiderFactory;
    protected JSONLogger aspectLogger;

    public SpiderTask(SpiderRecord spiderRecord, SpiderFactory spiderFactory) {
        this.spiderRecord = spiderRecord;
        this.spiderFactory = spiderFactory;
    }

    @Override
    public void run() {
        try {
            Spider spider = spiderFactory.build(spiderRecord.record);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            spiderRecord.context.put(Constants.DARWIN_DEBUG_MESSAGE, t.getMessage());
            spiderRecord.context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(t));
            spiderRecord.record.status = Constants.URL_STATUS_FAIL;
            DarwinUtil.putContext(spiderRecord.context, spiderRecord.record);
            if (aspectLogger != null) aspectLogger.commit(spiderRecord.context.getFeatureMap());
        }
    }

    /**
     * 设置切面日志
     *
     * @param aspectLogger 切面日志
     */
    public void setAspectLogger(JSONLogger aspectLogger) {
        this.aspectLogger = aspectLogger;
    }
}
