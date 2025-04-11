package xin.manong.darwin.spider.core;

/**
 * 异步爬虫任务
 *
 * @author frankcl
 * @date 2023-03-24 15:36:56
 */
public class SpiderTask implements Runnable {

    protected Spider spider;
    protected SpiderRecord spiderRecord;

    public SpiderTask(SpiderRecord spiderRecord, Spider spider) {
        this.spiderRecord = spiderRecord;
        this.spider = spider;
    }

    @Override
    public void run() {
        spider.process(spiderRecord.record, spiderRecord.context);
    }
}
