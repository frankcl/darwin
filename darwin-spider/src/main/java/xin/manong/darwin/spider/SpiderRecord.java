package xin.manong.darwin.spider;

import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.base.common.Context;

/**
 * 爬虫数据
 *
 * @author frankcl
 * @date 2023-03-24 16:38:02
 */
public class SpiderRecord {

    public URLRecord record;
    public Context context;

    public SpiderRecord(Context context, URLRecord record) {
        this.context = context;
        this.record = record;
    }
}
