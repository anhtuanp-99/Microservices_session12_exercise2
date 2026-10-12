package com.pharmacy.orderservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String customerEmail;

    @NotNull(message = "medicineId không được để trống")
    @Min(value = 1)
    private Long medicineId;

    @NotBlank(message = "Tên thuốc không được để trống")
    private String medicineName;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1)
    private Integer quantity;

    @NotNull(message = "Tổng tiền không được để trống")
    @Min(value = 1)
    private BigDecimal totalPrice;
}