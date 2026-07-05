package com.sumit.fooddelivery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OwnershipAuthorizationIntegrationTest extends BaseIntegrationTest {

    @Test
    void restaurantOwnerCannotCreateMenuItemForAnotherOwnersRestaurant() throws Exception {
        mockMvc.perform(
                        post("/restaurants/{restaurantId}/menu-items", data.owner2Restaurant.getId())
                                .header("Authorization", basicAuth("owner", "owner123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "name": "Cross Owner Item",
                                          "price": 99,
                                          "stock": 5
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void restaurantOwnerCanCreateMenuItemForOwnRestaurant() throws Exception {
        mockMvc.perform(
                        post("/restaurants/{restaurantId}/menu-items", data.restaurant.getId())
                                .header("Authorization", basicAuth("owner", "owner123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "name": "Owner Special Pizza",
                                          "price": 399,
                                          "stock": 10
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Owner Special Pizza"));
    }

    @Test
    void customerCannotCreateOrderUsingAnotherCustomerId() throws Exception {
        mockMvc.perform(
                        post("/orders")
                                .header("Authorization", basicAuth("customer2", "customer2123"))
                                .contentType("application/json")
                                .content(createOrderJson(
                                        data.customer.getId(),
                                        data.restaurant.getId(),
                                        data.pizza.getId(),
                                        1,
                                        "PAY_OK"
                                ))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void wrongRestaurantOwnerCannotAcceptAnotherOwnersOrder() throws Exception {
        Long orderId = createPaidOrderForOwner2Restaurant();

        mockMvc.perform(
                        patch("/orders/{id}/accept", orderId)
                                .header("Authorization", basicAuth("owner", "owner123"))
                )
                .andExpect(status().isForbidden());


        mockMvc.perform(
                        patch("/orders/{id}/accept", orderId)
                                .header("Authorization", basicAuth("owner2", "owner2123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("ACCEPTED"));
    }

    @Test
    void customerCannotListAllCustomers() throws Exception {
        mockMvc.perform(
                        get("/customers")
                                .header("Authorization", basicAuth("customer", "customer123"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListAllCustomers() throws Exception {
        mockMvc.perform(
                        get("/customers")
                                .header("Authorization", basicAuth("admin", "admin123"))
                )
                .andExpect(status().isOk());
    }
}
