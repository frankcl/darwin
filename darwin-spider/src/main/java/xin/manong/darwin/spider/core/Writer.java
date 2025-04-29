package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.OSSService;
import xin.manong.darwin.spider.input.Input;
import xin.manong.darwin.spider.output.OSSOutput;
import xin.manong.weapon.base.common.Context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 爬取数据写入
 *
 * @author frankcl
 * @date 2025-04-27 18:12:46
 */
@Component
public class Writer {

    @Resource
    private SpiderConfig spiderConfig;
    @Resource
    private OSSService ossService;
    private final Map<Integer, String> categoryDirMap;

    public Writer() {
        categoryDirMap = new HashMap<>();
        categoryDirMap.put(Constants.CONTENT_CATEGORY_PAGE, "text");
        categoryDirMap.put(Constants.CONTENT_CATEGORY_RESOURCE, "resource");
        categoryDirMap.put(Constants.CONTENT_CATEGORY_STREAM, "stream");
    }

    /**
     * 爬取数据写入
     *
     * @param record 数据
     * @param input 输入数据源
     * @param context 上下文
     */
    public void write(URLRecord record, Input input, Context context) throws IOException {
        long startTime = System.currentTimeMillis();
        try {
            if (!input.isOpened()) input.open();
            String ossKey = buildOSSKey(record);
            OSSOutput output = new OSSOutput(ossKey, ossService);
            input.transport(output);
            record.fetchContentURL = ossService.buildURL(ossKey);
        } finally {
            context.put(Constants.DARWIN_WRITE_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 构建OSS key
     *
     * @param record URL数据
     * @return OSS key
     */
    private String buildOSSKey(URLRecord record) {
        String suffix = record.mediaType == null ? null : record.mediaType.suffix;
        String key = String.format("%s/%s/%s", spiderConfig.ossDirectory,
                categoryDirMap.get(record.category), record.key);
        if (StringUtils.isEmpty(suffix)) return key;
        return String.format("%s.%s", key, suffix);
    }
}
