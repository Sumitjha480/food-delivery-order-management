package com.sumit.fooddelivery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderLifecycleIntegrationTest extends BaseIntegrationTest {

    @Test
    void fullOrderLifecycleShouldReachDeliveredAndCreateHistory() throws Exception {
        Long orderId = createPaidOrder();

        acceptOrder(orderId, "owner", "owner123");
        prepareOrder(orderId, "owner", "owner123");
        claimOrder(orderId, data.partner.getId(), "partner", "partner123");
        markOutForDelivery(orderId, "partner", "partner123");
        markDelivered(orderId, "partner", "partner123");

        mockMvc.perform(
                        get("/orders/{id}", orderId)
                                .header("Authorization", basicAuth("admin", "admin123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("DELIVERED"))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.deliveryPartnerName").value("Ravi Partner"));

        mockMvc.perform(
                        get("/orders/{id}/history", orderId)
                                .header("Authorization", basicAuth("admin", "admin123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].newStatus").value("PLACED"))
                .andExpect(jsonPath("$[1].newStatus").value("ACCEPTED"))
                .andExpect(jsonPath("$[2].newStatus").value("PREPARING"))
                .andExpect(jsonPath("$[3].newStatus").value("OUT_FOR_DELIVERY"))
                .andExpect(jsonPath("$[4].newStatus").value("DELIVERED"));
    }

    @Test
    void invalidTransitionShouldReturnBadRequest() throws Exception {
        Long orderId = createPaidOrder();

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/orders/{id}/preparing", orderId)
                                .header("Authorization", basicAuth("owner", "owner123"))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only ACCEPTED orders can move to PREPARING. Current status is PLACED"));
    }
}
