package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.dto.request.CustomerRequest;
import com.sumit.fooddelivery.dto.request.CustomerUpdateRequest;
import com.sumit.fooddelivery.dto.response.CustomerResponse;
import com.sumit.fooddelivery.entity.Customer;
import com.sumit.fooddelivery.entity.User;
import com.sumit.fooddelivery.enums.UserRole;
import com.sumit.fooddelivery.repository.CustomerRepository;
import com.sumit.fooddelivery.repository.UserRepository;
import com.sumit.fooddelivery.security.CurrentUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public CustomerResponse create(CustomerRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("User must have CUSTOMER role");
        }

        if (customerRepository.existsByUser_Id(user.getId())) {
            throw new IllegalArgumentException("Customer profile already exists for username: " + user.getUsername());
        }

        if (customerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Customer already exists with email: " + request.getEmail());
        }

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        return map(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAll() {
        return customerRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {

        Customer customer = getCustomerOrThrow(id);

        currentUserService.requireAdminOrCustomer(customer);

        return map(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getMe() {

        Customer customer = currentUserService.getCurrentCustomer();

        return map(customer);
    }

    public CustomerResponse update(Long id, CustomerUpdateRequest request) {

        Customer customer = getCustomerOrThrow(id);

        currentUserService.requireAdminOrCustomer(customer);

        customerRepository.findByEmailIgnoreCase(request.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Customer already exists with email: " + request.getEmail());
                });

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        return map(customerRepository.save(customer));
    }

    public CustomerResponse updateMe(CustomerUpdateRequest request) {

        Customer customer = currentUserService.getCurrentCustomer();

        return update(customer.getId(), request);
    }

    public void delete(Long id) {

        Customer customer = getCustomerOrThrow(id);

        if (!currentUserService.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Only admin can delete customers");
        }

        customerRepository.delete(customer);
    }

    private Customer getCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
    }

    private CustomerResponse map(Customer customer) {

        User user = customer.getUser();

        return CustomerResponse.builder()
                .id(customer.getId())
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : null)
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .build();
    }
}
