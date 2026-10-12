package com.pharmacy.emailservice.service;

import com.pharmacy.emailservice.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.notification.email-enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    public void sendOrderConfirmation(OrderCreatedEvent event) {
        log.info("[EMAIL] Gửi xác nhận đơn {} tới {}",
                event.getOrderCode(), event.getCustomerEmail());

        if (!emailEnabled) {
            log.info("[EMAIL DISABLED] Nội dung: Đơn {} — {} × {} — Tổng: {} VND",
                    event.getOrderCode(), event.getMedicineName(),
                    event.getQuantity(), event.getTotalPrice());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(event.getCustomerEmail());
            message.setSubject("[Nhà thuốc] Xác nhận đơn hàng " + event.getOrderCode());
            message.setText("""
                    Cảm ơn quý khách đã đặt hàng.

                    Mã đơn     : %s
                    Thuốc      : %s
                    Số lượng   : %d
                    Tổng tiền  : %s VND
                    Thời gian  : %s

                    Chúng tôi sẽ liên hệ giao hàng sớm nhất.
                    """.formatted(
                    event.getOrderCode(),
                    event.getMedicineName(),
                    event.getQuantity(),
                    event.getTotalPrice(),
                    event.getCreatedAt()));

            mailSender.send(message);
            log.info("[EMAIL] ✅ Đã gửi tới {}", event.getCustomerEmail());

        } catch (Exception e) {
            log.error("[EMAIL] ❌ Lỗi: {}", e.getMessage(), e);
        }
    }
}