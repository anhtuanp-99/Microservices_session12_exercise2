package com.pharmacy.emailservice.consumer;

import com.pharmacy.emailservice.event.OrderCreatedEvent;
import com.pharmacy.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "${app.kafka.topic.order-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "3"
    )
    public void onOrderCreated(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("┌─ [EMAIL-CONSUMER] partition={}, offset={}, orderCode={}",
                partition, offset, event.getOrderCode());

        try {
            emailService.sendOrderConfirmation(event);
            log.info("└─ [EMAIL] OK");
        } catch (Exception e) {
            log.error("└─ [EMAIL] Lỗi: {}", e.getMessage(), e);
        }
    }
}