package com.sumit.fooddelivery.dto.response;

import com.sumit.fooddelivery.enums.CityStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CityResponse {

    private Long id;

    private String name;

    private CityStatus status;
}
