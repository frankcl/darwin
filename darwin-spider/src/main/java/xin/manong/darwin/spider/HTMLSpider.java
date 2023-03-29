package xin.manong.darwin.spider;

import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.weapon.base.common.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 网页爬虫
 * 1. HTML网页
 * 2. JSON内容
 * 3. 其他文本内容
 *
 * @author frankcl
 * @date 2023-03-24 16:21:30
 */
@Component
public class HTMLSpider extends Spider {

    private static final Logger logger = LoggerFactory.getLogger(HTMLSpider.class);

    public HTMLSpider() {
        super("html");
    }

    @Override
    protected void handle(URLRecord record, Context context) throws Exception {
        Long executeFetchTime = 0L, executePutTime = 0L, executeParseTime = 0L;
        try {
            Rule rule = getMatchRule(record, context);
            if (rule == null) {
                record.status = Constants.URL_STATUS_FAIL;
                return;
            }
            Long startFetchTime = System.currentTimeMillis();
            String content = getContentHTML(record, context);
            executeFetchTime = System.currentTimeMillis() - startFetchTime;
            if (content == null) {
                record.status = Constants.URL_STATUS_FAIL;
                return;
            }
            byte[] bytes = content.getBytes(Charset.forName("UTF-8"));
            Long startPutTime = System.currentTimeMillis();
            if (!writeContent(record, bytes)) {
                executePutTime = System.currentTimeMillis() - startPutTime;
                record.status = Constants.URL_STATUS_FAIL;
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取内容写入OSS失败");
                logger.error("write fetch content failed for url[{}]", record.url);
                return;
            }
            executePutTime = System.currentTimeMillis() - startPutTime;
            Long startParseTime = System.currentTimeMillis();
            ParseRequest request = new ParseRequest.Builder().content(content).record(record).build();
            ParseResponse response = parseService.parse(rule, request);
            executeParseTime = System.currentTimeMillis() - startParseTime;
            if (!response.status) {
                record.status = Constants.URL_STATUS_FAIL;
                context.put(Constants.DARWIN_DEBUG_MESSAGE, String.format("解析失败[%s]", response.message));
                logger.error("parse content failed for url[{}], cause[{}]", record.url, response.message);
                return;
            }
            if (response.structureMap != null && !response.structureMap.isEmpty()) {
                record.structureMap = response.structureMap;
            }
            if (response.userDefinedMap != null && !response.userDefinedMap.isEmpty()) {
                record.userDefinedMap.putAll(response.userDefinedMap);
            }
            handleFollowLinks(response.followLinks, record, context);
            record.status = Constants.URL_STATUS_SUCCESS;
        } finally {
            context.put(Constants.DARWIN_FETCH_TIME, executeFetchTime);
            context.put(Constants.DARWIN_PUT_TIME, executePutTime);
            context.put(Constants.DARWIN_PARSE_TIME, executeParseTime);
        }
    }

    /**
     * 获取内容HTML
     * 1. 如果避免重复抓取，读取OSS内容
     * 2. 如果不满足1条件，直接抓取
     *
     * @param record URL数据
     * @param context 上下文
     * @return 成功返回HTML，否则返回null
     * @throws IOException
     */
    private String getContentHTML(URLRecord record, Context context) throws IOException {
        ByteArrayOutputStream outputStream = null;
        InputStream inputStream = (InputStream) context.get(Constants.DARWIN_INPUT_STREAM);
        if (inputStream != null) {
            int size = 4096, n;
            byte[] buffer = new byte[size];
            try {
                outputStream = new ByteArrayOutputStream();
                while ((n = inputStream.read(buffer, 0, size)) != -1) {
                    outputStream.write(buffer, 0, n);
                }
                return new String(outputStream.toByteArray(), Charset.forName("UTF-8"));
            } finally {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
            }
        }
        Response httpResponse = fetch(record, context);
        if (httpResponse == null) return null;
        try {
            return httpResponse.body().string();
        } finally {
            httpResponse.close();
        }
    }

    /**
     * 处理抽链结果
     *
     * @param followLinks 抽链数据列表
     * @param record 父链接URL
     * @param context 上下文
     */
    private void handleFollowLinks(List<URLRecord> followLinks, URLRecord record, Context context) {
        int discardFollowLinkNum = 0;
        for (URLRecord followLink : followLinks) {
            try {
                followLink.jobId = record.jobId;
                followLink.parentURL = record.url;
                followLink.depth = record.depth + 1;
                if (followLink.priority == null) followLink.priority = record.priority;
                if (followLink.concurrentLevel == null) followLink.concurrentLevel = record.concurrentLevel;
                if (!followLink.check()) {
                    discardFollowLinkNum++;
                    logger.warn("invalid follow link[{}] for parent url[{}]", followLink.url, record.url);
                    continue;
                }
                multiQueueService.pushQueue(followLink);
            } finally {
                Context linkFollowContext = new Context();
                DarwinUtil.putContext(linkFollowContext, followLink);
                linkFollowContext.put(Constants.DARWIN_RECORD_TYPE, Constants.RECORD_TYPE_FOLLOW_LINK);
                if (aspectLogger != null) aspectLogger.commit(linkFollowContext.getFeatureMap());
            }
        }
        context.put(Constants.FOLLOW_LINK_NUM, followLinks.size());
        context.put(Constants.DISCARD_FOLLOW_LINK_NUM, discardFollowLinkNum);
    }
}
