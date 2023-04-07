package xin.manong.darwin.spider;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 编码探测器
 *
 * @author frankcl
 * @date 2023-04-06 17:52:17
 */
public class EncodeDetector {

    private static final Logger logger = LoggerFactory.getLogger(EncodeDetector.class);

    private static final int MAX_BYTE_SIZE = 1024;
    private static final String ENCODE_UTF8 = "UTF-8";

    /**
     * 探测字节数组编码类型
     *
     * @param body 字节数组
     * @return 编码类型
     */
    public static String detect(byte[] body) {
        return detect(body, MAX_BYTE_SIZE);
    }

    /**
     * 探测字节数组编码类型
     *
     * @param body 字节数组
     * @param byteSize 探测字节数量
     * @return 编码类型
     */
    public static String detect(byte[] body, int byteSize) {
        if (body == null || body.length == 0) {
            logger.error("detected body is null or empty");
            throw new RuntimeException("detected body is null or empty");
        }
        if (byteSize <= 0) byteSize = MAX_BYTE_SIZE;
        UniversalDetector detector = new UniversalDetector(null);
        try {
            detector.handleData(body, 0, body.length > byteSize ? byteSize : body.length);
            detector.dataEnd();
            String charset = detector.getDetectedCharset();
            return StringUtils.isEmpty(charset) ? ENCODE_UTF8 : charset;
        } finally {
            detector.reset();
        }
    }
}
