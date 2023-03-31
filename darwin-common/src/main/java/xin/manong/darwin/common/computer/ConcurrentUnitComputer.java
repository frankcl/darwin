package xin.manong.darwin.common.computer;

import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.base.util.CommonUtil;
import xin.manong.weapon.base.util.DomainUtil;

/**
 * 并发单元计算器
 *
 * @author frankcl
 * @date 2023-03-09 19:43:10
 */
public class ConcurrentUnitComputer {

    /**
     * 根据URL数据计算并发单元
     *
     * @param record URL数据
     * @return 并发单元
     */
    public static String compute(URLRecord record) {
        String host = CommonUtil.getHost(record.url);
        if (record.concurrentLevel != null && record.concurrentLevel == Constants.CONCURRENT_LEVEL_HOST) return host;
        return DomainUtil.getDomain(host);
    }
}
