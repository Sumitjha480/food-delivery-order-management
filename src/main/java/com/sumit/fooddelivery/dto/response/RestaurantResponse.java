package com.sumit.fooddelivery.dto.response;

import com.sumit.fooddelivery.enums.RestaurantStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {

    private Long id;

    private String name;

    private String address;

    private String city;

    private Integer estimatedDeliveryTime;

    private RestaurantStatus status;

}
