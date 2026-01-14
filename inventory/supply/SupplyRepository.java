package ru.kurs.inventory.supply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplyRepository extends JpaRepository<Supply, Long> {

    @Query("select s from Supply s " +
            "join fetch s.warehouse w " +
            "join fetch s.supplier sp " +
            "order by s.suppliedAt desc")
    List<Supply> findAllDetailed();

    @Query("select s from Supply s " +
            "join fetch s.warehouse w " +
            "join fetch s.supplier sp " +
            "where s.id = :id")
    Optional<Supply> findByIdDetailed(@Param("id") Long id);
}
