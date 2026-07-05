package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.dto.request.OrderRequest;
import com.sumit.fooddelivery.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse create(OrderRequest request);

    List<OrderResponse> getAll();

    OrderResponse getById(Long id);

    void delete(Long id);
}