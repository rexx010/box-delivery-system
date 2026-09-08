package com.smartboxdeliverysystem.item.dto.requests;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "name may only contain letters, numbers, hyphen and underscore")
    private String name;

    @NotNull
    @DecimalMin(value = "0", inclusive = false, message = "weight must be greater than 0")
    @DecimalMax(value = "500", message = "a single item cannot exceed 500g")
    private BigDecimal weight;

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "code may only contain upper case letters, numbers and underscore")
    private String code;
}