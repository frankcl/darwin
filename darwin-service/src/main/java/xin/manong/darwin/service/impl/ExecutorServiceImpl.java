package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.ws.rs.NotFoundException;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.model.Executor;
import xin.manong.darwin.service.dao.mapper.ExecutorMapper;
import xin.manong.darwin.service.iface.ExecutorService;

import java.util.List;

/**
 * 执行器服务实现
 *
 * @author frankcl
 * @date 2025-03-09 20:13:47
 */
@Service
public class ExecutorServiceImpl implements ExecutorService {

    @Resource
    private ExecutorMapper executorMapper;

    @Override
    public Executor get(String name) {
        LambdaQueryWrapper<Executor> query = new LambdaQueryWrapper<>();
        query.eq(Executor::getName, name);
        return executorMapper.selectOne(query);
    }

    @Override
    public boolean add(Executor executor) {
        if (get(executor.getName()) != null) throw new IllegalStateException("执行器已存在");
        return executorMapper.insert(executor) > 0;
    }

    @Override
    public boolean update(Executor executor) {
        if (executorMapper.selectById(executor.getId()) == null) throw new NotFoundException("执行器不存在");
        return executorMapper.updateById(executor) > 0;
    }

    @Override
    public boolean updateByName(String name, Executor executor) {
        Executor prev = get(name);
        if (prev == null) throw new NotFoundException("执行器不存在");
        executor.id = prev.id;
        return update(executor);
    }

    @Override
    public List<Executor> getList() {
        LambdaQueryWrapper<Executor> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Executor::getCreateTime);
        return executorMapper.selectList(query);
    }
}
