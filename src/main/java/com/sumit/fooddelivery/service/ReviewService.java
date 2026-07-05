package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.dto.request.ReviewRequest;
import com.sumit.fooddelivery.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse createForOrder(Long orderId, ReviewRequest request);

    List<ReviewResponse> getAll();

    ReviewResponse getById(Long id);

    ReviewResponse getByOrderId(Long orderId);

    List<ReviewResponse> getByRestaurantId(Long restaurantId);

    List<ReviewResponse> getByCustomerId(Long customerId);

    ReviewResponse update(Long id, ReviewRequest request);

    void delete(Long id);
}
