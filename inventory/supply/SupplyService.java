package ru.kurs.inventory.supply;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kurs.inventory.catalog.Product;
import ru.kurs.inventory.catalog.ProductService;
import ru.kurs.inventory.common.EntityNotFoundException;
import ru.kurs.inventory.partner.Supplier;
import ru.kurs.inventory.partner.SupplierService;
import ru.kurs.inventory.stock.MovementType;
import ru.kurs.inventory.stock.StockMovement;
import ru.kurs.inventory.stock.StockMovementRepository;
import ru.kurs.inventory.stock.StockService;
import ru.kurs.inventory.supply.dto.CreateSupplyItemDto;
import ru.kurs.inventory.supply.dto.CreateSupplyRequest;
import ru.kurs.inventory.user.User;
import ru.kurs.inventory.user.UserService;
import ru.kurs.inventory.warehouse.Warehouse;
import ru.kurs.inventory.warehouse.WarehouseService;

import java.util.List;

@Service
@Transactional
public class SupplyService {

    private final SupplyRepository supplyRepository;
    private final SupplyItemRepository supplyItemRepository;
    private final SupplierService supplierService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final UserService userService;
    private final StockService stockService;
    private final StockMovementRepository stockMovementRepository;

    public SupplyService(SupplyRepository supplyRepository,
                         SupplyItemRepository supplyItemRepository,
                         SupplierService supplierService,
                         WarehouseService warehouseService,
                         ProductService productService,
                         UserService userService,
                         StockService stockService,
                         StockMovementRepository stockMovementRepository) {
        this.supplyRepository = supplyRepository;
        this.supplyItemRepository = supplyItemRepository;
        this.supplierService = supplierService;
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.userService = userService;
        this.stockService = stockService;
        this.stockMovementRepository = stockMovementRepository;
    }

    public Supply receive(CreateSupplyRequest req, String username) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Добавьте хотя бы одну позицию");
        }

        User creator = userService.getByUsername(username);
        Warehouse warehouse = warehouseService.getById(req.getWarehouseId());
        Supplier supplier = supplierService.getById(req.getSupplierId());

        Supply supply = new Supply();
        supply.setWarehouse(warehouse);
        supply.setSupplier(supplier);
        supply.setReferenceNumber(req.getReferenceNumber());
        supply.setStatus(SupplyStatus.RECEIVED);
        supply.setCreatedBy(creator);
        supply = supplyRepository.save(supply);

        for (CreateSupplyItemDto dto : req.getItems()) {
            Product product = productService.findBySku(dto.getSku())
                    .orElseThrow(() -> new EntityNotFoundException("Товар не найден: sku=" + dto.getSku()));

            // приход на склад
            stockService.add(warehouse, product, dto.getQuantity());

            SupplyItem item = new SupplyItem();
            item.setSupply(supply);
            item.setProduct(product);
            item.setQuantity(dto.getQuantity());
            supplyItemRepository.save(item);

            StockMovement movement = new StockMovement();
            movement.setMovementType(MovementType.IN);
            movement.setWarehouse(warehouse);
            movement.setProduct(product);
            movement.setQuantity(dto.getQuantity());
            movement.setCreatedBy(creator);
            stockMovementRepository.save(movement);
        }

        return supply;
    }

    @Transactional(readOnly = true)
    public List<Supply> findAll() {
        return supplyRepository.findAllDetailed();
    }

    @Transactional(readOnly = true)
    public Supply getById(Long id) {
        return supplyRepository.findByIdDetailed(id)
                .orElseThrow(() -> new EntityNotFoundException("Поставка не найдена: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<SupplyItem> getItems(Long supplyId) {
        return supplyItemRepository.findAllBySupplyIdDetailed(supplyId);
    }
}
