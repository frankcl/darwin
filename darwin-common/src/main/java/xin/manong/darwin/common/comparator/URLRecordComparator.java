package xin.manong.darwin.common.comparator;

import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;

import java.util.Comparator;

/**
 * URL比较器
 * 1. 根据优先级比较
 * 2. 优先级相同，根据入队时间比较，入队时间越早越靠前
 *
 * @author frankcl
 * @date 2023-03-07 20:17:22
 */
public class URLRecordComparator implements Comparator<URLRecord> {
    @Override
    public int compare(URLRecord left, URLRecord right) {
        int leftPriority = left.priority == null ? Constants.PRIORITY_NORMAL : left.priority;
        int rightPriority = right.priority == null ? Constants.PRIORITY_NORMAL : right.priority;
        if (leftPriority < rightPriority) return -1;
        else if (leftPriority > rightPriority) return 1;
        long leftInQueueTime = left.pushTime == null ? System.currentTimeMillis() : left.pushTime;
        long rightInQueueTime = right.pushTime == null ? System.currentTimeMillis() : right.pushTime;
        return Long.compare(leftInQueueTime, rightInQueueTime);
    }
}
