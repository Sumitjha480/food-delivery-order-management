package com.sumit.fooddelivery.service.impl;

import com.sumit.fooddelivery.dto.request.RestaurantRequest;
import com.sumit.fooddelivery.dto.response.RestaurantResponse;
import com.sumit.fooddelivery.entity.Restaurant;
import com.sumit.fooddelivery.enums.RestaurantStatus;
import com.sumit.fooddelivery.exception.ResourceNotFoundException;
import com.sumit.fooddelivery.repository.RestaurantRepository;
import com.sumit.fooddelivery.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Override
    public RestaurantResponse createRestaurant(RestaurantRequest request) {

        Restaurant restaurant = new Restaurant();

        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setEstimatedDeliveryTime(
                request.getEstimatedDeliveryTime());

        restaurant.setStatus(RestaurantStatus.ACTIVE);

        restaurant = restaurantRepository.save(restaurant);

        return mapToResponse(restaurant);
    }

    @Override
    public List<RestaurantResponse> getAllRestaurants() {

        return restaurantRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public RestaurantResponse getRestaurant(Long id) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Restaurant not found with id : " + id));

        return mapToResponse(restaurant);
    }

    @Override
    public RestaurantResponse updateRestaurant(Long id,
                                               RestaurantRequest request) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Restaurant not found with id : " + id));

        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setEstimatedDeliveryTime(
                request.getEstimatedDeliveryTime());

        restaurant = restaurantRepository.save(restaurant);

        return mapToResponse(restaurant);
    }

    @Override
    public void deleteRestaurant(Long id) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Restaurant not found with id : " + id));

        restaurantRepository.delete(restaurant);
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {

        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .city(restaurant.getCity())
                .estimatedDeliveryTime(
                        restaurant.getEstimatedDeliveryTime())
                .status(restaurant.getStatus())
                .build();
    }
}
