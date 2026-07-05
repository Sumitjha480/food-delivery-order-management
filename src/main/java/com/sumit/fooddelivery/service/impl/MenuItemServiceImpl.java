package com.sumit.fooddelivery.service.impl;

import com.sumit.fooddelivery.dto.request.MenuItemRequest;
import com.sumit.fooddelivery.dto.response.MenuItemResponse;
import com.sumit.fooddelivery.entity.MenuItem;
import com.sumit.fooddelivery.entity.Restaurant;
import com.sumit.fooddelivery.exception.ResourceNotFoundException;
import com.sumit.fooddelivery.repository.MenuItemRepository;
import com.sumit.fooddelivery.repository.RestaurantRepository;
import com.sumit.fooddelivery.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    public MenuItemResponse createMenuItem(Long restaurantId,
                                           MenuItemRequest request) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Restaurant not found with id : " + restaurantId));

        MenuItem menuItem = new MenuItem();

        menuItem.setName(request.getName());
        menuItem.setPrice(request.getPrice());
        menuItem.setStock(request.getStock());
        menuItem.setRestaurant(restaurant);

        menuItem = menuItemRepository.save(menuItem);

        return mapToResponse(menuItem);
    }

    @Override
    public List<MenuItemResponse> getMenuItems(Long restaurantId) {

        return menuItemRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public MenuItemResponse getMenuItem(Long id) {

        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Menu item not found with id : " + id));

        return mapToResponse(menuItem);
    }

    @Override
    public MenuItemResponse updateMenuItem(Long id,
                                           MenuItemRequest request) {

        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Menu item not found with id : " + id));

        menuItem.setName(request.getName());
        menuItem.setPrice(request.getPrice());
        menuItem.setStock(request.getStock());

        menuItem = menuItemRepository.save(menuItem);

        return mapToResponse(menuItem);
    }

    @Override
    public void deleteMenuItem(Long id) {

        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Menu item not found with id : " + id));

        menuItemRepository.delete(menuItem);
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {

        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .price(menuItem.getPrice())
                .stock(menuItem.getStock())
                .restaurantId(menuItem.getRestaurant().getId())
                .build();
    }
}