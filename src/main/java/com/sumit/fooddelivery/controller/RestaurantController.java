package com.sumit.fooddelivery.controller;

import com.sumit.fooddelivery.dto.request.RestaurantRequest;
import com.sumit.fooddelivery.dto.response.RestaurantResponse;
import com.sumit.fooddelivery.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Valid @RequestBody RestaurantRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.createRestaurant(request));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {

        return ResponseEntity.ok(
                restaurantService.getAllRestaurants()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurant(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                restaurantService.getRestaurant(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {

        return ResponseEntity.ok(
                restaurantService.updateRestaurant(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(
            @PathVariable Long id) {

        restaurantService.deleteRestaurant(id);

        return ResponseEntity.noContent().build();
    }

}
