package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Lazy;
import xin.manong.darwin.common.model.MediaType;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.spider.input.Input;
import xin.manong.weapon.base.common.Context;

import java.io.IOException;
import java.util.List;

/**
 * 数据爬虫接口
 *
 * @author frankcl
 * @date 2025-04-27 18:09:35
 */
public abstract class Spider implements InitializingBean {

    @Resource
    protected SpiderConfig spiderConfig;
    @Resource
    @Lazy
    protected Router router;
    @Resource
    protected Writer writer;

    /**
     * 爬取数据
     *
     * @param record 数据
     * @param context 上下文
     * @return 下一步处理的媒体类型
     * @throws IOException I/O异常
     */
    public MediaType handle(URLRecord record, Context context) throws IOException {
        return handle(record, null, context);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        supportedMediaTypes().forEach(mediaType -> router.registerSpider(mediaType, this));
    }

    /**
     * 爬取数据
     *
     * @param record 数据
     * @param input 输入数据源
     * @param context 上下文
     * @return 下一步处理的媒体类型
     * @throws IOException I/O异常
     */
    public abstract MediaType handle(URLRecord record, Input input, Context context) throws IOException;

    /**
     * 支持媒体类型列表
     *
     * @return 支持媒体类型列表
     */
    public abstract List<MediaType> supportedMediaTypes();
}
