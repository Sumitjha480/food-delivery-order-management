package com.sumit.fooddelivery.config;

import com.sumit.fooddelivery.entity.Customer;
import com.sumit.fooddelivery.entity.DeliveryPartner;
import com.sumit.fooddelivery.entity.MenuItem;
import com.sumit.fooddelivery.entity.Restaurant;
import com.sumit.fooddelivery.entity.User;
import com.sumit.fooddelivery.enums.DeliveryPartnerStatus;
import com.sumit.fooddelivery.enums.RestaurantStatus;
import com.sumit.fooddelivery.enums.UserRole;
import com.sumit.fooddelivery.repository.CustomerRepository;
import com.sumit.fooddelivery.repository.DeliveryPartnerRepository;
import com.sumit.fooddelivery.repository.MenuItemRepository;
import com.sumit.fooddelivery.repository.RestaurantRepository;
import com.sumit.fooddelivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        createUserIfNotExists("admin", "admin123", UserRole.ADMIN);
        createUserIfNotExists("owner", "owner123", UserRole.RESTAURANT_OWNER);
        createUserIfNotExists("customer", "customer123", UserRole.CUSTOMER);
        createUserIfNotExists("partner", "partner123", UserRole.DELIVERY_PARTNER);

        Customer customer = createCustomerIfNotExists();

        Restaurant restaurant = createRestaurantIfNotExists();

        createMenuItemIfNotExists(restaurant);

        createDeliveryPartnerIfNotExists();
    }

    private void createUserIfNotExists(String username, String rawPassword, UserRole role) {

        if (userRepository.existsByUsername(username)) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        userRepository.save(user);
    }

    private Customer createCustomerIfNotExists() {

        return customerRepository.findAll()
                .stream()
                .filter(customer -> "customer@test.com".equals(customer.getEmail()))
                .findFirst()
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setName("Test Customer");
                    customer.setEmail("customer@test.com");
                    customer.setPhone("9999999999");
                    customer.setAddress("Delhi");
                    return customerRepository.save(customer);
                });
    }

    private Restaurant createRestaurantIfNotExists() {

        return restaurantRepository.findAll()
                .stream()
                .filter(restaurant -> "Pizza Palace".equals(restaurant.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Restaurant restaurant = new Restaurant();
                    restaurant.setName("Pizza Palace");
                    restaurant.setAddress("Connaught Place");
                    restaurant.setCity("Delhi");
                    restaurant.setStatus(RestaurantStatus.ACTIVE);
                    restaurant.setEstimatedDeliveryTime(30);
                    return restaurantRepository.save(restaurant);
                });
    }

    private void createMenuItemIfNotExists(Restaurant restaurant) {

        boolean alreadyExists = menuItemRepository.findByRestaurantId(restaurant.getId())
                .stream()
                .anyMatch(menuItem -> "Margherita Pizza".equals(menuItem.getName()));

        if (alreadyExists) {
            return;
        }

        MenuItem menuItem = new MenuItem();
        menuItem.setName("Margherita Pizza");
        menuItem.setPrice(BigDecimal.valueOf(299));
        menuItem.setStock(10);
        menuItem.setRestaurant(restaurant);

        menuItemRepository.save(menuItem);
    }

    private void createDeliveryPartnerIfNotExists() {

        boolean alreadyExists = deliveryPartnerRepository.findAll()
                .stream()
                .anyMatch(partner -> "Ravi Partner".equals(partner.getName()));

        if (alreadyExists) {
            return;
        }

        DeliveryPartner partner = new DeliveryPartner();
        partner.setName("Ravi Partner");
        partner.setStatus(DeliveryPartnerStatus.AVAILABLE);

        deliveryPartnerRepository.save(partner);
    }
}
