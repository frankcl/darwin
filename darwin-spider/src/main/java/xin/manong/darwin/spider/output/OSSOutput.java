package xin.manong.darwin.spider.output;

import xin.manong.darwin.service.iface.OSSService;

import java.io.IOException;
import java.io.InputStream;

/**
 * OSS数据输出
 *
 * @author frankcl
 * @date 2025-04-11 13:54:58
 */
public class OSSOutput extends Output {

    private final String key;
    private final OSSService ossService;

    public OSSOutput(String key, OSSService ossService) {
        this.key = key;
        this.ossService = ossService;
    }

    @Override
    public void sink(InputStream inputStream) throws IOException {
        if (!ossService.put(key, inputStream)) {
            throw new IOException(String.format("put oss object failed for key: %s", key));
        }
    }
}
