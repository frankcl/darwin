package xin.manong.darwin.service.dao.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * 创建时间及更新时间自动填充
 * 1. 创建时间create_time
 * 2. 更新时间update_time
 *
 * @author frankcl
 * @date 2022-08-16 13:29:16
 */
@Component
public class CreateUpdateTimeHandler implements MetaObjectHandler {

    private static final String FIELD_CREATE_TIME = "createTime";
    private static final String FIELD_UPDATE_TIME = "updateTime";

    @Override
    public void insertFill(MetaObject metaObject) {
        if (getFieldValByName(FIELD_CREATE_TIME, metaObject) == null) {
            setFieldValByName(FIELD_CREATE_TIME, System.currentTimeMillis(), metaObject);
        }
        setFieldValByName(FIELD_UPDATE_TIME, System.currentTimeMillis(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName(FIELD_UPDATE_TIME, System.currentTimeMillis(), metaObject);
    }
}
