package xin.manong.darwin.spider.core;

import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.spider.input.Input;
import xin.manong.weapon.base.common.Context;

import java.io.IOException;
import java.util.List;

/**
 * 资源爬虫
 *
 * @author frankcl
 * @date 2025-04-27 21:15:45
 */
public class ResourceSpider extends Spider {

    @Override
    public MediaType handle(URLRecord record, Input input, Context context) throws IOException {
        assert input != null;
        record.category = Constants.CONTENT_CATEGORY_RESOURCE;
        writer.write(record, input, context);
        return MediaType.UNKNOWN;
    }

    @Override
    public List<MediaType> supportedMediaTypes() {
        return List.of(MediaType.IMAGE, MediaType.VIDEO, MediaType.AUDIO, MediaType.PDF);
    }
}
