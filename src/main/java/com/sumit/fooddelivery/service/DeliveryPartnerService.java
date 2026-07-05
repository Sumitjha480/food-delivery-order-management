package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.dto.request.DeliveryPartnerRequest;
import com.sumit.fooddelivery.dto.response.DeliveryPartnerResponse;
import com.sumit.fooddelivery.entity.City;
import com.sumit.fooddelivery.entity.DeliveryPartner;
import com.sumit.fooddelivery.enums.CityStatus;
import com.sumit.fooddelivery.enums.DeliveryPartnerStatus;
import com.sumit.fooddelivery.repository.CityRepository;
import com.sumit.fooddelivery.repository.DeliveryPartnerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final CityRepository cityRepository;

    public DeliveryPartnerResponse create(DeliveryPartnerRequest request) {

        City city = getActiveCityOrThrow(request.getCityId());

        DeliveryPartner partner = new DeliveryPartner();
        partner.setName(request.getName());
        partner.setPhone(request.getPhone());
        partner.setCity(city);
        partner.setStatus(request.getStatus() != null
                ? request.getStatus()
                : DeliveryPartnerStatus.AVAILABLE);

        return map(deliveryPartnerRepository.save(partner));
    }

    @Transactional(readOnly = true)
    public List<DeliveryPartnerResponse> getAll(Long cityId) {

        List<DeliveryPartner> partners = cityId != null
                ? deliveryPartnerRepository.findByCity_Id(cityId)
                : deliveryPartnerRepository.findAll();

        return partners.stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeliveryPartnerResponse get(Long id) {
        return map(getPartnerOrThrow(id));
    }

    public DeliveryPartnerResponse update(Long id, DeliveryPartnerRequest request) {

        DeliveryPartner existing = getPartnerOrThrow(id);
        City city = getActiveCityOrThrow(request.getCityId());

        existing.setName(request.getName());
        existing.setPhone(request.getPhone());
        existing.setCity(city);

        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

        return map(deliveryPartnerRepository.save(existing));
    }

    public void delete(Long id) {

        if (!deliveryPartnerRepository.existsById(id)) {
            throw new EntityNotFoundException("Delivery partner not found");
        }

        deliveryPartnerRepository.deleteById(id);
    }

    private DeliveryPartner getPartnerOrThrow(Long id) {
        return deliveryPartnerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Delivery partner not found"));
    }

    private City getActiveCityOrThrow(Long cityId) {

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new EntityNotFoundException("City not found"));

        if (city.getStatus() != CityStatus.ACTIVE) {
            throw new IllegalArgumentException("City is not active: " + city.getName());
        }

        return city;
    }

    private DeliveryPartnerResponse map(DeliveryPartner partner) {

        City city = partner.getCity();

        return DeliveryPartnerResponse.builder()
                .id(partner.getId())
                .name(partner.getName())
                .phone(partner.getPhone())
                .cityId(city != null ? city.getId() : null)
                .cityName(city != null ? city.getName() : null)
                .status(partner.getStatus())
                .build();
    }
}
