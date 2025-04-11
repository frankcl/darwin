package xin.manong.darwin.spider.core;

import org.springframework.stereotype.Component;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.spider.input.Input;
import xin.manong.weapon.base.common.Context;

/**
 * 资源爬虫
 * 1. 文档
 * 2. 图片
 * 3. 视频
 *
 * @author frankcl
 * @date 2023-03-24 16:22:15
 */
@Component
public class ResourceSpider extends Spider {

    private static final String CATEGORY = "resource";

    public ResourceSpider() {
        super(CATEGORY);
    }

    @Override
    protected void handle(URLRecord record, Context context) throws Exception {
        Input input = buildInput(record, context);
        write(record, input, context);
    }
}
