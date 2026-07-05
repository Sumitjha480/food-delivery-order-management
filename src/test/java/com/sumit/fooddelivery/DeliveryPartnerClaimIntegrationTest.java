package com.sumit.fooddelivery;

import com.sumit.fooddelivery.entity.DeliveryPartner;
import com.sumit.fooddelivery.entity.User;
import com.sumit.fooddelivery.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeliveryPartnerClaimIntegrationTest extends BaseIntegrationTest {

    @Test
    void partnerCannotClaimUsingAnotherPartnerProfile() throws Exception {
        Long orderId = createPaidOrder();
        acceptOrder(orderId, "owner", "owner123");

        mockMvc.perform(
                        patch("/orders/{id}/claim", orderId)
                                .header("Authorization", basicAuth("partner2", "partner2123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "deliveryPartnerId": %d
                                        }
                                        """.formatted(data.partner.getId()))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void correctPartnerCanClaimOwnProfile() throws Exception {
        Long orderId = createPaidOrder();
        acceptOrder(orderId, "owner", "owner123");

        mockMvc.perform(
                        patch("/orders/{id}/claim", orderId)
                                .header("Authorization", basicAuth("partner", "partner123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "deliveryPartnerId": %d
                                        }
                                        """.formatted(data.partner.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryPartnerName").value("Ravi Partner"));
    }

    @Test
    void partnerFromDifferentCityCannotClaimOrder() throws Exception {
        User mumbaiPartnerUser = createUser("mumbaiPartner", "mumbai123", UserRole.DELIVERY_PARTNER);

        DeliveryPartner mumbaiPartner = createDeliveryPartner(
                mumbaiPartnerUser,
                "Mumbai Partner",
                data.mumbai
        );

        Long orderId = createPaidOrder();
        acceptOrder(orderId, "owner", "owner123");

        mockMvc.perform(
                        patch("/orders/{id}/claim", orderId)
                                .header("Authorization", basicAuth("mumbaiPartner", "mumbai123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "deliveryPartnerId": %d
                                        }
                                        """.formatted(mumbaiPartner.getId()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Delivery partner city does not match restaurant city. Restaurant city: Delhi, partner city: Mumbai"
                ));
    }

    @Test
    void busyPartnerCannotClaimAnotherOrder() throws Exception {
        Long firstOrderId = createPaidOrder();
        acceptOrder(firstOrderId, "owner", "owner123");
        claimOrder(firstOrderId, data.partner.getId(), "partner", "partner123");

        Long secondOrderId = createPaidOrder();
        acceptOrder(secondOrderId, "owner", "owner123");

        mockMvc.perform(
                        patch("/orders/{id}/claim", secondOrderId)
                                .header("Authorization", basicAuth("partner", "partner123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "deliveryPartnerId": %d
                                        }
                                        """.formatted(data.partner.getId()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Delivery partner is not available. Current status is BUSY"));
    }

    @Test
    void wrongPartnerCannotUpdateAssignedOrderDeliveryStatus() throws Exception {
        Long orderId = createPaidOrder();
        acceptOrder(orderId, "owner", "owner123");
        prepareOrder(orderId, "owner", "owner123");
        claimOrder(orderId, data.partner2.getId(), "partner2", "partner2123");

        mockMvc.perform(
                        patch("/orders/{id}/out-for-delivery", orderId)
                                .header("Authorization", basicAuth("partner", "partner123"))
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        patch("/orders/{id}/out-for-delivery", orderId)
                                .header("Authorization", basicAuth("partner2", "partner2123"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("OUT_FOR_DELIVERY"));
    }
}
