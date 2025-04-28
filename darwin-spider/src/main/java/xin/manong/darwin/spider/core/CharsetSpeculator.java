package xin.manong.darwin.spider.core;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.universalchardet.UniversalDetector;

/**
 * 字符集推测器
 * 负责推测输入字节数据所属字符集
 *
 * @author frankcl
 * @date 2025-04-11 14:43:59
 */
public class CharsetSpeculator {

    public static String speculate(byte[] byteArray) {
        return speculate(byteArray, 0, byteArray.length);
    }

    public static String speculate(byte[] byteArray, int offset, int length) {
        if (byteArray == null || byteArray.length == 0) {
            throw new IllegalArgumentException("Bytes are not allowed to be empty");
        }
        if (offset < 0 || offset > byteArray.length) {
            throw new IllegalArgumentException("Offset or length out of bounds of array");
        }
        UniversalDetector detector = new UniversalDetector(null);
        try {
            detector.handleData(byteArray, offset, Math.min(byteArray.length - offset, length));
            detector.dataEnd();
            String charset = detector.getDetectedCharset();
            return StringUtils.isEmpty(charset) ? null : charset;
        } finally {
            detector.reset();
        }
    }
}
