package ru.kurs.inventory.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kurs.inventory.catalog.ProductService;
import ru.kurs.inventory.stock.StockService;
import ru.kurs.inventory.warehouse.WarehouseService;

@Controller
@RequestMapping("/stock")
@Validated
public class StockController {

    private final StockService stockService;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    public StockController(StockService stockService,
                           WarehouseService warehouseService,
                           ProductService productService) {
        this.stockService = stockService;
        this.warehouseService = warehouseService;
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public String list(@RequestParam(required = false) Long warehouseId,
                       @RequestParam(required = false) String q,
                       Model model) {
        model.addAttribute("warehouses", warehouseService.findAllActive());
        model.addAttribute("warehouseId", warehouseId);
        model.addAttribute("q", q);
        model.addAttribute("items", stockService.view(warehouseId, q));
        return "stock/list";
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String adjust(@RequestParam @NotNull Long warehouseId,
                         @RequestParam @NotNull Long productId,
                         @RequestParam @Min(1) int delta,
                         @RequestParam(defaultValue = "IN") String type,
                         RedirectAttributes ra) {
        if (!type.equalsIgnoreCase("IN") && !type.equalsIgnoreCase("OUT")) {
            ra.addFlashAttribute("error", "Неверный тип операции");
            return "redirect:/stock?warehouseId=" + warehouseId;
        }

        long signedDelta = type.equalsIgnoreCase("IN") ? delta : -delta;

        try {
            stockService.adjust(warehouseId, productId, signedDelta);
            ra.addFlashAttribute("success", "Остатки обновлены");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/stock?warehouseId=" + warehouseId;
    }

    @GetMapping("/adjust")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String adjustForm(@RequestParam(required = false) Long warehouseId,
                             Model model) {
        model.addAttribute("warehouses", warehouseService.findAllActive());
        model.addAttribute("products", productService.findAllActive());
        model.addAttribute("warehouseId", warehouseId);
        return "stock/adjust";
    }
}
