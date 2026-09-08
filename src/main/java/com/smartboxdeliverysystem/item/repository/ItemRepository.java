package com.smartboxdeliverysystem.item.repository;

import com.smartboxdeliverysystem.item.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByBoxTxref(String txref);

    @Query("select coalesce(sum(i.weight), 0) from Item i where i.box.txref = :txref")
    BigDecimal sumWeightByBoxTxref(@Param("txref") String txref);
}
