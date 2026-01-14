package ru.kurs.inventory.stock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kurs.inventory.catalog.Product;
import ru.kurs.inventory.catalog.ProductRepository;
import ru.kurs.inventory.warehouse.Warehouse;
import ru.kurs.inventory.warehouse.WarehouseRepository;

import java.util.List;

@Service
@Transactional
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    public StockService(StockItemRepository stockItemRepository,
                        WarehouseRepository warehouseRepository,
                        ProductRepository productRepository) {
        this.stockItemRepository = stockItemRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
    }

    /**
     * Быстрый доступ к остатку по товару.
     * Если warehouseId == null, то возвращается суммарный остаток по всем складам.
     */
    @Transactional(readOnly = true)
    public long getQuantity(Long productId, Long warehouseId) {
        if (warehouseId == null) {
            return stockItemRepository.sumQuantityByProductId(productId);
        }
        return stockItemRepository.sumQuantityByProductIdAndWarehouseId(productId, warehouseId);
    }

    @Transactional(readOnly = true)
    public List<StockItem> findAll() {
        return stockItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public StockItem getOrCreate(Warehouse warehouse, Product product) {
        return stockItemRepository.findByWarehouseAndProduct(warehouse, product)
                .orElseGet(() -> stockItemRepository.save(new StockItem(warehouse, product)));
    }

    public void add(Warehouse warehouse, Product product, long qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Количество должно быть > 0");
        }
        StockItem item = getOrCreate(warehouse, product);
        item.setQuantity(item.getQuantity() + qty);
        stockItemRepository.save(item);
    }

    public void subtract(Warehouse warehouse, Product product, long qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Количество должно быть > 0");
        }
        StockItem item = getOrCreate(warehouse, product);
        long newQty = item.getQuantity() - qty;
        if (newQty < 0) {
            throw new IllegalArgumentException("Недостаточно товара на складе. Остаток=" + item.getQuantity());
        }
        item.setQuantity(newQty);
        stockItemRepository.save(item);
    }

    /**
     * Для UI: просмотр остатков с фильтрацией по складу и/или строке поиска по названию товара.
     */
    @Transactional(readOnly = true)
    public List<StockItem> view(Long warehouseId, String q) {
        boolean hasWarehouse = warehouseId != null;
        boolean hasQ = q != null && !q.isBlank();
        String query = hasQ ? q.trim() : null;

        // UI-поиск должен работать и по названию товара, и по SKU.
        if (hasWarehouse && hasQ) {
            List<StockItem> byName = stockItemRepository
                    .findByWarehouseIdAndProductNameContainingIgnoreCaseOrderByProductNameAsc(warehouseId, query);
            if (!byName.isEmpty()) {
                return byName;
            }
            return stockItemRepository
                    .findByWarehouseIdAndProductSkuContainingIgnoreCaseOrderByProductNameAsc(warehouseId, query);
        }
        if (hasWarehouse) {
            return stockItemRepository.findByWarehouseIdOrderByProductNameAsc(warehouseId);
        }
        if (hasQ) {
            List<StockItem> byName = stockItemRepository
                    .findByProductNameContainingIgnoreCaseOrderByProductNameAsc(query);
            if (!byName.isEmpty()) {
                return byName;
            }
            return stockItemRepository.findByProductSkuContainingIgnoreCaseOrderByProductNameAsc(query);
        }
        return stockItemRepository.findAllByOrderByProductNameAsc();
    }

    /**
     * Универсальная корректировка остатков: delta может быть + (приход) или - (расход).
     */
    public void adjust(Long warehouseId, Long productId, long delta) {
        if (delta == 0) {
            throw new IllegalArgumentException("Изменение количества не может быть 0");
        }
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Склад не найден: id=" + warehouseId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден: id=" + productId));

        long qty = Math.abs(delta);
        if (delta > 0) {
            add(warehouse, product, qty);
        } else {
            subtract(warehouse, product, qty);
        }
    }
}
