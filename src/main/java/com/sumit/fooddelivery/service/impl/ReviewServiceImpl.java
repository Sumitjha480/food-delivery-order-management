package com.sumit.fooddelivery.service.impl;

import com.sumit.fooddelivery.dto.request.ReviewRequest;
import com.sumit.fooddelivery.dto.response.ReviewResponse;
import com.sumit.fooddelivery.entity.Order;
import com.sumit.fooddelivery.entity.Review;
import com.sumit.fooddelivery.enums.OrderStatus;
import com.sumit.fooddelivery.repository.OrderRepository;
import com.sumit.fooddelivery.repository.ReviewRepository;
import com.sumit.fooddelivery.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Override
    public ReviewResponse createForOrder(Long orderId, ReviewRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException(
                    "Review can be added only after order is DELIVERED. Current status is "
                            + order.getOrderStatus()
            );
        }

        if (reviewRepository.existsByOrder_Id(orderId)) {
            throw new IllegalArgumentException("Review already exists for this order");
        }

        Review review = new Review();
        review.setOrder(order);
        review.setCustomer(order.getCustomer());
        review.setRestaurant(order.getRestaurant());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return map(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAll() {
        return reviewRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getById(Long id) {
        return map(reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getByOrderId(Long orderId) {
        return map(reviewRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found for order")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getByRestaurantId(Long restaurantId) {
        return reviewRepository.findByRestaurant_IdOrderByCreatedAtDesc(restaurantId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getByCustomerId(Long customerId) {
        return reviewRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public ReviewResponse update(Long id, ReviewRequest request) {

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));

        if (review.getOrder().getOrderStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Only reviews for delivered orders can be updated");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return map(reviewRepository.save(review));
    }

    @Override
    public void delete(Long id) {

        if (!reviewRepository.existsById(id)) {
            throw new EntityNotFoundException("Review not found");
        }

        reviewRepository.deleteById(id);
    }

    private ReviewResponse map(Review review) {

        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrder().getId())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getName())
                .restaurantId(review.getRestaurant().getId())
                .restaurantName(review.getRestaurant().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
