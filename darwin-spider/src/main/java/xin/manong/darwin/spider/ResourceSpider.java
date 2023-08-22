package xin.manong.darwin.spider;

import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
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

    public ResourceSpider() {
        super("resource");
    }

    @Override
    protected void handle(URLRecord record, Context context) throws Exception {
        SpiderResource resource = null;
        try {
            resource = getSpiderResource(record, context);
            if (resource == null) resource = fetchResource(record, context);
            if (resource == null || resource.inputStream == null) return;
            record.mimeType = resource.mimeType;
            record.subMimeType = resource.subMimeType;
            record.fetchTime = System.currentTimeMillis();
            if (!writeContent(record, resource.inputStream, context)) return;
            record.status = Constants.URL_STATUS_SUCCESS;
        } finally {
            if (resource != null) resource.close();
        }
    }
}
