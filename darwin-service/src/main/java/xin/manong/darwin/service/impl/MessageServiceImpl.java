package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.model.Message;
import xin.manong.darwin.service.dao.mapper.MessageMapper;
import xin.manong.darwin.service.iface.MessageService;

/**
 * 消息服务实现
 *
 * @author frankcl
 * @date 2025-03-09 20:13:47
 */
@Service
public class MessageServiceImpl implements MessageService {

    private static final int MAX_MESSAGE_NUM = 10;

    @Resource
    private MessageMapper messageMapper;


    @Override
    public Message pop(String sourceKey, int sourceType) {
        LambdaQueryWrapper<Message> query = new LambdaQueryWrapper<>();
        query.eq(Message::getSourceKey, sourceKey);
        query.eq(Message::getSourceType, sourceType);
        query.orderByAsc(Message::getCreateTime);
        Message message = messageMapper.selectOne(query, false);
        if (message != null) messageMapper.deleteById(message.getId());
        return message;
    }

    @Override
    public boolean push(Message message) {
        Long count = messageCount(message.sourceKey, message.sourceType);
        while (count >= MAX_MESSAGE_NUM) {
            if (pop(message.sourceKey, message.sourceType) == null) break;
            count = messageCount(message.sourceKey, message.sourceType);
        }
        return messageMapper.insert(message) > 0;
    }

    @Override
    public Long messageCount(String sourceKey, int sourceType) {
        LambdaQueryWrapper<Message> query = new LambdaQueryWrapper<>();
        query.eq(Message::getSourceKey, sourceKey);
        query.eq(Message::getSourceType, sourceType);
        return messageMapper.selectCount(query);
    }
}
