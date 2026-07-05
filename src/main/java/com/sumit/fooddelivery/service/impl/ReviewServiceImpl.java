package com.sumit.fooddelivery.service.impl;

import com.sumit.fooddelivery.dto.request.ReviewRequest;
import com.sumit.fooddelivery.dto.response.ReviewResponse;
import com.sumit.fooddelivery.entity.Customer;
import com.sumit.fooddelivery.entity.Restaurant;
import com.sumit.fooddelivery.entity.Review;
import com.sumit.fooddelivery.repository.CustomerRepository;
import com.sumit.fooddelivery.repository.RestaurantRepository;
import com.sumit.fooddelivery.repository.ReviewRepository;
import com.sumit.fooddelivery.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    public ReviewResponse create(ReviewRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        Review review = new Review();
        review.setCustomer(customer);
        review.setRestaurant(restaurant);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return map(reviewRepository.save(review));
    }

    @Override
    public List<ReviewResponse> getAll() {
        return reviewRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public ReviewResponse getById(Long id) {

        return map(reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found")));
    }

    @Override
    public ReviewResponse update(Long id, ReviewRequest request) {

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        review.setCustomer(customer);
        review.setRestaurant(restaurant);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return map(reviewRepository.save(review));
    }

    @Override
    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }

    private ReviewResponse map(Review review) {

        return ReviewResponse.builder()
                .id(review.getId())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getName())
                .restaurantId(review.getRestaurant().getId())
                .restaurantName(review.getRestaurant().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .build();
    }
}