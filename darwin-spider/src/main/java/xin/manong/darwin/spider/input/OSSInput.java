package xin.manong.darwin.spider.input;

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
    private final OSSClient ossClient;

    public OSSInput(String ossURL, OSSClient ossClient) {
        this.ossMeta = OSSClient.parseURL(ossURL);
        this.ossClient = ossClient;
        if (ossMeta == null) throw new IllegalArgumentException("invalid input oss url");
    }

    public OSSInput(OSSMeta ossMeta, OSSClient ossClient) {
        this.ossMeta = ossMeta;
        this.ossClient = ossClient;
    }

    @Override
    public void open() throws IOException {
        close();
        inputStream = ossClient.getObjectStream(ossMeta.bucket, ossMeta.key);
    }
}
