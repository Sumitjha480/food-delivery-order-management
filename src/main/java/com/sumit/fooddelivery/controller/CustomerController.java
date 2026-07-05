package com.sumit.fooddelivery.controller;

import com.sumit.fooddelivery.entity.Customer;
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
    public Customer create(@Valid @RequestBody Customer customer){
        return customerService.create(customer);
    }

    @GetMapping
    public List<Customer> getAll(){
        return customerService.getAll();
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable Long id){
        return customerService.get(id);
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id,
                           @Valid @RequestBody Customer customer){
        return customerService.update(id, customer);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        customerService.delete(id);
    }
}