package com.sumit.fooddelivery.controller;

import com.sumit.fooddelivery.dto.request.MenuItemRequest;
import com.sumit.fooddelivery.dto.response.MenuItemResponse;
import com.sumit.fooddelivery.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<MenuItemResponse> createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuItemService.createMenuItem(restaurantId, request));
    }

    @GetMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<List<MenuItemResponse>> getMenuItems(
            @PathVariable Long restaurantId) {

        return ResponseEntity.ok(
                menuItemService.getMenuItems(restaurantId));
    }

    @GetMapping("/menu-items/{id}")
    public ResponseEntity<MenuItemResponse> getMenuItem(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                menuItemService.getMenuItem(id));
    }

    @PutMapping("/menu-items/{id}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {

        return ResponseEntity.ok(
                menuItemService.updateMenuItem(id, request));
    }

    @DeleteMapping("/menu-items/{id}")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable Long id) {

        menuItemService.deleteMenuItem(id);

        return ResponseEntity.noContent().build();
    }
}