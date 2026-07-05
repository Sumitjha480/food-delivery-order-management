package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.dto.request.ReviewRequest;
import com.sumit.fooddelivery.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse create(ReviewRequest request);

    List<ReviewResponse> getAll();

    ReviewResponse getById(Long id);

    ReviewResponse update(Long id, ReviewRequest request);

    void delete(Long id);
}