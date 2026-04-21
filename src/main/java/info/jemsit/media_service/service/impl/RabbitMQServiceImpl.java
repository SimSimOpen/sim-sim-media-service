package info.jemsit.media_service.service.impl;

import info.jemsit.common.data.enums.RabbitMQMessages;
import info.jemsit.common.dto.message.RabbitMQMessage;
import info.jemsit.media_service.service.RabbitMQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static info.jemsit.common.data.constants.RabbitMQConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQServiceImpl implements RabbitMQService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public <T extends RabbitMQMessage> void sendMessageToRabbitMQ(T message) {
        if(message.getMessage() != null) {
            log.info("Sending message to RabbitMQ: {}", message.getMessageString());
        } else {
            log.info("Sending message to RabbitMQ: {}", message.getUserAvatarUrl());
        }
        rabbitTemplate.convertAndSend(
                MEDIA_EXCHANGE,
                MEDIA_KEY,
                message
        );
    }
}
