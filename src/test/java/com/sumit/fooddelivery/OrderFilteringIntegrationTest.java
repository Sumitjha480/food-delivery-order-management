package com.sumit.fooddelivery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderFilteringIntegrationTest extends BaseIntegrationTest {

    @Test
    void adminCanFilterOrdersByStatusRestaurantCustomerAndPartner() throws Exception {
        Long orderId = createPaidOrder();
        completeOrderDelivery(orderId);

        mockMvc.perform(
                        get("/orders?status=DELIVERED")
                                .header("Authorization", basicAuth("admin", "admin123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(
                        get("/orders?restaurantId=" + data.restaurant.getId())
                                .header("Authorization", basicAuth("admin", "admin123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(
                        get("/orders?customerId=" + data.customer.getId())
                                .header("Authorization", basicAuth("admin", "admin123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(
                        get("/orders?deliveryPartnerId=" + data.partner.getId())
                                .header("Authorization", basicAuth("admin", "admin123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    void customerCanSeeOnlyOwnOrders() throws Exception {
        createPaidOrder();

        mockMvc.perform(
                        get("/orders")
                                .header("Authorization", basicAuth("customer", "customer123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(
                        get("/orders?customerId=" + data.customer2.getId())
                                .header("Authorization", basicAuth("customer", "customer123"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void restaurantOwnerCanSeeOnlyOwnRestaurantOrders() throws Exception {
        createPaidOrder();

        Long owner2OrderId = createPaidOrderForOwner2Restaurant();

        mockMvc.perform(
                        get("/orders?restaurantId=" + data.restaurant.getId())
                                .header("Authorization", basicAuth("owner", "owner123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(
                        get("/orders?restaurantId=" + data.owner2Restaurant.getId())
                                .header("Authorization", basicAuth("owner", "owner123"))
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        get("/orders/" + owner2OrderId)
                                .header("Authorization", basicAuth("owner", "owner123"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deliveryPartnerCanSeeOnlyAssignedOrders() throws Exception {
        Long orderId = createPaidOrder();
        acceptOrder(orderId, "owner", "owner123");
        claimOrder(orderId, data.partner.getId(), "partner", "partner123");

        mockMvc.perform(
                        get("/orders")
                                .header("Authorization", basicAuth("partner", "partner123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(
                        get("/orders?deliveryPartnerId=" + data.partner2.getId())
                                .header("Authorization", basicAuth("partner", "partner123"))
                )
                .andExpect(status().isForbidden());
    }
}
