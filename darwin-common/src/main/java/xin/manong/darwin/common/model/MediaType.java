package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 媒体类型
 *
 * @author frankcl
 * @date 2025-04-27 14:03:57
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaType {

    public static MediaType UNKNOWN = new MediaType("unknown", "unknown");

    public static MediaType TEXT_PLAIN = new MediaType("text", "plain");
    public static MediaType TEXT_HTML = new MediaType("text", "html");
    public static MediaType TEXT_XML = new MediaType("text", "xml");
    public static MediaType TEXT_CSV = new MediaType("text", "csv");
    public static MediaType TEXT_CSS = new MediaType("text", "css");
    public static MediaType TEXT_JAVASCRIPT = new MediaType("text", "javascript");

    public static MediaType IMAGE_JPEG = new MediaType("image", "jpeg");
    public static MediaType IMAGE_PNG = new MediaType("image", "png");
    public static MediaType IMAGE_GIF = new MediaType("image", "gif");
    public static MediaType IMAGE_BMP = new MediaType("image", "bmp");
    public static MediaType IMAGE_TIFF = new MediaType("image", "tiff");
    public static MediaType IMAGE_WEBP = new MediaType("image", "webp");
    public static MediaType IMAGE_AVIF = new MediaType("image", "avif");
    public static MediaType IMAGE_SVG = new MediaType("image", "svg+xml");

    public static MediaType VIDEO_MP4 = new MediaType("video", "mp4");
    public static MediaType VIDEO_MPEG = new MediaType("video", "mpeg");
    public static MediaType VIDEO_WEBM = new MediaType("video", "webm");
    public static MediaType VIDEO_OGG = new MediaType("video", "ogg");
    public static MediaType VIDEO_AVI = new MediaType("video", "x-msvideo");
    public static MediaType VIDEO_QUICKTIME = new MediaType("video", "quicktime");
    public static MediaType VIDEO_ASF = new MediaType("video", "x-ms-asf");

    public static MediaType AUDIO_XMPEG = new MediaType("audio", "x-mpeg");
    public static MediaType AUDIO_MPEG = new MediaType("audio", "mpeg");
    public static MediaType AUDIO_AU = new MediaType("audio", "basic");
    public static MediaType AUDIO_WAV = new MediaType("audio", "x-wav");
    public static MediaType AUDIO_FLAC = new MediaType("audio", "flac");
    public static MediaType AUDIO_AAC = new MediaType("audio", "aac");
    public static MediaType AUDIO_WMA = new MediaType("audio", "wma");

    public static MediaType APPLICATION_JSON = new MediaType("application", "json");
    public static MediaType APPLICATION_XML = new MediaType("application", "xml");
    public static MediaType APPLICATION_PDF = new MediaType("application", "pdf");
    public static MediaType APPLICATION_JAVASCRIPT = new MediaType("application", "javascript");
    public static MediaType APPLICATION_X_JAVASCRIPT = new MediaType("application", "x-javascript");
    public static MediaType APPLICATION_XHTML = new MediaType("application", "xhtml+xml");
    public static MediaType APPLICATION_DOC = new MediaType("application", "msword");
    public static MediaType APPLICATION_DOCX = new MediaType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document");
    public static MediaType APPLICATION_XLS = new MediaType("application", "vnd.ms-excel");
    public static MediaType APPLICATION_XLSX = new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    public static MediaType APPLICATION_PPT = new MediaType("application", "vnd.ms-powerpoint");
    public static MediaType APPLICATION_PPTX = new MediaType("application", "vnd.openxmlformats-officedocument.presentationml.presentation");
    public static MediaType APPLICATION_OCTET = new MediaType("application", "octet-stream");

    public static MediaType STREAM_M3U8 = new MediaType("stream", "m3u8");

    @JSONField(name = "mime_type")
    @JsonProperty("mime_type")
    public String mimeType;
    @JSONField(name = "sub_mime_type")
    @JsonProperty("sub_mime_type")
    public String subMimeType;
    @JSONField(name = "charset")
    @JsonProperty("charset")
    public String charset;
    @JSONField(name = "alias")
    @JsonProperty("alias")
    public String alias;
    @JSONField(name = "suffix")
    @JsonProperty("suffix")
    public String suffix;

    public MediaType(String mimeType, String subMimeType) {
        this.mimeType = mimeType == null ? null : mimeType.toLowerCase();
        this.subMimeType = subMimeType == null ? null : subMimeType.toLowerCase();
        this.charset = null;
        this.alias = "UNKNOWN";
        this.suffix = null;
        initMedia();
    }

    /**
     * 是否为文本
     *
     * @return 是返回true，否则返回false
     */
    public boolean isText() {
        return mimeType != null && mimeType.equals("text");
    }

    /**
     * 是否为图片
     *
     * @return 是返回true，否则返回false
     */
    public boolean isImage() {
        return mimeType != null && mimeType.equals("image");
    }

    /**
     * 是否为视频
     *
     * @return 是返回true，否则返回false
     */
    public boolean isVideo() {
        return mimeType != null && mimeType.equals("video");
    }

    /**
     * 是否为音频
     *
     * @return 是返回true，否则返回false
     */
    public boolean isAudio() {
        return mimeType != null && mimeType.equals("audio");
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MediaType mediaType)) return false;
        if (object == this) return true;
        return Objects.equals(mimeType, mediaType.mimeType) &&
                Objects.equals(subMimeType, mediaType.subMimeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mimeType, subMimeType);
    }

    @Override
    public String toString() {
        if (StringUtils.isEmpty(mimeType) || StringUtils.isEmpty(subMimeType)) return "unknown";
        return String.format("%s/%s", mimeType, subMimeType);
    }

    /**
     * 初始化不同媒体类型的别名和后缀
     */
    private void initMedia() {
        if (mimeType == null || subMimeType == null) return;
        switch (mimeType) {
            case "text" -> initTextMedia();
            case "image" -> initImageMedia();
            case "video" -> initVideoMedia();
            case "audio" -> initAudioMedia();
            case "application" -> initApplicationMedia();
            case "stream" -> initStreamMedia();
        }
    }

    /**
     * 初始化文本媒体类型的别名和后缀
     */
    private void initTextMedia() {
        switch (subMimeType) {
            case "plain" -> {
                alias = "PLAIN";
                suffix = "txt";
            }
            case "html" -> {
                alias = "HTML";
                suffix = "html";
            }
            case "xml" -> {
                alias = "XML";
                suffix = "xml";
            }
            case "css" -> {
                alias = "CSS";
                suffix = "css";
            }
            case "javascript" -> {
                alias = "JAVASCRIPT";
                suffix = "js";
            }
            case "csv" -> {
                alias = "CSV";
                suffix = "csv";
            }
            default -> {
                alias = "PLAIN";
            }
        }
    }

    /**
     * 初始化图片媒体类型的别名和后缀
     */
    private void initImageMedia() {
        switch (subMimeType) {
            case "jpeg" -> {
                alias = "IMAGE";
                suffix = "jpeg";
            }
            case "png" -> {
                alias = "IMAGE";
                suffix = "png";
            }
            case "gif" -> {
                alias = "IMAGE";
                suffix = "gif";
            }
            case "avif" -> {
                alias = "IMAGE";
                suffix = "avif";
            }
            case "webp" -> {
                alias = "IMAGE";
                suffix = "webp";
            }
            case "bmp" -> {
                alias = "IMAGE";
                suffix = "bmp";
            }
            case "tiff" -> {
                alias = "IMAGE";
                suffix = "tiff";
            }
            case "svg+xml" -> {
                alias = "IMAGE";
                suffix = "svg";
            }
            default -> {
                alias = "IMAGE";
            }
        }
    }

    /**
     * 初始化视频媒体类型的别名和后缀
     */
    private void initVideoMedia() {
        switch (subMimeType) {
            case "mp4" -> {
                alias = "VIDEO";
                suffix = "mp4";
            }
            case "webm" -> {
                alias = "VIDEO";
                suffix = "webm";
            }
            case "ogg" -> {
                alias = "VIDEO";
                suffix = "ogg";
            }
            case "x-msvideo" -> {
                alias = "VIDEO";
                suffix = "avi";
            }
            case "quicktime" -> {
                alias = "VIDEO";
                suffix = "mov";
            }
            case "mpeg" -> {
                alias = "VIDEO";
                suffix = "mpeg";
            }
            case "x-ms-asf" -> {
                alias = "VIDEO";
                suffix = "asf";
            }
            default -> {
                alias = "VIDEO";
            }
        }
    }

    /**
     * 初始化音频媒体类型的别名和后缀
     */
    private void initAudioMedia() {
        switch (subMimeType) {
            case "basic" -> {
                alias = "AUDIO";
                suffix = "au";
            }
            case "x-wav" -> {
                alias = "AUDIO";
                suffix = "wav";
            }
            case "flac" -> {
                alias = "AUDIO";
                suffix = "flac";
            }
            case "aac" -> {
                alias = "AUDIO";
                suffix = "aac";
            }
            case "wma" -> {
                alias = "AUDIO";
                suffix = "wma";
            }
            case "x-mpeg", "mpeg" -> {
                alias = "AUDIO";
                suffix = "mp3";
            }
            default -> {
                alias = "AUDIO";
            }
        }
    }

    /**
     * 初始化应用媒体类型的别名和后缀
     */
    private void initApplicationMedia() {
        switch (subMimeType) {
            case "json" -> {
                alias = "JSON";
                suffix = "json";
            }
            case "xml" -> {
                alias = "XML";
                suffix = "xml";
            }
            case "pdf" -> {
                alias = "PDF";
                suffix = "pdf";
            }
            case "javascript" -> {
                alias = "JAVASCRIPT";
                suffix = "js";
            }
            case "xhtml+xml" -> {
                alias = "XHTML";
                suffix = "xhtml";
            }
            case "msword" -> {
                alias = "DOC";
                suffix = "doc";
            }
            case "vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                alias = "DOCX";
                suffix = "docx";
            }
            case "vnd.ms-excel" -> {
                alias = "XLS";
                suffix = "xls";
            }
            case "vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> {
                alias = "XLSX";
                suffix = "xlsx";
            }
            case "vnd.ms-powerpoint" -> {
                alias = "PPT";
                suffix = "ppt";
            }
            case "vnd.openxmlformats-officedocument.presentationml.presentation" -> {
                alias = "PPTX";
                suffix = "pptx";
            }
            case "octet-stream" -> {
                alias = "OCTET-STREAM";
            }
        }
    }

    /**
     * 初始化流媒体类型
     */
    private void initStreamMedia() {
        if (subMimeType.equals("m3u8")) {
            alias = "M3U8";
        }
    }
}
