package com.sumit.fooddelivery.service.impl;

import com.sumit.fooddelivery.dto.request.CityRequest;
import com.sumit.fooddelivery.dto.response.CityResponse;
import com.sumit.fooddelivery.entity.City;
import com.sumit.fooddelivery.enums.CityStatus;
import com.sumit.fooddelivery.repository.CityRepository;
import com.sumit.fooddelivery.service.CityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Override
    public CityResponse create(CityRequest request) {

        if (cityRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("City already exists with name: " + request.getName());
        }

        City city = new City();
        city.setName(request.getName().trim());
        city.setStatus(CityStatus.ACTIVE);

        return map(cityRepository.save(city));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityResponse> getAll() {
        return cityRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CityResponse getById(Long id) {
        return map(getCityOrThrow(id));
    }

    @Override
    public CityResponse update(Long id, CityRequest request) {

        City city = getCityOrThrow(id);

        cityRepository.findByNameIgnoreCase(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("City already exists with name: " + request.getName());
                });

        city.setName(request.getName().trim());

        return map(cityRepository.save(city));
    }

    @Override
    public CityResponse activate(Long id) {

        City city = getCityOrThrow(id);
        city.setStatus(CityStatus.ACTIVE);

        return map(cityRepository.save(city));
    }

    @Override
    public CityResponse deactivate(Long id) {

        City city = getCityOrThrow(id);
        city.setStatus(CityStatus.INACTIVE);

        return map(cityRepository.save(city));
    }

    @Override
    public void delete(Long id) {

        if (!cityRepository.existsById(id)) {
            throw new EntityNotFoundException("City not found");
        }

        cityRepository.deleteById(id);
    }

    private City getCityOrThrow(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("City not found"));
    }

    private CityResponse map(City city) {
        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .status(city.getStatus())
                .build();
    }
}
