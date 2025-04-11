package xin.manong.darwin.spider.output;

import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;

import java.io.IOException;
import java.io.InputStream;

/**
 * OSS数据输出
 *
 * @author frankcl
 * @date 2025-04-11 13:54:58
 */
public class OSSOutput extends Output {

    private final OSSMeta ossMeta;
    private final OSSClient ossClient;

    public OSSOutput(OSSMeta ossMeta, OSSClient ossClient) {
        this.ossMeta = ossMeta;
        this.ossClient = ossClient;
    }

    public OSSOutput(String ossURL, OSSClient ossClient) {
        this.ossMeta = OSSClient.parseURL(ossURL);
        this.ossClient = ossClient;
        if (ossMeta == null) throw new IllegalArgumentException("invalid output oss url");
    }

    @Override
    public void sink(InputStream inputStream) throws IOException {
        if (!ossClient.putObject(ossMeta.bucket, ossMeta.key, inputStream)) {
            throw new IOException(String.format("put oss object[%s:%s] failed", ossMeta.bucket, ossMeta.key));
        }
    }
}
