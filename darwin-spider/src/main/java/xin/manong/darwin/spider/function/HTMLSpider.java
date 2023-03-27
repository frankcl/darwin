package xin.manong.darwin.spider.function;

import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.LinkURL;
import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestMethod;

import java.nio.charset.Charset;
import java.util.ArrayList;
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
    public FetchResponse fetch(URLRecord record) {
        Response httpResponse = null;
        try {
            HttpRequest httpRequest = new HttpRequest.Builder().requestURL(record.url).method(RequestMethod.GET).build();
            if (record.headers != null && !record.headers.isEmpty()) httpRequest.headers = record.headers;
            httpResponse = httpClient.execute(httpRequest);
            if (httpResponse == null || !httpResponse.isSuccessful()) {
                record.status = Constants.URL_STATUS_FAIL;
                logger.error("execute http request failed for url[{}]", record.url);
                return FetchResponse.buildError(httpResponse == null ? 500 :
                        httpResponse.code(), "执行HTTP请求失败");
            }
            String content = httpResponse.body().string();
            byte[] bytes = content.getBytes(Charset.forName("UTF-8"));
            if (!writeContent(record, bytes)) {
                record.status = Constants.URL_STATUS_FAIL;
                logger.error("write fetch content failed for url[{}]", record.url);
                return FetchResponse.buildError(httpResponse.code(), "抓取内容写入OSS失败");
            }
            return FetchResponse.buildOK(httpResponse.code(), new String(bytes, Charset.forName("UTF-8")));
        } catch (Exception e) {
            if (httpResponse != null) httpResponse.close();
            record.status = Constants.URL_STATUS_FAIL;
            logger.error("fetch content error for url[{}]", record.url);
            logger.error(e.getMessage(), e);
            return FetchResponse.buildError(httpResponse == null ? 500 : httpResponse.code(), e.getMessage());
        }
    }

    @Override
    public ParseResponse parse(URLRecord record, String html) {
        try {
            Job job = jobService.getCache(record.jobId);
            if (job == null) {
                record.status = Constants.URL_STATUS_FAIL;
                logger.error("job[{}] is not found for url[{}]", record.jobId, record.url);
                return ParseResponse.buildErrorResponse(String.format("任务[%s]不存在", record.jobId));
            }
            List<Rule> rules = new ArrayList<>();
            for (Integer ruleId : job.ruleIds) {
                Rule rule = ruleService.getCache(ruleId.longValue());
                if (rule == null || !ruleService.match(record, rule)) continue;
                rules.add(rule);
            }
            if (rules.size() != 1) {
                record.status = Constants.URL_STATUS_FAIL;
                logger.error("match rule num[{}] is unexpected", rules.size());
                return ParseResponse.buildErrorResponse(String.format("匹配规则数[%d]不符合预期", rules.size()));
            }
            LinkURL linkURL = new LinkURL(record.url);
            if (record.headers != null && !record.headers.isEmpty()) linkURL.headers = record.headers;
            if (record.userDefinedMap != null && !record.userDefinedMap.isEmpty())
                linkURL.userDefinedMap = record.userDefinedMap;
            ParseRequest request = new ParseRequest.Builder().content(html).linkURL(linkURL).build();
            ParseResponse response = parseService.parse(rules.get(0), request);
            if (!response.status) return response;
            if (response.structureMap != null && !response.structureMap.isEmpty()) {
                record.structureMap = response.structureMap;
            }
            return response;
        } catch (Exception e) {
            record.status = Constants.URL_STATUS_FAIL;
            logger.error("parse content error for url[{}]", record.url);
            logger.error(e.getMessage(), e);
            return ParseResponse.buildErrorResponse(String.format("解析内容失败[%s]", record.url));
        }
    }

    @Override
    public boolean supportParse() {
        return true;
    }
}
