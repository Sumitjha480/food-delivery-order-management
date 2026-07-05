package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.dto.request.RestaurantRequest;
import com.sumit.fooddelivery.dto.response.RestaurantResponse;

import java.util.List;

public interface RestaurantService {

    RestaurantResponse createRestaurant(RestaurantRequest request);

    List<RestaurantResponse> getAllRestaurants();

    RestaurantResponse getRestaurant(Long id);

    RestaurantResponse updateRestaurant(Long id,
                                        RestaurantRequest request);

    void deleteRestaurant(Long id);

}