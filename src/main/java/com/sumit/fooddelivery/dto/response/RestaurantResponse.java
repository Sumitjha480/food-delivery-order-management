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

    private Long cityId;

    private String cityName;

    private Long ownerId;

    private String ownerUsername;

    private Integer estimatedDeliveryTime;

    private RestaurantStatus status;
}
