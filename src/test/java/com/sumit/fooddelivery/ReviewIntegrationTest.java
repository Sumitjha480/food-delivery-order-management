package com.sumit.fooddelivery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewIntegrationTest extends BaseIntegrationTest {

    @Test
    void reviewBeforeDeliveryShouldFail() throws Exception {
        Long orderId = createPaidOrder();

        mockMvc.perform(
                        post("/orders/{orderId}/review", orderId)
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "rating": 5,
                                          "comment": "Great food"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Review can be added only after order is DELIVERED. Current status is PLACED"
                ));
    }

    @Test
    void customerCanReviewDeliveredOrderOnlyOnce() throws Exception {
        Long orderId = createPaidOrder();
        completeOrderDelivery(orderId);

        mockMvc.perform(
                        post("/orders/{orderId}/review", orderId)
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "rating": 5,
                                          "comment": "Great food and fast delivery"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great food and fast delivery"));

        mockMvc.perform(
                        post("/orders/{orderId}/review", orderId)
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "rating": 4,
                                          "comment": "Duplicate review"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Review already exists for this order"));
    }

    @Test
    void differentCustomerCannotReviewSomeoneElsesDeliveredOrder() throws Exception {
        Long orderId = createPaidOrder();
        completeOrderDelivery(orderId);

        mockMvc.perform(
                        post("/orders/{orderId}/review", orderId)
                                .header("Authorization", basicAuth("customer2", "customer2123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "rating": 5,
                                          "comment": "Trying to review someone else's order"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidRatingShouldReturnValidationError() throws Exception {
        Long orderId = createPaidOrder();
        completeOrderDelivery(orderId);

        mockMvc.perform(
                        post("/orders/{orderId}/review", orderId)
                                .header("Authorization", basicAuth("customer", "customer123"))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "rating": 6,
                                          "comment": "Invalid rating"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
