package com.greenbasket.api.payload.response;

import com.greenbasket.api.model.Category;
import com.greenbasket.api.model.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private CategoryResponse category;
    private String imageUrl;
    private Boolean isOrganic;
    private String origin;
    private Map<String, Object> nutrition;
    private Double rating;

    public static ProductResponse fromProduct(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setCategory(CategoryResponse.fromCategory(product.getCategory()));
        response.setImageUrl(product.getImageUrl());
        response.setIsOrganic(product.getIsOrganic());
        response.setOrigin(product.getOrigin());
        response.setNutrition(product.getNutrition());
        response.setRating(product.getRating());
        return response;
    }
}

@Data
class CategoryResponse {
    private Long id;
    private String name;
    private String description;

    public static CategoryResponse fromCategory(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        return response;
    }
} 