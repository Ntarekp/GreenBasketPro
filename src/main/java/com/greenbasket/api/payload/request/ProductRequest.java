package com.greenbasket.api.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ProductRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    @Positive
    private Double price;

    @NotNull
    @Positive
    private Integer stock;

    @NotBlank
    private String category;

    private List<String> images = new ArrayList<>();

    private Boolean isOrganic = true;

    private String origin;

    private Map<String, Object> nutrition;
}
