package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.entity.Customer;
import com.sumit.fooddelivery.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer create(Customer customer){
        return customerRepository.save(customer);
    }

    public List<Customer> getAll(){
        return customerRepository.findAll();
    }

    public Customer get(Long id){
        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Customer not found"));
    }

    public Customer update(Long id, Customer customer){

        Customer existing = get(id);

        existing.setName(customer.getName());
        existing.setEmail(customer.getEmail());
        existing.setPhone(customer.getPhone());
        existing.setAddress(customer.getAddress());

        return customerRepository.save(existing);
    }

    public void delete(Long id){
        customerRepository.deleteById(id);
    }

}