package com.smartboxdeliverysystem.box.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "box")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "txref", nullable = false, unique = true, length = 20)
    private String txref;

    @Column(name = "weight_limit", nullable = false)
    private BigDecimal weightLimit;

    @Column(name = "battery_capacity", nullable = false)
    private Integer batteryCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private BoxState state;
}
