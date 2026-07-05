package com.sumit.fooddelivery.dto.response;

import com.sumit.fooddelivery.enums.DeliveryPartnerStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryPartnerResponse {

    private Long id;

    private Long userId;

    private String username;

    private String name;

    private String phone;

    private Long cityId;

    private String cityName;

    private DeliveryPartnerStatus status;
}
