package ru.kurs.inventory.supply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupplyItemRepository extends JpaRepository<SupplyItem, Long> {

    @Query("select si from SupplyItem si join fetch si.product p where si.supply.id = :supplyId")
    List<SupplyItem> findAllBySupplyIdDetailed(@Param("supplyId") Long supplyId);
}
