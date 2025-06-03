package com.greenbasket.api.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderRequest {
    @NotNull
    private Long addressId;

    @NotBlank
    private String paymentMethod;

    @NotBlank
    private String shippingMethod;

    @NotNull
    @Positive
    private Double shippingCost;

    private String couponCode;

    private String notes;
}
