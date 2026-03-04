package info.jemsit.media_service.service;

import info.jemsit.common.dto.message.RabbitMQMessage;

public interface RabbitMQService {
       <T extends RabbitMQMessage>  void sendMessageToRabbitMQ(T message);
}
