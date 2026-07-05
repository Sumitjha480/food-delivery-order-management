package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.dto.request.DeliveryPartnerRequest;
import com.sumit.fooddelivery.dto.response.DeliveryPartnerResponse;
import com.sumit.fooddelivery.entity.City;
import com.sumit.fooddelivery.entity.DeliveryPartner;
import com.sumit.fooddelivery.entity.User;
import com.sumit.fooddelivery.enums.CityStatus;
import com.sumit.fooddelivery.enums.DeliveryPartnerStatus;
import com.sumit.fooddelivery.enums.UserRole;
import com.sumit.fooddelivery.repository.CityRepository;
import com.sumit.fooddelivery.repository.DeliveryPartnerRepository;
import com.sumit.fooddelivery.repository.UserRepository;
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
    private final UserRepository userRepository;

    public DeliveryPartnerResponse create(DeliveryPartnerRequest request) {

        City city = getActiveCityOrThrow(request.getCityId());
        User user = getDeliveryPartnerUserOrThrow(request.getUsername());

        if (deliveryPartnerRepository.existsByUser_Id(user.getId())) {
            throw new IllegalArgumentException(
                    "Delivery partner profile already exists for username: " + user.getUsername()
            );
        }

        DeliveryPartner partner = new DeliveryPartner();
        partner.setUser(user);
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
        User user = getDeliveryPartnerUserOrThrow(request.getUsername());

        deliveryPartnerRepository.findByUser_Username(user.getUsername())
                .filter(partner -> !partner.getId().equals(id))
                .ifPresent(partner -> {
                    throw new IllegalArgumentException(
                            "Another delivery partner already uses username: " + user.getUsername()
                    );
                });

        existing.setUser(user);
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

    private User getDeliveryPartnerUserOrThrow(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Delivery partner user not found"));

        if (user.getRole() != UserRole.DELIVERY_PARTNER) {
            throw new IllegalArgumentException("User must have DELIVERY_PARTNER role");
        }

        return user;
    }

    private DeliveryPartnerResponse map(DeliveryPartner partner) {

        City city = partner.getCity();
        User user = partner.getUser();

        return DeliveryPartnerResponse.builder()
                .id(partner.getId())
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : null)
                .name(partner.getName())
                .phone(partner.getPhone())
                .cityId(city != null ? city.getId() : null)
                .cityName(city != null ? city.getName() : null)
                .status(partner.getStatus())
                .build();
    }
}
