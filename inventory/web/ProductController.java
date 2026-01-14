package ru.kurs.inventory.web;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kurs.inventory.catalog.CategoryService;
import ru.kurs.inventory.catalog.Product;
import ru.kurs.inventory.catalog.ProductService;
import ru.kurs.inventory.stock.StockService;
import ru.kurs.inventory.warehouse.WarehouseService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final WarehouseService warehouseService;
    private final StockService stockService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             WarehouseService warehouseService,
                             StockService stockService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.warehouseService = warehouseService;
        this.stockService = stockService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Long warehouseId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Product> result = productService.search(q, page, 10);

        // остатки для текущей страницы (суммарно или по выбранному складу)
        Map<Long, Long> qtyByProductId = new HashMap<>();
        for (Product p : result.getContent()) {
            qtyByProductId.put(p.getId(), stockService.getQuantity(p.getId(), warehouseId));
        }

        model.addAttribute("productsPage", result);
        model.addAttribute("q", q);
        model.addAttribute("warehouses", warehouseService.findAllActive());
        model.addAttribute("warehouseId", warehouseId);
        model.addAttribute("qtyByProductId", qtyByProductId);
        return "products/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("categoryId", null);
        return "products/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String create(@Valid @ModelAttribute("product") Product product,
                         BindingResult bindingResult,
                         @RequestParam(required = false) Long categoryId,
                         Model model,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("categoryId", categoryId);
            return "products/form";
        }
        try {
            productService.create(product, categoryId);
            ra.addFlashAttribute("success", "Товар создан");
            return "redirect:/products";
        } catch (Exception ex) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("categoryId", categoryId);
            ViewUtils.putError(model, ex);
            return "products/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.getById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("categoryId", product.getCategory() != null ? product.getCategory().getId() : null);
        return "products/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("product") Product product,
                         BindingResult bindingResult,
                         @RequestParam(required = false) Long categoryId,
                         Model model,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("categoryId", categoryId);
            return "products/form";
        }
        try {
            productService.update(id, product, categoryId);
            ra.addFlashAttribute("success", "Товар обновлён");
            return "redirect:/products";
        } catch (Exception ex) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("categoryId", categoryId);
            ViewUtils.putError(model, ex);
            return "products/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("success", "Товар удалён");
        return "redirect:/products";
    }
}
