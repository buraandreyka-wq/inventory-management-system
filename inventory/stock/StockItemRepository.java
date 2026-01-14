package ru.kurs.inventory.stock;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.kurs.inventory.catalog.Product;
import ru.kurs.inventory.warehouse.Warehouse;

import java.util.List;
import java.util.Optional;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    Optional<StockItem> findByWarehouseAndProduct(Warehouse warehouse, Product product);

    /**
     * Для UI нам нужно отображать поля товара (sku/name/unit/minStock).
     * Поэтому подгружаем association product сразу, чтобы не ловить LazyInitializationException
     * при рендеринге Thymeleaf после завершения транзакции.
     */
    @EntityGraph(attributePaths = "product")
    List<StockItem> findByWarehouseIdOrderByProductNameAsc(Long warehouseId);

    @EntityGraph(attributePaths = "product")
    List<StockItem> findByWarehouseIdAndProductNameContainingIgnoreCaseOrderByProductNameAsc(Long warehouseId, String q);

    @EntityGraph(attributePaths = "product")
    List<StockItem> findByWarehouseIdAndProductSkuContainingIgnoreCaseOrderByProductNameAsc(Long warehouseId, String q);

    @EntityGraph(attributePaths = "product")
    List<StockItem> findByProductNameContainingIgnoreCaseOrderByProductNameAsc(String q);

    @EntityGraph(attributePaths = "product")
    List<StockItem> findByProductSkuContainingIgnoreCaseOrderByProductNameAsc(String q);

    @EntityGraph(attributePaths = "product")
    List<StockItem> findAllByOrderByProductNameAsc();

    @org.springframework.data.jpa.repository.Query("select coalesce(sum(si.quantity), 0) from StockItem si where si.product.id = :productId")
    long sumQuantityByProductId(@org.springframework.data.repository.query.Param("productId") Long productId);

    @org.springframework.data.jpa.repository.Query("select coalesce(sum(si.quantity), 0) from StockItem si where si.product.id = :productId and si.warehouse.id = :warehouseId")
    long sumQuantityByProductIdAndWarehouseId(@org.springframework.data.repository.query.Param("productId") Long productId,
                                              @org.springframework.data.repository.query.Param("warehouseId") Long warehouseId);
}
