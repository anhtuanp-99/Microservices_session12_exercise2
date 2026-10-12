package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.dto.request.CreateOrderRequest;
import com.pharmacy.orderservice.dto.response.OrderResponse;
import com.pharmacy.orderservice.entity.Order;
import com.pharmacy.orderservice.event.OrderCreatedEvent;
import com.pharmacy.orderservice.producer.OrderEventProducer;
import com.pharmacy.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    /**
     * LUỒNG:
     *   1. Lưu order vào DB
     *   2. Gửi event async → response NGAY
     *   3. Consumer xử lý sau (trừ kho, gửi mail)
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = Order.builder()
                .orderCode(orderCode)
                .customerEmail(request.getCustomerEmail())
                .medicineId(request.getMedicineId())
                .medicineName(request.getMedicineName())
                .quantity(request.getQuantity())
                .totalPrice(request.getTotalPrice())
                .status("PENDING")
                .build();

        Order saved = orderRepository.save(order);
        log.info("Đã lưu order: code={}, id={}", orderCode, saved.getId());

        // Gửi event bất đồng bộ
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderCode(orderCode)
                .customerEmail(request.getCustomerEmail())
                .medicineId(request.getMedicineId())
                .medicineName(request.getMedicineName())
                .quantity(request.getQuantity())
                .totalPrice(request.getTotalPrice())
                .createdAt(saved.getCreatedAt())
                .build();

        orderEventProducer.sendAsync(event);

        // Response trả ngay — không chờ consumer
        return OrderResponse.builder()
                .id(saved.getId())
                .orderCode(orderCode)
                .customerEmail(saved.getCustomerEmail())
                .medicineId(saved.getMedicineId())
                .medicineName(saved.getMedicineName())
                .quantity(saved.getQuantity())
                .totalPrice(saved.getTotalPrice())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}