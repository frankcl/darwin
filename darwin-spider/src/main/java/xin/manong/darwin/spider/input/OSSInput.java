package xin.manong.darwin.spider.input;

import xin.manong.darwin.service.iface.OSSService;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;

import java.io.IOException;

/**
 * OSS数据源输入
 *
 * @author frankcl
 * @date 2025-04-11 13:30:11
 */
public class OSSInput extends Input {

    private final OSSMeta ossMeta;
    private final OSSService ossService;

    public OSSInput(String ossURL, OSSService ossService) {
        this.ossMeta = OSSClient.parseURL(ossURL);
        this.ossService = ossService;
        if (ossMeta == null) throw new IllegalArgumentException("invalid input oss url");
    }

    public OSSInput(OSSMeta ossMeta, OSSService ossService) {
        this.ossMeta = ossMeta;
        this.ossService = ossService;
    }

    @Override
    public void open() throws IOException {
        close();
        inputStream = ossService.getObjectStream(ossMeta.bucket, ossMeta.key);
    }
}
