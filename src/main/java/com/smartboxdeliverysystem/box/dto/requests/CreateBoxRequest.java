package com.smartboxdeliverysystem.box.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBoxRequest {

    @NotBlank
    @Size(max = 20, message = "txref must be at most 20 characters")
    private String txref;

    @NotNull
    @DecimalMin(value = "0", inclusive = false, message = "weightLimit must be greater than 0")
    @DecimalMax(value = "500", message = "weightLimit cannot exceed 500g")
    private BigDecimal weightLimit;

    @NotNull
    @Min(value = 0, message = "batteryCapacity cannot be negative")
    @Max(value = 100, message = "batteryCapacity cannot exceed 100")
    private Integer batteryCapacity;
}
