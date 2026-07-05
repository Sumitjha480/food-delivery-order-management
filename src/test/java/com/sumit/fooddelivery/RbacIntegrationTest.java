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
class RbacIntegrationTest extends BaseIntegrationTest {

    @Test
    void protectedApiWithoutAuthShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/restaurants"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotCreateRestaurant() throws Exception {
        mockMvc.perform(
                        post("/restaurants")
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "name": "Invalid Restaurant",
                                          "address": "Invalid Address",
                                          "cityId": %d,
                                          "ownerUsername": "owner",
                                          "estimatedDeliveryTime": 30
                                        }
                                        """.formatted(data.delhi.getId()))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateCity() throws Exception {
        mockMvc.perform(
                        post("/cities")
                                .header("Authorization", basicAuth("admin", "admin123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "name": "Bangalore"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bangalore"));
    }

    @Test
    void customerCannotCreateCity() throws Exception {
        mockMvc.perform(
                        post("/cities")
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "name": "Chennai"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateDeliveryPartnerProfile() throws Exception {
        createUser("newPartner", "newPartner123", com.sumit.fooddelivery.enums.UserRole.DELIVERY_PARTNER);

        mockMvc.perform(
                        post("/delivery-partners")
                                .header("Authorization", basicAuth("admin", "admin123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "username": "newPartner",
                                          "name": "New Partner",
                                          "phone": "8888888888",
                                          "cityId": %d,
                                          "status": "AVAILABLE"
                                        }
                                        """.formatted(data.delhi.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newPartner"));
    }

    @Test
    void customerCannotCreateDeliveryPartnerProfile() throws Exception {
        mockMvc.perform(
                        post("/delivery-partners")
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "username": "partner",
                                          "name": "Invalid Partner",
                                          "phone": "8888888888",
                                          "cityId": %d,
                                          "status": "AVAILABLE"
                                        }
                                        """.formatted(data.delhi.getId()))
                )
                .andExpect(status().isForbidden());
    }
}
