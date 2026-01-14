package ru.kurs.inventory.stock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kurs.inventory.admin.dto.ProductDailyDemandRow;
import ru.kurs.inventory.stock.MovementType;

import java.time.Instant;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("select m from StockMovement m join fetch m.product p join fetch m.warehouse w order by m.createdAt desc")
    Page<StockMovement> findAllDetailed(Pageable pageable);

    /**
     * История спроса по дням: продажи + списания/расход = движения OUT.
     *
     * Важно: используем функцию date(m.createdAt) (MySQL). Для тестов на H2 этот запрос может не использоваться.
     */
    @Query("select new ru.kurs.inventory.admin.dto.ProductDailyDemandRow(p.id, p.sku, p.name, function('date', m.createdAt), sum(m.quantity)) " +
            "from StockMovement m join m.product p " +
            "where m.movementType = :type and m.createdAt >= :from and p.active = true " +
            "group by p.id, p.sku, p.name, function('date', m.createdAt) " +
            "order by p.name asc, function('date', m.createdAt) asc")
    List<ProductDailyDemandRow> dailyDemandByProduct(@Param("type") MovementType type,
                                                     @Param("from") Instant from);
}
