package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.AppSecret;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.request.AppSecretSearchRequest;

/**
 * 应用秘钥服务接口
 *
 * @author frankcl
 * @date 2025-10-16 14:56:16
 */
public interface AppSecretService {

    /**
     * 添加应用秘钥
     *
     * @param appSecret 应用秘钥
     * @return 成功返回true，否则返回false
     */
    boolean add(AppSecret appSecret);

    /**
     * 更新应用秘钥
     *
     * @param appSecret 应用秘钥
     * @return 成功返回true，否则返回false
     */
    boolean update(AppSecret appSecret);

    /**
     * 删除应用秘钥
     *
     * @param id 秘钥ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(Integer id);

    /**
     * 是否存在应用秘钥
     *
     * @param accessKey Access key
     * @param secretKey Secret key
     * @return 存在返回true，否则返回false
     */
    boolean exists(String accessKey, String secretKey);

    /**
     * 生成随机AccessKey
     *
     * @return 随机AccessKey
     */
    String randomAccessKey();

    /**
     * 生成随机SecretKey
     *
     * @return 随机SecretKey
     */
    String randomSecretKey();

    /**
     * 根据ID获取应用秘钥
     *
     * @param id 秘钥ID
     * @return 应用秘钥
     */
    AppSecret get(Integer id);

    /**
     * 根据AccessKey和SecretKey获取秘钥
     *
     * @param accessKey AccessKey
     * @param secretKey SecretKey
     * @return 应用秘钥
     */
    AppSecret get(String accessKey, String secretKey);

    /**
     * 搜索应用秘钥
     *
     * @param searchRequest 搜索请求
     * @return 秘钥分页列表
     */
    Pager<AppSecret> search(AppSecretSearchRequest searchRequest);
}
