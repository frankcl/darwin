package xin.manong.darwin.service.impl;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.iface.OSSService;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;

import java.io.InputStream;

/**
 * OSS服务实现
 *
 * @author frankcl
 * @date 2025-04-14 17:59:47
 */
@Service
public class OSSServiceImpl implements OSSService {

    private static final Logger logger = LoggerFactory.getLogger(OSSServiceImpl.class);

    @Resource
    private ServiceConfig config;
    @Resource
    private OSSClient ossClient;

    @Override
    public boolean existsByKey(String key) {
        return ossClient.exist(config.oss.bucket, key);
    }

    @Override
    public boolean existsByURL(String ossURL) {
        OSSMeta ossMeta = OSSClient.parseURL(ossURL);
        if (ossMeta == null) {
            logger.warn("invalid exists oss url: {}", ossURL);
            return false;
        }
        return existsByKey(ossMeta.key);
    }

    @Override
    public void deleteByKey(String key) {
        ossClient.deleteObject(config.oss.bucket, key);
    }

    @Override
    public void deleteByURL(String ossURL) {
        OSSMeta ossMeta = OSSClient.parseURL(ossURL);
        if (ossMeta == null) {
            logger.warn("invalid deleting oss url: {}", ossURL);
            return;
        }
        ossClient.deleteObject(ossMeta.bucket, ossMeta.key);
    }

    @Override
    public boolean put(String key, InputStream inputStream) {
        return ossClient.putObject(config.oss.bucket, key, inputStream);
    }

    @Override
    public boolean put(String key, byte[] byteArray) {
        return ossClient.putObject(config.oss.bucket, key, byteArray);
    }

    @Override
    public byte[] getByURL(String ossURL) {
        OSSMeta ossMeta = OSSClient.parseURL(ossURL);
        if (ossMeta == null) throw new IllegalArgumentException(String.format("invalid oss url: %s", ossURL));
        return ossClient.getObject(ossMeta.bucket, ossMeta.key);
    }

    @Override
    public byte[] getByKey(String key) {
        return ossClient.getObject(config.oss.bucket, key);
    }

    @Override
    public InputStream getObjectStream(String ossURL) {
        OSSMeta ossMeta = OSSClient.parseURL(ossURL);
        if (ossMeta == null) throw new IllegalArgumentException(String.format("invalid oss url: %s", ossURL));
        return ossClient.getObjectStream(ossMeta.bucket, ossMeta.key);
    }

    @Override
    public InputStream getObjectStream(String bucket, String key) {
        return ossClient.getObjectStream(bucket, key);
    }

    @Override
    public String buildURL(String ossKey) {
        OSSMeta ossMeta = new OSSMeta(config.oss.region, config.oss.bucket, ossKey);
        return OSSClient.buildURL(ossMeta);
    }

    @Override
    public String signURL(String ossURL) {
        OSSMeta ossMeta = OSSClient.parseURL(ossURL);
        if (ossMeta == null) {
            logger.warn("invalid sign oss url: {}", ossURL);
            return ossURL;
        }
        return ossClient.sign(ossMeta.bucket, ossMeta.key);
    }
}
