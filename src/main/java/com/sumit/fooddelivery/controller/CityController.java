package com.sumit.fooddelivery.controller;

import com.sumit.fooddelivery.dto.request.CityRequest;
import com.sumit.fooddelivery.dto.response.CityResponse;
import com.sumit.fooddelivery.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CityResponse create(@Valid @RequestBody CityRequest request) {
        return cityService.create(request);
    }

    @GetMapping
    public List<CityResponse> getAll() {
        return cityService.getAll();
    }

    @GetMapping("/{id}")
    public CityResponse getById(@PathVariable Long id) {
        return cityService.getById(id);
    }

    @PutMapping("/{id}")
    public CityResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CityRequest request
    ) {
        return cityService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    public CityResponse activate(@PathVariable Long id) {
        return cityService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public CityResponse deactivate(@PathVariable Long id) {
        return cityService.deactivate(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        cityService.delete(id);
    }
}
