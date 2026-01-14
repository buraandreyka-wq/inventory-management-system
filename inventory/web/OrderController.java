package ru.kurs.inventory.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kurs.inventory.order.Order;
import ru.kurs.inventory.order.OrderService;
import ru.kurs.inventory.order.dto.CreateSalesOrderRequest;
import ru.kurs.inventory.warehouse.WarehouseService;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final WarehouseService warehouseService;

    public OrderController(OrderService orderService, WarehouseService warehouseService) {
        this.orderService = orderService;
        this.warehouseService = warehouseService;
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @GetMapping
    public String myOrders(Authentication auth, Model model) {
        model.addAttribute("orders", orderService.myOrders(auth.getName()));
        return "orders/list";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @GetMapping("/new")
    public String newSalesOrderForm(Model model) {
        CreateSalesOrderRequest req = new CreateSalesOrderRequest();
        // по умолчанию 1 пустая строка
        req.getItems().add(new ru.kurs.inventory.order.dto.CreateSalesOrderItemDto());

        model.addAttribute("req", req);
        model.addAttribute("warehouses", warehouseService.findAllActive());
        return "orders/new";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @PostMapping
    public String createSalesOrder(@ModelAttribute("req") @Valid CreateSalesOrderRequest req,
                                   BindingResult binding,
                                   Authentication auth,
                                   Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("warehouses", warehouseService.findAllActive());
            return "orders/new";
        }
        try {
            Order created = orderService.createSalesOrder(req, auth.getName());
            return "redirect:/orders/" + created.getId();
        } catch (Exception ex) {
            model.addAttribute("warehouses", warehouseService.findAllActive());
            ViewUtils.putError(model, ex);
            return "orders/new";
        }
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication auth, Model model) {
        // простая проверка: сотрудник видит только свои заказы
        Order order = orderService.getById(id);
        if (!order.getCreatedBy().getUsername().equals(auth.getName())) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("items", orderService.getItems(id));
        return "orders/view";
    }
}
