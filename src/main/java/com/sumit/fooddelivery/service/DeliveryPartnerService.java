package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.entity.DeliveryPartner;
import com.sumit.fooddelivery.repository.DeliveryPartnerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;

    public DeliveryPartner create(DeliveryPartner partner) {
        return deliveryPartnerRepository.save(partner);
    }

    public List<DeliveryPartner> getAll() {
        return deliveryPartnerRepository.findAll();
    }

    public DeliveryPartner get(Long id) {
        return deliveryPartnerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Delivery Partner not found"));
    }

    public DeliveryPartner update(Long id, DeliveryPartner partner) {

        DeliveryPartner existing = get(id);

        existing.setName(partner.getName());
        existing.setStatus(partner.getStatus());

        return deliveryPartnerRepository.save(existing);
    }

    public void delete(Long id) {
        deliveryPartnerRepository.deleteById(id);
    }
}