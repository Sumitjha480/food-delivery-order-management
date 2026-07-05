package com.sumit.fooddelivery.config;

import com.sumit.fooddelivery.entity.*;
import com.sumit.fooddelivery.enums.CityStatus;
import com.sumit.fooddelivery.enums.DeliveryPartnerStatus;
import com.sumit.fooddelivery.enums.RestaurantStatus;
import com.sumit.fooddelivery.enums.UserRole;
import com.sumit.fooddelivery.repository.*;
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
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        createUserIfNotExists("admin", "admin123", UserRole.ADMIN);
        createUserIfNotExists("owner", "owner123", UserRole.RESTAURANT_OWNER);
        createUserIfNotExists("owner2", "owner2123", UserRole.RESTAURANT_OWNER);
        createUserIfNotExists("customer", "customer123", UserRole.CUSTOMER);
        createUserIfNotExists("partner", "partner123", UserRole.DELIVERY_PARTNER);
        createUserIfNotExists("partner2", "partner2123", UserRole.DELIVERY_PARTNER);
        User owner = userRepository.findByUsername("owner")
                .orElseThrow(() -> new IllegalStateException("Demo owner user not found"));
        User customerUser = userRepository.findByUsername("customer")
                .orElseThrow(() -> new IllegalStateException("Demo customer user not found"));
        User partnerUser = userRepository.findByUsername("partner")
                .orElseThrow(() -> new IllegalStateException("Demo partner user not found"));

        User partner2User = userRepository.findByUsername("partner2")
                .orElseThrow(() -> new IllegalStateException("Demo partner2 user not found"));


        City city = createCityIfNotExists("Delhi");

        createCustomerIfNotExists(customerUser);

        Restaurant restaurant = createRestaurantIfNotExists(city, owner);

        createMenuItemIfNotExists(restaurant);

        createDeliveryPartnerIfNotExists(city, partnerUser);
        createSecondDeliveryPartnerIfNotExists(city, partner2User);
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

    private City createCityIfNotExists(String cityName) {

        return cityRepository.findByNameIgnoreCase(cityName)
                .orElseGet(() -> {
                    City city = new City();
                    city.setName(cityName);
                    city.setStatus(CityStatus.ACTIVE);
                    return cityRepository.save(city);
                });
    }

    private Customer createCustomerIfNotExists(User customerUser) {

        return customerRepository.findAll()
                .stream()
                .filter(customer -> "customer@test.com".equals(customer.getEmail()))
                .findFirst()
                .map(existing -> {
                    if (existing.getUser() == null) {
                        existing.setUser(customerUser);
                        return customerRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setUser(customerUser);
                    customer.setName("Test Customer");
                    customer.setEmail("customer@test.com");
                    customer.setPhone("9999999999");
                    customer.setAddress("Delhi");
                    return customerRepository.save(customer);
                });
    }


    private Restaurant createRestaurantIfNotExists(City city, User owner) {

        return restaurantRepository.findAll()
                .stream()
                .filter(restaurant -> "Pizza Palace".equals(restaurant.getName()))
                .findFirst()
                .map(existing -> {
                    boolean changed = false;

                    if (existing.getCityEntity() == null) {
                        existing.setCityEntity(city);
                        existing.setCity(city.getName());
                        changed = true;
                    }

                    if (existing.getOwner() == null) {
                        existing.setOwner(owner);
                        changed = true;
                    }

                    return changed ? restaurantRepository.save(existing) : existing;
                })
                .orElseGet(() -> {
                    Restaurant restaurant = new Restaurant();
                    restaurant.setName("Pizza Palace");
                    restaurant.setAddress("Connaught Place");
                    restaurant.setCity(city.getName());
                    restaurant.setCityEntity(city);
                    restaurant.setOwner(owner);
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

    private void createDeliveryPartnerIfNotExists(City city, User partnerUser) {

        deliveryPartnerRepository.findAll()
                .stream()
                .filter(partner -> "Ravi Partner".equals(partner.getName()))
                .findFirst()
                .ifPresentOrElse(existing -> {
                    boolean changed = false;

                    if (existing.getCity() == null) {
                        existing.setCity(city);
                        changed = true;
                    }

                    if (existing.getUser() == null) {
                        existing.setUser(partnerUser);
                        changed = true;
                    }

                    if (changed) {
                        deliveryPartnerRepository.save(existing);
                    }
                }, () -> {
                    DeliveryPartner partner = new DeliveryPartner();
                    partner.setUser(partnerUser);
                    partner.setName("Ravi Partner");
                    partner.setPhone("9999990000");
                    partner.setCity(city);
                    partner.setStatus(DeliveryPartnerStatus.AVAILABLE);

                    deliveryPartnerRepository.save(partner);
                });
    }

    private void createSecondDeliveryPartnerIfNotExists(City city, User partner2User) {

        boolean alreadyExists = deliveryPartnerRepository.findAll()
                .stream()
                .anyMatch(partner -> "Second Partner".equals(partner.getName()));

        if (alreadyExists) {
            deliveryPartnerRepository.findAll()
                    .stream()
                    .filter(partner -> "Second Partner".equals(partner.getName()))
                    .filter(partner -> partner.getUser() == null)
                    .forEach(partner -> {
                        partner.setUser(partner2User);
                        partner.setCity(city);
                        deliveryPartnerRepository.save(partner);
                    });
            return;
        }

        DeliveryPartner partner = new DeliveryPartner();
        partner.setUser(partner2User);
        partner.setName("Second Partner");
        partner.setPhone("9999991111");
        partner.setCity(city);
        partner.setStatus(DeliveryPartnerStatus.AVAILABLE);

        deliveryPartnerRepository.save(partner);
    }
}
