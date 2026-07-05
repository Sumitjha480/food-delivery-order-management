package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.dto.request.OrderRequest;
import com.sumit.fooddelivery.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse create(OrderRequest request);

    List<OrderResponse> getAll();

    OrderResponse getById(Long id);

    OrderResponse accept(Long id);

    OrderResponse reject(Long id, String reason);

    OrderResponse assignDeliveryPartner(Long orderId, Long deliveryPartnerId);

    OrderResponse claimDeliveryPartner(Long orderId, Long deliveryPartnerId);

    OrderResponse markPreparing(Long id);

    OrderResponse markOutForDelivery(Long id);

    OrderResponse markDelivered(Long id);

    void delete(Long id);
}
