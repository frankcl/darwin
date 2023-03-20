package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.dao.mapper.URLMapper;
import xin.manong.darwin.service.iface.URLService;

import javax.annotation.Resource;

/**
 * URL服务实现
 *
 * @author frankcl
 * @date 2023-03-20 20:02:44
 */
@Service
public class URLServiceImpl implements URLService {

    private static final Logger logger = LoggerFactory.getLogger(URLServiceImpl.class);

    @Resource
    protected URLMapper urlMapper;

    @Override
    public Boolean add(URLRecord record) {
        QueryWrapper<URLRecord> query = new QueryWrapper<>();
        query.lambda().eq(URLRecord::getKey, record.key);
        if (urlMapper.selectCount(query) > 0) {
            logger.error("record key[{}] has existed for url[{}]", record.key, record.url);
            throw new RuntimeException(String.format("URL记录key[%s]已存在", record.key));
        }
        return urlMapper.insert(record) > 0;
    }

    @Override
    public URLRecord get(String key) {
        if (StringUtils.isEmpty(key)) {
            logger.error("url record key is empty");
            throw new RuntimeException("URL记录key为空");
        }
        return urlMapper.selectById(key);
    }

    @Override
    public Boolean delete(String key) {
        if (urlMapper.selectById(key) == null) {
            logger.error("url record[{}] is not found", key);
            throw new RuntimeException(String.format("URL记录[%s]不存在", key));
        }
        return urlMapper.deleteById(key) > 0;
    }
}
