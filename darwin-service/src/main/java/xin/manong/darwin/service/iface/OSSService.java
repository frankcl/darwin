package xin.manong.darwin.service.iface;

import java.io.InputStream;

/**
 * OSS服务接口
 *
 * @author frankcl
 * @date 2025-04-14 17:52:33
 */
public interface OSSService {

    /**
     * 根据key判断OSS对象是否存在
     *
     * @param key OSS key
     * @return 存在返回true，否则返回false
     */
    boolean existsByKey(String key);

    /**
     * 根据URL判断OSS对象是否存在
     *
     * @param ossURL URL
     * @return 存在返回true，否则返回false
     */
    boolean existsByURL(String ossURL);

    /**
     * 根据key删除OSS对象
     *
     * @param key OSS key
     */
    void deleteByKey(String key);

    /**
     * 根据URL删除OSS对象
     *
     * @param ossURL OSS URL
     */
    void deleteByURL(String ossURL);

    /**
     * 数据写入OSS
     *
     * @param key OSS key
     * @param inputStream 输入流
     * @return 成功返回true，否则返回false
     */
    boolean put(String key, InputStream inputStream);

    /**
     * 数据写入OSS
     *
     * @param key OSS key
     * @param byteArray 字节数据
     * @return 成功返回true，否则返回false
     */
    boolean put(String key, byte[] byteArray);

    /**
     * 获取OSS对象内容
     *
     * @param ossURL OSS URL
     * @return OSS对象内容
     */
    byte[] getByURL(String ossURL);

    /**
     * 获取OSS对象内容
     *
     * @param key OSS key
     * @return OSS对象内容
     */
    byte[] getByKey(String key);

    /**
     * 获取OSS对象数据流
     *
     * @param ossURL OSS URL
     * @return 对象数据流
     */
    InputStream getObjectStream(String ossURL);

    /**
     * 获取OSS对象数据流
     *
     * @param bucket OSS bucket
     * @param key OSS key
     * @return 对象数据流
     */
    InputStream getObjectStream(String bucket, String key);

    /**
     * 构建OSS URL
     *
     * @param ossKey OSS key
     * @return OSS URL
     */
    String buildURL(String ossKey);

    /**
     * 加签URL
     *
     * @param ossURL 待加签URL
     * @return 加签URL
     */
    String signURL(String ossURL);
}
