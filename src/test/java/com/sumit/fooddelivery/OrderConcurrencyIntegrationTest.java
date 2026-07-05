package com.sumit.fooddelivery;

import com.sumit.fooddelivery.dto.request.OrderItemRequest;
import com.sumit.fooddelivery.dto.request.OrderRequest;
import com.sumit.fooddelivery.dto.request.PaymentRequest;
import com.sumit.fooddelivery.entity.MenuItem;
import com.sumit.fooddelivery.enums.PaymentMethod;
import com.sumit.fooddelivery.enums.UserRole;
import com.sumit.fooddelivery.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderConcurrencyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Test
    void concurrentOrdersShouldNotOversellLimitedStock() throws InterruptedException {
        data.pizza.setStock(1);
        menuItemRepository.saveAndFlush(data.pizza);

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
                    authenticateAs("customer", UserRole.CUSTOMER);

                    readyLatch.countDown();
                    startLatch.await();

                    orderService.create(buildOrderRequest());

                    successCount.incrementAndGet();
                } catch (Exception ex) {
                    failureCount.incrementAndGet();
                } finally {
                    SecurityContextHolderHolder.clear();
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        executor.shutdown();

        MenuItem updatedMenuItem = menuItemRepository.findById(data.pizza.getId()).orElseThrow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);
        assertThat(updatedMenuItem.getStock()).isEqualTo(0);
        assertThat(orderRepository.count()).isEqualTo(1);
    }

    private OrderRequest buildOrderRequest() {
        OrderRequest request = new OrderRequest();
        request.setCustomerId(data.customer.getId());
        request.setRestaurantId(data.restaurant.getId());

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setMenuItemId(data.pizza.getId());
        itemRequest.setQuantity(1);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setMethod(PaymentMethod.UPI);
        paymentRequest.setToken("PAY_OK");

        request.setItems(List.of(itemRequest));
        request.setPayment(paymentRequest);

        return request;
    }

    private static class SecurityContextHolderHolder {
        static void clear() {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
