package ru.kurs.inventory.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kurs.inventory.catalog.ProductService;
import ru.kurs.inventory.partner.SupplierService;
import ru.kurs.inventory.supply.Supply;
import ru.kurs.inventory.supply.SupplyService;
import ru.kurs.inventory.supply.dto.CreateSupplyRequest;
import ru.kurs.inventory.warehouse.WarehouseService;

@Controller
@RequestMapping("/supplies")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class SupplyController {

    private final SupplyService supplyService;
    private final WarehouseService warehouseService;
    private final SupplierService supplierService;

    public SupplyController(SupplyService supplyService,
                            WarehouseService warehouseService,
                            SupplierService supplierService) {
        this.supplyService = supplyService;
        this.warehouseService = warehouseService;
        this.supplierService = supplierService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("supplies", supplyService.findAll());
        return "supplies/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        CreateSupplyRequest req = new CreateSupplyRequest();
        // по умолчанию 1 пустая строка
        req.getItems().add(new ru.kurs.inventory.supply.dto.CreateSupplyItemDto());

        model.addAttribute("req", req);
        model.addAttribute("warehouses", warehouseService.findAllActive());
        model.addAttribute("suppliers", supplierService.findAll());
        return "supplies/new";
    }

    @PostMapping
    public String create(@ModelAttribute("req") @Valid CreateSupplyRequest req,
                         BindingResult binding,
                         Authentication auth,
                         Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("warehouses", warehouseService.findAllActive());
            model.addAttribute("suppliers", supplierService.findAll());
            return "supplies/new";
        }

        try {
            Supply created = supplyService.receive(req, auth.getName());
            return "redirect:/supplies/" + created.getId();
        } catch (Exception ex) {
            model.addAttribute("warehouses", warehouseService.findAllActive());
            model.addAttribute("suppliers", supplierService.findAll());
            ViewUtils.putError(model, ex);
            return "supplies/new";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Supply supply = supplyService.getById(id);
        model.addAttribute("supply", supply);
        model.addAttribute("items", supplyService.getItems(id));
        return "supplies/view";
    }
}
