package com.sumit.fooddelivery.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponse {

    private Long id;

    private Long userId;

    private String username;

    private String name;

    private String email;

    private String phone;

    private String address;
}
