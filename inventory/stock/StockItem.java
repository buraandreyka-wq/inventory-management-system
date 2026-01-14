package ru.kurs.inventory.stock;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import ru.kurs.inventory.catalog.Product;
import ru.kurs.inventory.common.BaseEntity;
import ru.kurs.inventory.warehouse.Warehouse;

@Entity
@Table(
        name = "stock_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_stock_warehouse_product", columnNames = {"warehouse_id", "product_id"})
        }
)
public class StockItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Min(0)
    @Column(name = "quantity", nullable = false)
    private long quantity = 0;

    protected StockItem() {
    }

    public StockItem(Warehouse warehouse, Product product) {
        this.warehouse = warehouse;
        this.product = product;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public Product getProduct() {
        return product;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}
