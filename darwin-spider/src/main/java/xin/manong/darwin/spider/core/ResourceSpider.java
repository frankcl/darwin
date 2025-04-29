package xin.manong.darwin.spider.core;

import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.MediaType;
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
@Component
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
        return List.of(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG, MediaType.IMAGE_BMP, MediaType.IMAGE_AVIF,
                MediaType.IMAGE_GIF, MediaType.IMAGE_SVG, MediaType.IMAGE_TIFF, MediaType.IMAGE_WEBP,
                MediaType.VIDEO_MPEG, MediaType.VIDEO_MP4, MediaType.VIDEO_ASF, MediaType.VIDEO_AVI,
                MediaType.VIDEO_QUICKTIME, MediaType.VIDEO_OGG, MediaType.VIDEO_WEBM,
                MediaType.AUDIO_AAC, MediaType.AUDIO_AU, MediaType.AUDIO_FLAC, MediaType.AUDIO_MP3,
                MediaType.AUDIO_WAV, MediaType.AUDIO_WMA, MediaType.APPLICATION_PDF,
                MediaType.APPLICATION_DOC, MediaType.APPLICATION_DOCX, MediaType.APPLICATION_XLS,
                MediaType.APPLICATION_XLSX, MediaType.APPLICATION_PPT, MediaType.APPLICATION_PPTX,
                MediaType.APPLICATION_OCTET);
    }
}
