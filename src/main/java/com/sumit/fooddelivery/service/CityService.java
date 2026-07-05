package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.dto.request.CityRequest;
import com.sumit.fooddelivery.dto.response.CityResponse;

import java.util.List;

public interface CityService {

    CityResponse create(CityRequest request);

    List<CityResponse> getAll();

    CityResponse getById(Long id);

    CityResponse update(Long id, CityRequest request);

    CityResponse activate(Long id);

    CityResponse deactivate(Long id);

    void delete(Long id);
}
