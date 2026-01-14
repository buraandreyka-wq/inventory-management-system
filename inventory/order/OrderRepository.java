package ru.kurs.inventory.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o join fetch o.warehouse w where o.createdBy.id = :userId order by o.createdAt desc")
    List<Order> findAllByCreatedById(@Param("userId") Long userId);

    @Query("select o from Order o " +
            "join fetch o.createdBy cb " +
            "join fetch o.warehouse w " +
            "left join fetch o.supplier s " +
            "where o.id = :id")
    java.util.Optional<Order> findByIdWithViewRelations(@Param("id") Long id);
}
