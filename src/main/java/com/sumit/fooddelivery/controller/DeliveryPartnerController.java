package com.sumit.fooddelivery.controller;

import com.sumit.fooddelivery.dto.request.DeliveryPartnerRequest;
import com.sumit.fooddelivery.dto.response.DeliveryPartnerResponse;
import com.sumit.fooddelivery.service.DeliveryPartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery-partners")
@RequiredArgsConstructor
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;

    @PostMapping
    public DeliveryPartnerResponse create(@Valid @RequestBody DeliveryPartnerRequest request) {
        return deliveryPartnerService.create(request);
    }

    @GetMapping
    public List<DeliveryPartnerResponse> getAll(
            @RequestParam(required = false) Long cityId
    ) {
        return deliveryPartnerService.getAll(cityId);
    }

    @GetMapping("/{id}")
    public DeliveryPartnerResponse get(@PathVariable Long id) {
        return deliveryPartnerService.get(id);
    }

    @PutMapping("/{id}")
    public DeliveryPartnerResponse update(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryPartnerRequest request
    ) {
        return deliveryPartnerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        deliveryPartnerService.delete(id);
    }
}
