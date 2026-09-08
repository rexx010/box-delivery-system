package com.smartboxdeliverysystem.box.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatteryResponse {
    private String txref;
    private Integer batteryCapacity;
}