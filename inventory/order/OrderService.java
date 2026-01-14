package ru.kurs.inventory.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kurs.inventory.catalog.Product;
import ru.kurs.inventory.catalog.ProductService;
import ru.kurs.inventory.common.EntityNotFoundException;
import ru.kurs.inventory.order.dto.CreateSalesOrderItemDto;
import ru.kurs.inventory.order.dto.CreateSalesOrderRequest;
import ru.kurs.inventory.stock.MovementType;
import ru.kurs.inventory.stock.StockMovement;
import ru.kurs.inventory.stock.StockMovementRepository;
import ru.kurs.inventory.stock.StockService;
import ru.kurs.inventory.user.User;
import ru.kurs.inventory.user.UserService;
import ru.kurs.inventory.warehouse.Warehouse;
import ru.kurs.inventory.warehouse.WarehouseService;

import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final StockService stockService;
    private final StockMovementRepository stockMovementRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        UserService userService,
                        WarehouseService warehouseService,
                        ProductService productService,
                        StockService stockService,
                        StockMovementRepository stockMovementRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.stockService = stockService;
        this.stockMovementRepository = stockMovementRepository;
    }

    public Order createSalesOrder(CreateSalesOrderRequest req, String username) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Добавьте хотя бы одну позицию");
        }

        User creator = userService.getByUsername(username);
        Warehouse warehouse = warehouseService.getById(req.getWarehouseId());

        Order order = new Order();
        order.setOrderType(OrderType.SALES);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setWarehouse(warehouse);
        order.setCustomerName(req.getCustomerName());
        order.setCreatedBy(creator);
        order = orderRepository.save(order);

        for (CreateSalesOrderItemDto dto : req.getItems()) {
            Product product = productService.findBySku(dto.getSku())
                    .orElseThrow(() -> new EntityNotFoundException("Товар не найден: sku=" + dto.getSku()));

            // списание со склада
            stockService.subtract(warehouse, product, dto.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(dto.getQuantity());
            item.setPrice(product.getPrice());
            orderItemRepository.save(item);

            StockMovement movement = new StockMovement();
            movement.setMovementType(MovementType.OUT);
            movement.setSourceOrder(order);
            movement.setWarehouse(warehouse);
            movement.setProduct(product);
            movement.setQuantity(dto.getQuantity());
            movement.setCreatedBy(creator);
            stockMovementRepository.save(movement);
        }

        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> myOrders(String username) {
        User user = userService.getByUsername(username);
        return orderRepository.findAllByCreatedById(user.getId());
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        // Важно: для страницы просмотра нам нужны связанные сущности (createdBy/warehouse),
        // иначе при выключенном OpenSessionInView получим LazyInitializationException.
        return orderRepository.findByIdWithViewRelations(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getItems(Long orderId) {
        return orderItemRepository.findAllByOrderId(orderId);
    }
}
