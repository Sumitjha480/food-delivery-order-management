package com.sumit.fooddelivery.security;

import com.sumit.fooddelivery.entity.Customer;
import com.sumit.fooddelivery.entity.DeliveryPartner;
import com.sumit.fooddelivery.entity.Restaurant;
import com.sumit.fooddelivery.entity.User;
import com.sumit.fooddelivery.enums.UserRole;
import com.sumit.fooddelivery.repository.CustomerRepository;
import com.sumit.fooddelivery.repository.DeliveryPartnerRepository;
import com.sumit.fooddelivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;

    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    public Customer getCurrentCustomer() {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != UserRole.CUSTOMER) {
            throw new AccessDeniedException("Current user is not a customer");
        }

        return customerRepository.findByUser_Username(currentUser.getUsername())
                .orElseThrow(() -> new AccessDeniedException("Customer profile not found for current user"));
    }

    public DeliveryPartner getCurrentDeliveryPartner() {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != UserRole.DELIVERY_PARTNER) {
            throw new AccessDeniedException("Current user is not a delivery partner");
        }

        return deliveryPartnerRepository.findByUser_Username(currentUser.getUsername())
                .orElseThrow(() -> new AccessDeniedException("Delivery partner profile not found for current user"));
    }

    public boolean hasRole(UserRole role) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        String expectedAuthority = "ROLE_" + role.name();

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals(expectedAuthority));
    }

    public void requireAdminOrRestaurantOwner(Restaurant restaurant) {

        if (hasRole(UserRole.ADMIN)) {
            return;
        }

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != UserRole.RESTAURANT_OWNER) {
            throw new AccessDeniedException("Only admin or restaurant owner can perform this action");
        }

        if (restaurant.getOwner() == null) {
            throw new AccessDeniedException("Restaurant does not have an owner configured");
        }

        if (!restaurant.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can manage only your own restaurant");
        }
    }

    public void requireAdminOrCustomer(Customer customer) {

        if (hasRole(UserRole.ADMIN)) {
            return;
        }

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != UserRole.CUSTOMER) {
            throw new AccessDeniedException("Only admin or customer can perform this action");
        }

        if (customer.getUser() == null) {
            throw new AccessDeniedException("Customer profile is not linked to a user");
        }

        if (!customer.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can access only your own customer profile");
        }
    }

    public void requireAdminOrDeliveryPartner(DeliveryPartner deliveryPartner) {

        if (hasRole(UserRole.ADMIN)) {
            return;
        }

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != UserRole.DELIVERY_PARTNER) {
            throw new AccessDeniedException("Only admin or delivery partner can perform this action");
        }

        if (deliveryPartner.getUser() == null) {
            throw new AccessDeniedException("Delivery partner profile is not linked to a user");
        }

        if (!deliveryPartner.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can act only as your own delivery partner profile");
        }
    }
}
