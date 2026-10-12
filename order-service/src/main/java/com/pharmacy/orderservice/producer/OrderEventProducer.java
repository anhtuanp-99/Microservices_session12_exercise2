package com.pharmacy.orderservice.producer;

import com.pharmacy.orderservice.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> orderEventKafkaTemplate;

    @Value("${app.kafka.topic.order-events}")
    private String topic;

    /**
     * Gửi event BẤT ĐỒNG BỘ — không chờ ACK.
     * Response trả về ngay, consumer xử lý sau.
     */
    public void sendAsync(OrderCreatedEvent event) {
        String key = event.getOrderCode();

        log.info("Gửi event (async): topic={}, key={}", topic, key);

        CompletableFuture<SendResult<String, OrderCreatedEvent>> future =
                orderEventKafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error(">>> Gửi thất bại: orderCode={}, lỗi={}",
                        event.getOrderCode(), ex.getMessage());
            } else {
                log.info("<<< Gửi thành công: partition={}, offset={}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}