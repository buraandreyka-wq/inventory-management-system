package ru.kurs.inventory.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kurs.inventory.admin.dto.ProductSalesRow;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("select oi from OrderItem oi join fetch oi.product p where oi.order.id = :orderId")
    List<OrderItem> findAllByOrderId(@Param("orderId") Long orderId);

    @Query("select new ru.kurs.inventory.admin.dto.ProductSalesRow(p.sku, p.name, sum(oi.quantity), sum(oi.price * oi.quantity)) " +
            "from OrderItem oi join oi.product p join oi.order o " +
            "where o.orderType = ru.kurs.inventory.order.OrderType.SALES and o.status = ru.kurs.inventory.order.OrderStatus.CONFIRMED " +
            "group by p.sku, p.name order by sum(oi.quantity) desc")
    List<ProductSalesRow> topSoldProducts(org.springframework.data.domain.Pageable pageable);

    @Query("select coalesce(sum(oi.price * oi.quantity), 0) " +
            "from OrderItem oi join oi.order o " +
            "where o.orderType = ru.kurs.inventory.order.OrderType.SALES and o.status = ru.kurs.inventory.order.OrderStatus.CONFIRMED")
    java.math.BigDecimal totalRevenue();
}
