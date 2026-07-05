package com.sumit.fooddelivery.controller;

import com.sumit.fooddelivery.dto.request.AssignDeliveryPartnerRequest;
import com.sumit.fooddelivery.dto.request.OrderRequest;
import com.sumit.fooddelivery.dto.request.RejectOrderRequest;
import com.sumit.fooddelivery.dto.response.OrderResponse;
import com.sumit.fooddelivery.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request);
    }

    @GetMapping
    public List<OrderResponse> getAll() {
        return orderService.getAll();
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @PatchMapping("/{id}/accept")
    public OrderResponse accept(@PathVariable Long id) {
        return orderService.accept(id);
    }

    @PatchMapping("/{id}/reject")
    public OrderResponse reject(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) RejectOrderRequest request
    ) {
        String reason = request != null ? request.getReason() : null;
        return orderService.reject(id, reason);
    }

    @PatchMapping("/{id}/assign-partner")
    public OrderResponse assignDeliveryPartner(
            @PathVariable Long id,
            @Valid @RequestBody AssignDeliveryPartnerRequest request
    ) {
        return orderService.assignDeliveryPartner(id, request.getDeliveryPartnerId());
    }

    @PatchMapping("/{id}/claim")
    public OrderResponse claimDeliveryPartner(
            @PathVariable Long id,
            @Valid @RequestBody AssignDeliveryPartnerRequest request
    ) {
        return orderService.claimDeliveryPartner(id, request.getDeliveryPartnerId());
    }

    @PatchMapping("/{id}/preparing")
    public OrderResponse markPreparing(@PathVariable Long id) {
        return orderService.markPreparing(id);
    }

    @PatchMapping("/{id}/out-for-delivery")
    public OrderResponse markOutForDelivery(@PathVariable Long id) {
        return orderService.markOutForDelivery(id);
    }

    @PatchMapping("/{id}/delivered")
    public OrderResponse markDelivered(@PathVariable Long id) {
        return orderService.markDelivered(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        orderService.delete(id);
    }
}
