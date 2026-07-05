package com.sumit.fooddelivery.controller;

import com.sumit.fooddelivery.dto.request.ReviewRequest;
import com.sumit.fooddelivery.dto.response.ReviewResponse;
import com.sumit.fooddelivery.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/orders/{orderId}/review")
    public ReviewResponse createForOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody ReviewRequest request
    ) {
        return reviewService.createForOrder(orderId, request);
    }

    @GetMapping("/orders/{orderId}/review")
    public ReviewResponse getByOrder(@PathVariable Long orderId) {
        return reviewService.getByOrderId(orderId);
    }

    @GetMapping("/reviews")
    public List<ReviewResponse> getAll() {
        return reviewService.getAll();
    }

    @GetMapping("/reviews/{id}")
    public ReviewResponse get(@PathVariable Long id) {
        return reviewService.getById(id);
    }

    @GetMapping("/restaurants/{restaurantId}/reviews")
    public List<ReviewResponse> getByRestaurant(@PathVariable Long restaurantId) {
        return reviewService.getByRestaurantId(restaurantId);
    }

    @GetMapping("/customers/{customerId}/reviews")
    public List<ReviewResponse> getByCustomer(@PathVariable Long customerId) {
        return reviewService.getByCustomerId(customerId);
    }

    @PutMapping("/reviews/{id}")
    public ReviewResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request
    ) {
        return reviewService.update(id, request);
    }

    @DeleteMapping("/reviews/{id}")
    public void delete(@PathVariable Long id) {
        reviewService.delete(id);
    }
}
