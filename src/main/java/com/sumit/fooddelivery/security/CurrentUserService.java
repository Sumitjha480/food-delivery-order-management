package com.sumit.fooddelivery.security;

import com.sumit.fooddelivery.entity.Restaurant;
import com.sumit.fooddelivery.entity.User;
import com.sumit.fooddelivery.enums.UserRole;
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

    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
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
}
