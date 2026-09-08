package com.smartboxdeliverysystem.box.repository;

import com.smartboxdeliverysystem.box.models.Box;
import com.smartboxdeliverysystem.box.models.BoxState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface BoxRepository extends JpaRepository<Box, Long> {
    Optional<Box> findByTxref(String txref);
    boolean existsByTxref(String txref);
    List<Box> findByStateAndBatteryCapacityGreaterThanEqual(BoxState state, Integer minBattery);
}
