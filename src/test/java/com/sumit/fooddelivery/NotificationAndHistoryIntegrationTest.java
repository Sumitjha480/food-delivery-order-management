package com.sumit.fooddelivery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationAndHistoryIntegrationTest extends BaseIntegrationTest {

    @Test
    void orderStatusChangesShouldCreateAsyncNotifications() throws Exception {
        Long orderId = createPaidOrder();

        waitUntil(() -> notificationRepository.findByOrder_IdOrderBySentAtDesc(orderId).size() >= 2);

        assertThat(notificationRepository.findByOrder_IdOrderBySentAtDesc(orderId)).hasSizeGreaterThanOrEqualTo(2);

        acceptOrder(orderId, "owner", "owner123");

        waitUntil(() -> notificationRepository.findByOrder_IdOrderBySentAtDesc(orderId).size() >= 4);

        claimOrder(orderId, data.partner.getId(), "partner", "partner123");

        waitUntil(() -> notificationRepository.findByOrder_IdOrderBySentAtDesc(orderId).size() >= 7);

        mockMvc.perform(
                        get("/notifications/order/{orderId}", orderId)
                                .header("Authorization", basicAuth("admin", "admin123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(7)));
    }

    @Test
    void statusHistoryEndpointShouldBeOwnershipScoped() throws Exception {
        Long orderId = createPaidOrder();
        completeOrderDelivery(orderId);

        mockMvc.perform(
                        get("/orders/{id}/history", orderId)
                                .header("Authorization", basicAuth("customer", "customer123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        mockMvc.perform(
                        get("/orders/{id}/history", orderId)
                                .header("Authorization", basicAuth("customer2", "customer2123"))
                )
                .andExpect(status().isForbidden());
    }
}
