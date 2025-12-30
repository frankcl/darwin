package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.SeedRecord;
import xin.manong.darwin.common.util.URLNormalizer;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.SeedMapper;
import xin.manong.darwin.service.iface.SeedService;
import xin.manong.darwin.service.request.SeedSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

import java.util.List;

/**
 * 种子服务实现
 *
 * @author frankcl
 * @date 2025-04-01 16:49:49
 */
@Service
public class SeedServiceImpl implements SeedService {

    @Resource
    private SeedMapper seedMapper;

    @Override
    public boolean add(SeedRecord record) {
        if (record.mustNormalize()) {
            String normalizedURL = URLNormalizer.normalize(record.url);
            record.setUrl(normalizedURL);
        }
        return seedMapper.insert(record) > 0;
    }

    @Override
    public boolean update(SeedRecord record) {
        SeedRecord prevRecord = seedMapper.selectById(record.key);
        if (prevRecord == null) throw new NotFoundException("种子记录不存在");
        if (record.url != null && record.mustNormalize()) {
            String normalizedURL = URLNormalizer.normalize(record.url);
            record.setUrl(normalizedURL);
        }
        LambdaUpdateWrapper<SeedRecord> query = new LambdaUpdateWrapper<SeedRecord>().
                eq(SeedRecord::getKey, record.key).set(SeedRecord::getLinkScope, record.linkScope);
        return seedMapper.update(record, query) > 0;
    }

    @Override
    public SeedRecord get(String key) {
        return seedMapper.selectById(key);
    }

    @Override
    public boolean delete(String key) {
        if (seedMapper.selectById(key) == null) throw new NotFoundException("种子记录不存在");
        return seedMapper.deleteById(key) > 0;
    }

    @Override
    public boolean deleteByPlan(String planId) {
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("计划ID为空");
        LambdaQueryWrapper<SeedRecord> query = new LambdaQueryWrapper<>();
        query.eq(SeedRecord::getPlanId, planId);
        if (seedMapper.selectCount(query) == 0) return true;
        return seedMapper.delete(query) > 0;
    }

    @Override
    public List<SeedRecord> getList(String planId) {
        LambdaQueryWrapper<SeedRecord> query = new LambdaQueryWrapper<>();
        query.eq(SeedRecord::getPlanId, planId);
        return seedMapper.selectList(query);
    }

    @Override
    public Pager<SeedRecord> search(SeedSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new SeedSearchRequest();
        if (searchRequest.pageNum == null || searchRequest.pageNum < 1) searchRequest.pageNum = Constants.DEFAULT_PAGE_NUM;
        if (searchRequest.pageSize == null || searchRequest.pageSize <= 0) searchRequest.pageSize = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(SeedRecord.class, searchRequest);
        QueryWrapper<SeedRecord> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (searchRequest.httpRequest != null) query.eq("http_request", searchRequest.httpRequest.name());
        if (StringUtils.isNotEmpty(searchRequest.planId)) query.eq("plan_id", searchRequest.planId);
        if (StringUtils.isNotEmpty(searchRequest.url)) query.eq("hash", DigestUtils.md5Hex(searchRequest.url));
        IPage<SeedRecord> page = seedMapper.selectPage(new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
        return Converter.convert(page);
    }
}
