package xin.manong.darwin.service.dao.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.List;

/**
 * MybatisPlus配置
 *
 * @author frankcl
 * @date 2022-08-15 15:35:20
 */
@Configuration
@EnableTransactionManagement
@MapperScan("xin.manong.darwin.service.dao.mapper*")
public class MybatisPlusConfig {

    /**
     * 创建MybatisPlus拦截器
     * 1. 分页拦截器
     * 2. 乐观锁拦截器
     *
     * @return 自定义拦截器
     */
    @Bean
    public MybatisPlusInterceptor buildInterceptor() {
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        paginationInterceptor.setMaxLimit(-1L);
        paginationInterceptor.setDbType(DbType.MYSQL);
        paginationInterceptor.setOptimizeJoin(true);
        OptimisticLockerInnerInterceptor optimisticLockerInterceptor = new OptimisticLockerInnerInterceptor();
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        List<InnerInterceptor> innerInterceptors = new ArrayList<>();
        innerInterceptors.add(paginationInterceptor);
        innerInterceptors.add(optimisticLockerInterceptor);
        interceptor.setInterceptors(innerInterceptors);
        return interceptor;
    }
}
