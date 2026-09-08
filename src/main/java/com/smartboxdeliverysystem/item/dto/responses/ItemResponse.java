package com.smartboxdeliverysystem.item.dto.responses;

import com.smartboxdeliverysystem.item.model.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private String name;
    private BigDecimal weight;
    private String code;

    public static ItemResponse from(Item item) {
        return new ItemResponse(item.getName(), item.getWeight(), item.getCode());
    }
}