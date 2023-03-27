package xin.manong.darwin.spider.function;

import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseResponse;
import xin.manong.darwin.parse.service.ParseService;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.spider.async.SpiderConfig;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpClientConfig;

import javax.annotation.Resource;
import java.io.InputStream;

/**
 * @author frankcl
 * @date 2023-03-24 16:18:17
 */
public abstract class Spider {

    protected String category;
    @Resource
    protected SpiderConfig config;
    @Resource
    protected OSSClient ossClient;
    @Resource
    protected JobService jobService;
    @Resource
    protected RuleService ruleService;
    @Resource
    protected ParseService parseService;
    protected HttpClient httpClient;

    public Spider(String category) {
        this.category = category;
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        httpClientConfig.connectTimeoutSeconds = 5;
        httpClientConfig.readTimeoutSeconds = 10;
        httpClientConfig.keepAliveMinutes = 3;
        httpClientConfig.maxIdleConnections = 100;
        httpClientConfig.retryCnt = 3;
        httpClient = new HttpClient(httpClientConfig);
    }

    /**
     * 写入抓取内容到OSS
     *
     * @param record URL记录
     * @param bytes 内容字节数组
     * @return 成功返回true，否则返回false
     */
    public boolean writeContent(URLRecord record, byte[] bytes) {
        String key = String.format("%s/%s/%s", config.contentDirectory, category, record.hash);
        if (!ossClient.putObject(config.contentBucket, key, bytes)) return false;
        OSSMeta ossMeta = new OSSMeta();
        ossMeta.region = config.contentRegion;
        ossMeta.bucket = config.contentBucket;
        ossMeta.key = key;
        record.fetchContentURL = OSSClient.buildURL(ossMeta);
        return true;
    }

    /**
     * 写入抓取内容到OSS
     *
     * @param record URL记录
     * @param inputStream 内容字节流
     * @return 成功返回true，否则返回false
     */
    public boolean writeContent(URLRecord record, InputStream inputStream) {
        String key = String.format("%s/%s/%s", config.contentDirectory, category, record.hash);
        if (!ossClient.putObject(config.contentBucket, key, inputStream)) return false;
        OSSMeta ossMeta = new OSSMeta();
        ossMeta.region = config.contentRegion;
        ossMeta.bucket = config.contentBucket;
        ossMeta.key = key;
        record.fetchContentURL = OSSClient.buildURL(ossMeta);
        return true;
    }

    /**
     * 抓取
     *
     * @param record URL记录
     * @return 抓取响应
     */
    public abstract FetchResponse fetch(URLRecord record);

    /**
     * 解析
     *
     * @param record URL记录
     * @param html HTML
     * @return 解析响应
     */
    public abstract ParseResponse parse(URLRecord record, String html);

    /**
     * 是否支持解析
     *
     * @return 支持返回true，否则返回false
     */
    public boolean supportParse() {
        return false;
    };
}
