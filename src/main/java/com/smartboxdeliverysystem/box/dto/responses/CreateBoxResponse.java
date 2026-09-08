package com.smartboxdeliverysystem.box.dto.responses;

import com.smartboxdeliverysystem.box.models.Box;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBoxResponse {

    private String txref;
    private BigDecimal weightLimit;
    private Integer batteryCapacity;
    private String state;

    public static CreateBoxResponse from(Box box) {
        return new CreateBoxResponse(
                box.getTxref(),
                box.getWeightLimit(),
                box.getBatteryCapacity(),
                box.getState().name()
        );
    }
}