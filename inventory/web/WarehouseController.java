package ru.kurs.inventory.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kurs.inventory.warehouse.Warehouse;
import ru.kurs.inventory.warehouse.WarehouseService;

@Controller
@RequestMapping("/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String list(Model model) {
        model.addAttribute("warehouses", warehouseService.findAll());
        return "warehouses/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("warehouse", new Warehouse());
        return "warehouses/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String create(@Valid @ModelAttribute("warehouse") Warehouse warehouse,
                         BindingResult bindingResult,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "warehouses/form";
        }
        warehouseService.create(warehouse);
        ra.addFlashAttribute("success", "Склад создан");
        return "redirect:/warehouses";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("warehouse", warehouseService.getById(id));
        return "warehouses/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("warehouse") Warehouse warehouse,
                         BindingResult bindingResult,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "warehouses/form";
        }
        warehouseService.update(id, warehouse);
        ra.addFlashAttribute("success", "Склад обновлён");
        return "redirect:/warehouses";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        warehouseService.delete(id);
        ra.addFlashAttribute("success", "Склад удалён");
        return "redirect:/warehouses";
    }
}
