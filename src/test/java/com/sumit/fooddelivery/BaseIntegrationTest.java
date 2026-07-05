package com.sumit.fooddelivery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumit.fooddelivery.entity.*;
import com.sumit.fooddelivery.enums.*;
import com.sumit.fooddelivery.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected CityRepository cityRepository;

    @Autowired
    protected CustomerRepository customerRepository;

    @Autowired
    protected RestaurantRepository restaurantRepository;

    @Autowired
    protected MenuItemRepository menuItemRepository;

    @Autowired
    protected DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected OrderItemRepository orderItemRepository;

    @Autowired
    protected ReviewRepository reviewRepository;

    @Autowired
    protected NotificationRepository notificationRepository;

    @Autowired
    protected OrderStatusHistoryRepository orderStatusHistoryRepository;

    protected TestData data;

    @BeforeEach
    void baseSetUp() {
        SecurityContextHolder.clearContext();
        cleanDatabase();
        data = createTestData();
    }

    @AfterEach
    void baseTearDown() {
        SecurityContextHolder.clearContext();
    }

    protected void cleanDatabase() {
        orderStatusHistoryRepository.deleteAll();
        notificationRepository.deleteAll();
        reviewRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        menuItemRepository.deleteAll();
        deliveryPartnerRepository.deleteAll();
        restaurantRepository.deleteAll();
        customerRepository.deleteAll();
        cityRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected TestData createTestData() {
        TestData testData = new TestData();

        testData.adminUser = createUser("admin", "admin123", UserRole.ADMIN);
        testData.ownerUser = createUser("owner", "owner123", UserRole.RESTAURANT_OWNER);
        testData.owner2User = createUser("owner2", "owner2123", UserRole.RESTAURANT_OWNER);
        testData.customerUser = createUser("customer", "customer123", UserRole.CUSTOMER);
        testData.customer2User = createUser("customer2", "customer2123", UserRole.CUSTOMER);
        testData.partnerUser = createUser("partner", "partner123", UserRole.DELIVERY_PARTNER);
        testData.partner2User = createUser("partner2", "partner2123", UserRole.DELIVERY_PARTNER);

        testData.delhi = createCity("Delhi");
        testData.mumbai = createCity("Mumbai");

        testData.customer = createCustomer(
                testData.customerUser,
                "Test Customer",
                "customer-" + UUID.randomUUID() + "@test.com",
                "9999999999",
                "Delhi"
        );

        testData.customer2 = createCustomer(
                testData.customer2User,
                "Second Customer",
                "customer2-" + UUID.randomUUID() + "@test.com",
                "9999999998",
                "Delhi"
        );

        testData.restaurant = createRestaurant(
                "Pizza Palace",
                testData.delhi,
                testData.ownerUser
        );

        testData.owner2Restaurant = createRestaurant(
                "Owner Two Restaurant",
                testData.delhi,
                testData.owner2User
        );

        testData.pizza = createMenuItem(
                testData.restaurant,
                "Margherita Pizza",
                BigDecimal.valueOf(299),
                10
        );

        testData.owner2Burger = createMenuItem(
                testData.owner2Restaurant,
                "Owner Two Burger",
                BigDecimal.valueOf(199),
                10
        );

        testData.partner = createDeliveryPartner(
                testData.partnerUser,
                "Ravi Partner",
                testData.delhi
        );

        testData.partner2 = createDeliveryPartner(
                testData.partner2User,
                "Second Partner",
                testData.delhi
        );

        return testData;
    }

    protected User createUser(String username, String rawPassword, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }

    protected City createCity(String name) {
        City city = new City();
        city.setName(name);
        city.setStatus(CityStatus.ACTIVE);
        return cityRepository.save(city);
    }

    protected Customer createCustomer(
            User user,
            String name,
            String email,
            String phone,
            String address
    ) {
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);
        return customerRepository.save(customer);
    }

    protected Restaurant createRestaurant(String name, City city, User owner) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(name + " Address");
        restaurant.setCity(city.getName());
        restaurant.setCityEntity(city);
        restaurant.setOwner(owner);
        restaurant.setStatus(RestaurantStatus.ACTIVE);
        restaurant.setEstimatedDeliveryTime(30);
        return restaurantRepository.save(restaurant);
    }

    protected MenuItem createMenuItem(
            Restaurant restaurant,
            String name,
            BigDecimal price,
            Integer stock
    ) {
        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurant(restaurant);
        menuItem.setName(name);
        menuItem.setPrice(price);
        menuItem.setStock(stock);
        return menuItemRepository.save(menuItem);
    }

    protected DeliveryPartner createDeliveryPartner(User user, String name, City city) {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setUser(user);
        partner.setName(name);
        partner.setPhone("9999990000");
        partner.setCity(city);
        partner.setStatus(DeliveryPartnerStatus.AVAILABLE);
        return deliveryPartnerRepository.save(partner);
    }

    protected String basicAuth(String username, String password) {
        String raw = username + ":" + password;
        return "Basic " + Base64.getEncoder()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    protected String createOrderJson(
            Long customerId,
            Long restaurantId,
            Long menuItemId,
            int quantity,
            String paymentToken
    ) {
        return """
                {
                  "customerId": %d,
                  "restaurantId": %d,
                  "items": [
                    {
                      "menuItemId": %d,
                      "quantity": %d
                    }
                  ],
                  "payment": {
                    "method": "UPI",
                    "token": "%s"
                  }
                }
                """.formatted(customerId, restaurantId, menuItemId, quantity, paymentToken);
    }

    protected String createDuplicateItemOrderJson(
            Long customerId,
            Long restaurantId,
            Long menuItemId,
            int quantityOne,
            int quantityTwo
    ) {
        return """
                {
                  "customerId": %d,
                  "restaurantId": %d,
                  "items": [
                    {
                      "menuItemId": %d,
                      "quantity": %d
                    },
                    {
                      "menuItemId": %d,
                      "quantity": %d
                    }
                  ],
                  "payment": {
                    "method": "UPI",
                    "token": "PAY_OK"
                  }
                }
                """.formatted(
                customerId,
                restaurantId,
                menuItemId,
                quantityOne,
                menuItemId,
                quantityTwo
        );
    }

    protected Long createPaidOrder() throws Exception {
        return createPaidOrder(
                data.customer.getId(),
                data.restaurant.getId(),
                data.pizza.getId()
        );
    }

    protected Long createPaidOrder(
            Long customerId,
            Long restaurantId,
            Long menuItemId
    ) throws Exception {
        MvcResult result = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/orders")
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content(createOrderJson(customerId, restaurantId, menuItemId, 1, "PAY_OK"))
                )
                .andExpect(status().isOk())
                .andReturn();

        return json(result).get("id").asLong();
    }

    protected Long createPaidOrderForOwner2Restaurant() throws Exception {
        MvcResult result = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/orders")
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content(createOrderJson(
                                        data.customer.getId(),
                                        data.owner2Restaurant.getId(),
                                        data.owner2Burger.getId(),
                                        1,
                                        "PAY_OK"
                                ))
                )
                .andExpect(status().isOk())
                .andReturn();

        return json(result).get("id").asLong();
    }

    protected void acceptOrder(Long orderId, String username, String password) throws Exception {
        mockMvc.perform(
                        patch("/orders/{id}/accept", orderId)
                                .header("Authorization", basicAuth(username, password))
                )
                .andExpect(status().isOk());
    }

    protected void prepareOrder(Long orderId, String username, String password) throws Exception {
        mockMvc.perform(
                        patch("/orders/{id}/preparing", orderId)
                                .header("Authorization", basicAuth(username, password))
                )
                .andExpect(status().isOk());
    }

    protected void claimOrder(Long orderId, Long partnerId, String username, String password) throws Exception {
        mockMvc.perform(
                        patch("/orders/{id}/claim", orderId)
                                .header("Authorization", basicAuth(username, password))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "deliveryPartnerId": %d
                                        }
                                        """.formatted(partnerId))
                )
                .andExpect(status().isOk());
    }

    protected void markOutForDelivery(Long orderId, String username, String password) throws Exception {
        mockMvc.perform(
                        patch("/orders/{id}/out-for-delivery", orderId)
                                .header("Authorization", basicAuth(username, password))
                )
                .andExpect(status().isOk());
    }

    protected void markDelivered(Long orderId, String username, String password) throws Exception {
        mockMvc.perform(
                        patch("/orders/{id}/delivered", orderId)
                                .header("Authorization", basicAuth(username, password))
                )
                .andExpect(status().isOk());
    }

    protected void completeOrderDelivery(Long orderId) throws Exception {
        acceptOrder(orderId, "owner", "owner123");
        prepareOrder(orderId, "owner", "owner123");
        claimOrder(orderId, data.partner.getId(), "partner", "partner123");
        markOutForDelivery(orderId, "partner", "partner123");
        markDelivered(orderId, "partner", "partner123");
    }

    protected JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected void authenticateAs(String username, UserRole role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                )
        );
    }

    protected void waitUntil(BooleanSupplier condition) throws InterruptedException {
        long timeoutMillis = Duration.ofSeconds(3).toMillis();
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeoutMillis) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(100);
        }
    }

    protected static class TestData {
        public User adminUser;
        public User ownerUser;
        public User owner2User;
        public User customerUser;
        public User customer2User;
        public User partnerUser;
        public User partner2User;

        public City delhi;
        public City mumbai;

        public Customer customer;
        public Customer customer2;

        public Restaurant restaurant;
        public Restaurant owner2Restaurant;

        public MenuItem pizza;
        public MenuItem owner2Burger;

        public DeliveryPartner partner;
        public DeliveryPartner partner2;
    }
}
