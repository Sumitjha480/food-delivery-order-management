package com.sumit.fooddelivery.service.impl;

import com.sumit.fooddelivery.dto.request.RestaurantRequest;
import com.sumit.fooddelivery.dto.response.RestaurantResponse;
import com.sumit.fooddelivery.entity.City;
import com.sumit.fooddelivery.entity.Restaurant;
import com.sumit.fooddelivery.enums.CityStatus;
import com.sumit.fooddelivery.enums.RestaurantStatus;
import com.sumit.fooddelivery.exception.ResourceNotFoundException;
import com.sumit.fooddelivery.repository.CityRepository;
import com.sumit.fooddelivery.repository.RestaurantRepository;
import com.sumit.fooddelivery.service.RestaurantService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CityRepository cityRepository;

    @Override
    public RestaurantResponse createRestaurant(RestaurantRequest request) {

        City city = getActiveCityOrThrow(request.getCityId());

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(city.getName());
        restaurant.setCityEntity(city);
        restaurant.setEstimatedDeliveryTime(request.getEstimatedDeliveryTime());
        restaurant.setStatus(RestaurantStatus.ACTIVE);

        return mapToResponse(restaurantRepository.save(restaurant));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getRestaurantsByCity(Long cityId) {
        return restaurantRepository.findByCityEntity_Id(cityId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurant(Long id) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Restaurant not found with id : " + id));

        return mapToResponse(restaurant);
    }

    @Override
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Restaurant not found with id : " + id));

        City city = getActiveCityOrThrow(request.getCityId());

        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(city.getName());
        restaurant.setCityEntity(city);
        restaurant.setEstimatedDeliveryTime(request.getEstimatedDeliveryTime());

        return mapToResponse(restaurantRepository.save(restaurant));
    }

    @Override
    public void deleteRestaurant(Long id) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Restaurant not found with id : " + id));

        restaurantRepository.delete(restaurant);
    }

    private City getActiveCityOrThrow(Long cityId) {

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new EntityNotFoundException("City not found"));

        if (city.getStatus() != CityStatus.ACTIVE) {
            throw new IllegalArgumentException("City is not active: " + city.getName());
        }

        return city;
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {

        City city = restaurant.getCityEntity();

        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .cityId(city != null ? city.getId() : null)
                .cityName(city != null ? city.getName() : restaurant.getCity())
                .estimatedDeliveryTime(restaurant.getEstimatedDeliveryTime())
                .status(restaurant.getStatus())
                .build();
    }
}
