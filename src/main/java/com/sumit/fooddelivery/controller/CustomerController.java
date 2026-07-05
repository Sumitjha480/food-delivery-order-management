package com.sumit.fooddelivery.controller;

import com.sumit.fooddelivery.dto.request.CustomerRequest;
import com.sumit.fooddelivery.dto.request.CustomerUpdateRequest;
import com.sumit.fooddelivery.dto.response.CustomerResponse;
import com.sumit.fooddelivery.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(request);
    }

    @GetMapping
    public List<CustomerResponse> getAll() {
        return customerService.getAll();
    }

    @GetMapping("/me")
    public CustomerResponse getMe() {
        return customerService.getMe();
    }

    @PutMapping("/me")
    public CustomerResponse updateMe(@Valid @RequestBody CustomerUpdateRequest request) {
        return customerService.updateMe(request);
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return customerService.get(id);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }
}
