package xin.manong.darwin.spider.core;

/**
 * 异步爬虫任务
 *
 * @author frankcl
 * @date 2023-03-24 15:36:56
 */
public class SpiderTask implements Runnable {

    private final Router router;
    protected final SpiderRecord spiderRecord;

    public SpiderTask(SpiderRecord spiderRecord, Router router) {
        this.spiderRecord = spiderRecord;
        this.router = router;
    }

    @Override
    public void run() {
        router.route(spiderRecord.record, spiderRecord.context);
    }
}
