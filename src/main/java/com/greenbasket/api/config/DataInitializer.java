package com.greenbasket.api.config;

import com.greenbasket.api.model.*;
import com.greenbasket.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create admin user if not exists
        if (!userRepository.existsByEmail("admin@greenbasket.com")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@greenbasket.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);
        }

        // Create seller user if not exists
        if (!userRepository.existsByEmail("seller@greenbasket.com")) {
            User seller = new User();
            seller.setName("Seller User");
            seller.setEmail("seller@greenbasket.com");
            seller.setPassword(passwordEncoder.encode("seller123"));
            seller.setRole(UserRole.SELLER);
            userRepository.save(seller);
        }

        // Create customer user if not exists
        if (!userRepository.existsByEmail("customer@greenbasket.com")) {
            User customer = new User();
            customer.setName("Customer User");
            customer.setEmail("customer@greenbasket.com");
            customer.setPassword(passwordEncoder.encode("customer123"));
            customer.setRole(UserRole.CUSTOMER);
            userRepository.save(customer);
        }

        // Create categories if not exists
        if (categoryRepository.count() == 0) {
            Category vegetables = new Category();
            vegetables.setName("Vegetables");
            vegetables.setDescription("Fresh vegetables from local farms");

            Category fruits = new Category();
            fruits.setName("Fruits");
            fruits.setDescription("Fresh fruits from local farms");

            Category dairy = new Category();
            dairy.setName("Dairy");
            dairy.setDescription("Fresh dairy products");

            Category bakery = new Category();
            bakery.setName("Bakery");
            bakery.setDescription("Fresh baked goods");

            categoryRepository.saveAll(Arrays.asList(vegetables, fruits, dairy, bakery));
        }

        // Create sample products if not exists
        if (productRepository.count() == 0) {
            Category vegetables = categoryRepository.findByName("Vegetables").orElseThrow();
            Category fruits = categoryRepository.findByName("Fruits").orElseThrow();
            Category dairy = categoryRepository.findByName("Dairy").orElseThrow();
            Category bakery = categoryRepository.findByName("Bakery").orElseThrow();

            // Create a seller user
            User seller = new User();
            seller.setName("Local Farmer");
            seller.setEmail("farmer@greenbasket.com");
            seller.setPassword(passwordEncoder.encode("farmer123"));
            seller.setRole(UserRole.SELLER);
            seller = userRepository.save(seller);

            // Create products
            Product carrots = new Product();
            carrots.setName("Organic Carrots");
            carrots.setDescription("Fresh organic carrots from our local farm");
            carrots.setPrice(new BigDecimal("2.99"));
            carrots.setStockQuantity(100);
            carrots.setCategory(vegetables);
            carrots.setSeller(seller);
            carrots.setIsOrganic(true);
            carrots.setImageUrl("/placeholder.svg?height=200&width=200&text=Carrots");
            carrots.setNutrition(Map.of(
                "calories", 41,
                "protein", 0.9,
                "carbs", 9.6,
                "fiber", 2.8,
                "vitaminA", 428,
                "vitaminC", 5.9
            ));

            Product apples = new Product();
            apples.setName("Fresh Apples");
            apples.setDescription("Sweet and crisp apples from our orchard");
            apples.setPrice(new BigDecimal("3.99"));
            apples.setStockQuantity(150);
            apples.setCategory(fruits);
            apples.setSeller(seller);
            apples.setIsOrganic(true);
            apples.setImageUrl("/placeholder.svg?height=200&width=200&text=Apples");
            apples.setNutrition(Map.of(
                "calories", 95,
                "protein", 0.5,
                "carbs", 25,
                "fiber", 4.5,
                "vitaminC", 8.4
            ));

            Product milk = new Product();
            milk.setName("Organic Milk");
            milk.setDescription("Fresh organic milk from grass-fed cows");
            milk.setPrice(new BigDecimal("4.99"));
            milk.setStockQuantity(50);
            milk.setCategory(dairy);
            milk.setSeller(seller);
            milk.setIsOrganic(true);
            milk.setImageUrl("/placeholder.svg?height=200&width=200&text=Milk");
            milk.setNutrition(Map.of(
                "calories", 103,
                "protein", 8,
                "carbs", 12,
                "fat", 2.4,
                "calcium", 276,
                "vitaminD", 2.9
            ));

            Product bread = new Product();
            bread.setName("Artisan Bread");
            bread.setDescription("Freshly baked artisan bread");
            bread.setPrice(new BigDecimal("5.99"));
            bread.setStockQuantity(30);
            bread.setCategory(bakery);
            bread.setSeller(seller);
            bread.setIsOrganic(false);
            bread.setImageUrl("/placeholder.svg?height=200&width=200&text=Bread");
            bread.setNutrition(Map.of(
                "calories", 265,
                "protein", 9,
                "carbs", 49,
                "fiber", 2.7,
                "iron", 3.6
            ));

            Product organicTomatoes = new Product();
            organicTomatoes.setName("Organic Tomatoes");
            organicTomatoes.setDescription("Fresh organic tomatoes, locally grown");
            organicTomatoes.setPrice(new BigDecimal("2.99"));
            organicTomatoes.setStockQuantity(100);
            organicTomatoes.setCategory(vegetables);
            organicTomatoes.setImageUrl("https://example.com/tomatoes.jpg");
            organicTomatoes.setIsOrganic(true);
            organicTomatoes.setOrigin("Local Farm");
            organicTomatoes.setNutrition(Map.of(
                "calories", 22,
                "protein", 1.1,
                "carbohydrates", 4.8,
                "fiber", 1.2,
                "vitaminC", 21.0,
                "potassium", 237.0
            ));
            organicTomatoes.setSeller(seller);

            Product organicApples = new Product();
            organicApples.setName("Organic Apples");
            organicApples.setDescription("Sweet and crisp organic apples");
            organicApples.setPrice(new BigDecimal("3.99"));
            organicApples.setStockQuantity(150);
            organicApples.setCategory(fruits);
            organicApples.setImageUrl("https://example.com/apples.jpg");
            organicApples.setIsOrganic(true);
            organicApples.setOrigin("Local Orchard");
            organicApples.setNutrition(Map.of(
                "calories", 95,
                "protein", 0.5,
                "carbohydrates", 25.0,
                "fiber", 4.5,
                "vitaminC", 8.4,
                "potassium", 195.0
            ));
            organicApples.setSeller(seller);

            Product organicMilk = new Product();
            organicMilk.setName("Organic Whole Milk");
            organicMilk.setDescription("Creamy organic whole milk from grass-fed cows");
            organicMilk.setPrice(new BigDecimal("4.99"));
            organicMilk.setStockQuantity(50);
            organicMilk.setCategory(dairy);
            organicMilk.setImageUrl("https://example.com/milk.jpg");
            organicMilk.setIsOrganic(true);
            organicMilk.setOrigin("Local Dairy");
            organicMilk.setNutrition(Map.of(
                "calories", 150,
                "protein", 8.0,
                "carbohydrates", 12.0,
                "fat", 8.0,
                "calcium", 276.0,
                "vitaminD", 2.5
            ));
            organicMilk.setSeller(seller);

            productRepository.saveAll(Arrays.asList(carrots, apples, milk, bread, organicTomatoes, organicApples, organicMilk));
        }
    }
} 