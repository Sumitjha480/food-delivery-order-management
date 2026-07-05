package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.dto.request.MenuItemRequest;
import com.sumit.fooddelivery.dto.response.MenuItemResponse;

import java.util.List;

public interface MenuItemService {

    MenuItemResponse createMenuItem(Long restaurantId,
                                    MenuItemRequest request);

    List<MenuItemResponse> getMenuItems(Long restaurantId);

    MenuItemResponse getMenuItem(Long id);

    MenuItemResponse updateMenuItem(Long id,
                                    MenuItemRequest request);

    void deleteMenuItem(Long id);
}