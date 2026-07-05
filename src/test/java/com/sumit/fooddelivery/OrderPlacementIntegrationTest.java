package com.sumit.fooddelivery;

import com.sumit.fooddelivery.entity.MenuItem;
import com.sumit.fooddelivery.repository.OrderStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderPlacementIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderStatusHistoryRepository historyRepository;

    @Test
    void createOrderShouldReduceStockAndCreateStatusHistory() throws Exception {
        mockMvc.perform(
                        post("/orders")
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content(createOrderJson(
                                        data.customer.getId(),
                                        data.restaurant.getId(),
                                        data.pizza.getId(),
                                        1,
                                        "PAY_OK"
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("PLACED"))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.paymentReference").exists())
                .andExpect(jsonPath("$.items", hasSize(1)));

        MenuItem updatedMenuItem = menuItemRepository.findById(data.pizza.getId()).orElseThrow();

        assertThat(updatedMenuItem.getStock()).isEqualTo(9);
        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(historyRepository.findAll()).hasSize(1);
        assertThat(historyRepository.findAll().getFirst().getNewStatus().name()).isEqualTo("PLACED");
    }

    @Test
    void paymentFailureShouldRollbackStockAndOrder() throws Exception {
        mockMvc.perform(
                        post("/orders")
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content(createOrderJson(
                                        data.customer.getId(),
                                        data.restaurant.getId(),
                                        data.pizza.getId(),
                                        1,
                                        "FAIL"
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Payment failed: Payment was declined by mock payment gateway"));

        MenuItem updatedMenuItem = menuItemRepository.findById(data.pizza.getId()).orElseThrow();

        assertThat(updatedMenuItem.getStock()).isEqualTo(10);
        assertThat(orderRepository.count()).isZero();
        assertThat(orderItemRepository.count()).isZero();
    }

    @Test
    void insufficientStockShouldReturnBadRequest() throws Exception {
        mockMvc.perform(
                        post("/orders")
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content(createOrderJson(
                                        data.customer.getId(),
                                        data.restaurant.getId(),
                                        data.pizza.getId(),
                                        999,
                                        "PAY_OK"
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Insufficient stock for item: Margherita Pizza. Available stock: 10, requested quantity: 999"
                ));

        MenuItem updatedMenuItem = menuItemRepository.findById(data.pizza.getId()).orElseThrow();

        assertThat(updatedMenuItem.getStock()).isEqualTo(10);
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void duplicateMenuItemsShouldBeMergedBeforeStockDeduction() throws Exception {
        mockMvc.perform(
                        post("/orders")
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content(createDuplicateItemOrderJson(
                                        data.customer.getId(),
                                        data.restaurant.getId(),
                                        data.pizza.getId(),
                                        1,
                                        2
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(3));

        MenuItem updatedMenuItem = menuItemRepository.findById(data.pizza.getId()).orElseThrow();

        assertThat(updatedMenuItem.getStock()).isEqualTo(7);
    }
}
