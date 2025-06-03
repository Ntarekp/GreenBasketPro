package com.greenbasket.api.controller;

import com.greenbasket.api.model.Category;
import com.greenbasket.api.model.Product;
import com.greenbasket.api.model.ProductReview;
import com.greenbasket.api.model.User;
import com.greenbasket.api.payload.request.ProductRequest;
import com.greenbasket.api.payload.request.ReviewRequest;
import com.greenbasket.api.payload.response.MessageResponse;
import com.greenbasket.api.payload.response.ProductResponse;
import com.greenbasket.api.repository.CategoryRepository;
import com.greenbasket.api.repository.ProductRepository;
import com.greenbasket.api.repository.UserRepository;
import com.greenbasket.api.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProductController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isOrganic,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Double minPrice,
            @RequestParam(defaultValue = "1000000") Double maxPrice,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable paging = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<Product> pageProducts;

            if (search != null && !search.isEmpty()) {
                pageProducts = productRepository.search(search, paging);
            } else if (category != null && !category.isEmpty()) {
                pageProducts = productRepository.findByCategory(category, paging);
            } else if (isOrganic != null) {
                pageProducts = productRepository.findByIsOrganic(isOrganic, paging);
            } else if (minPrice != null && maxPrice != null) {
                pageProducts = productRepository.findByPriceRange(minPrice, maxPrice, paging);
            } else {
                pageProducts = productRepository.findAll(paging);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("products", pageProducts.getContent().stream()
                    .map(ProductResponse::fromProduct)
                    .collect(Collectors.toList()));
            response.put("currentPage", pageProducts.getNumber());
            response.put("totalItems", pageProducts.getTotalElements());
            response.put("totalPages", pageProducts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(ProductResponse.fromProduct(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            Optional<User> userOptional = userRepository.findById(userDetails.getId());
            
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found"));
            }
            
            User user = userOptional.get();
            
            Product product = new Product();
            product.setName(productRequest.getName());
            product.setDescription(productRequest.getDescription());
            product.setPrice(BigDecimal.valueOf(productRequest.getPrice()));
            product.setStockQuantity(productRequest.getStock());
            
            // Find or create category
            Category category = categoryRepository.findByName(productRequest.getCategory())
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(productRequest.getCategory());
                    return categoryRepository.save(newCategory);
                });
            product.setCategory(category);
            
            // Set first image as main image if available
            if (!productRequest.getImages().isEmpty()) {
                product.setImageUrl(productRequest.getImages().get(0));
            }
            
            product.setIsOrganic(productRequest.getIsOrganic());
            product.setOrigin(productRequest.getOrigin());
            product.setNutrition(productRequest.getNutrition());
            product.setSeller(user);
            
            Product savedProduct = productRepository.save(product);
            
            return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable("id") Long id, @Valid @RequestBody ProductRequest productRequest) {
        Optional<Product> productData = productRepository.findById(id);

        if (productData.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            Product product = productData.get();
            
            // Check if the user is the seller of the product or an admin
            if (!product.getSeller().getId().equals(userDetails.getId()) && 
                !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("You are not authorized to update this product"));
            }
            
            product.setName(productRequest.getName());
            product.setDescription(productRequest.getDescription());
            product.setPrice(BigDecimal.valueOf(productRequest.getPrice()));
            product.setStockQuantity(productRequest.getStock());
            
            // Find or create category
            Category category = categoryRepository.findByName(productRequest.getCategory())
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(productRequest.getCategory());
                    return categoryRepository.save(newCategory);
                });
            product.setCategory(category);
            
            // Set first image as main image if available
            if (!productRequest.getImages().isEmpty()) {
                product.setImageUrl(productRequest.getImages().get(0));
            }
            
            product.setIsOrganic(productRequest.getIsOrganic());
            product.setOrigin(productRequest.getOrigin());
            product.setNutrition(productRequest.getNutrition());
            
            return new ResponseEntity<>(productRepository.save(product), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") Long id) {
        try {
            Optional<Product> productData = productRepository.findById(id);
            
            if (!productData.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            Product product = productData.get();
            
            // Check if the user is the seller of the product or an admin
            if (!product.getSeller().getId().equals(userDetails.getId()) && 
                !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("You are not authorized to delete this product"));
            }
            
            productRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/reviews")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> addReview(@PathVariable("id") Long id, @Valid @RequestBody ReviewRequest reviewRequest) {
        Optional<Product> productData = productRepository.findById(id);

        if (productData.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            Product product = productData.get();
            
            ProductReview review = new ProductReview();
            review.setRating(reviewRequest.getRating());
            review.setComment(reviewRequest.getComment());
            review.setProduct(product);
            review.setUserId(userDetails.getId());
            
            product.getReviews().add(review);
            
            // Update product rating
            double totalRating = 0;
            for (ProductReview r : product.getReviews()) {
                totalRating += r.getRating();
            }
            product.setRating(totalRating / product.getReviews().size());
            
            productRepository.save(product);
            
            return new ResponseEntity<>(product, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
