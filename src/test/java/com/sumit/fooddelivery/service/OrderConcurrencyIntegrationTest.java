package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.dto.request.OrderItemRequest;
import com.sumit.fooddelivery.dto.request.OrderRequest;
import com.sumit.fooddelivery.entity.Customer;
import com.sumit.fooddelivery.entity.MenuItem;
import com.sumit.fooddelivery.entity.Restaurant;
import com.sumit.fooddelivery.enums.RestaurantStatus;
import com.sumit.fooddelivery.repository.CustomerRepository;
import com.sumit.fooddelivery.repository.MenuItemRepository;
import com.sumit.fooddelivery.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderConcurrencyIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Test
    void concurrentOrdersShouldNotOversellStock() throws InterruptedException {

        Customer customer = new Customer();
        customer.setName("Concurrency Test Customer");
        customer.setEmail("concurrency@test.com");
        customer.setPhone("8888888888");
        customer.setAddress("Delhi");
        customer = customerRepository.save(customer);

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Concurrency Test Restaurant");
        restaurant.setAddress("Test Address");
        restaurant.setCity("Delhi");
        restaurant.setEstimatedDeliveryTime(30);
        restaurant.setStatus(RestaurantStatus.ACTIVE);
        restaurant = restaurantRepository.save(restaurant);

        MenuItem menuItem = new MenuItem();
        menuItem.setName("Limited Burger");
        menuItem.setPrice(BigDecimal.valueOf(100));
        menuItem.setStock(1);
        menuItem.setRestaurant(restaurant);
        menuItem = menuItemRepository.save(menuItem);

        Long customerId = customer.getId();
        Long restaurantId = restaurant.getId();
        Long menuItemId = menuItem.getId();

        int concurrentRequests = 2;

        CountDownLatch readyLatch = new CountDownLatch(concurrentRequests);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(concurrentRequests);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        var executor = Executors.newFixedThreadPool(concurrentRequests);

        for (int i = 0; i < concurrentRequests; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    OrderRequest request = new OrderRequest();
                    request.setCustomerId(customerId);
                    request.setRestaurantId(restaurantId);

                    OrderItemRequest itemRequest = new OrderItemRequest();
                    itemRequest.setMenuItemId(menuItemId);
                    itemRequest.setQuantity(1);

                    request.setItems(List.of(itemRequest));

                    orderService.create(request);
                    successCount.incrementAndGet();

                } catch (Exception ex) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        executor.shutdown();

        MenuItem updatedMenuItem = menuItemRepository.findById(menuItemId).orElseThrow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);
        assertThat(updatedMenuItem.getStock()).isEqualTo(0);
    }
}
