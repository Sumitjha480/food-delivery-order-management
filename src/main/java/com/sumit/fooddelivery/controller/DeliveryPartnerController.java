package com.sumit.fooddelivery.controller;

import com.sumit.fooddelivery.entity.DeliveryPartner;
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
    public DeliveryPartner create(@Valid @RequestBody DeliveryPartner partner) {
        return deliveryPartnerService.create(partner);
    }

    @GetMapping
    public List<DeliveryPartner> getAll() {
        return deliveryPartnerService.getAll();
    }

    @GetMapping("/{id}")
    public DeliveryPartner get(@PathVariable Long id) {
        return deliveryPartnerService.get(id);
    }

    @PutMapping("/{id}")
    public DeliveryPartner update(@PathVariable Long id,
                                  @Valid @RequestBody DeliveryPartner partner) {
        return deliveryPartnerService.update(id, partner);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        deliveryPartnerService.delete(id);
    }
}