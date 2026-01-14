package ru.kurs.inventory.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kurs.inventory.partner.Supplier;
import ru.kurs.inventory.partner.SupplierService;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String list(Model model) {
        model.addAttribute("suppliers", supplierService.findAll());
        return "suppliers/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "suppliers/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String create(@Valid @ModelAttribute("supplier") Supplier supplier,
                         BindingResult bindingResult,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "suppliers/form";
        }
        supplierService.create(supplier);
        ra.addFlashAttribute("success", "Поставщик создан");
        return "redirect:/suppliers";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("supplier", supplierService.getById(id));
        return "suppliers/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("supplier") Supplier supplier,
                         BindingResult bindingResult,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "suppliers/form";
        }
        supplierService.update(id, supplier);
        ra.addFlashAttribute("success", "Поставщик обновлён");
        return "redirect:/suppliers";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        supplierService.delete(id);
        ra.addFlashAttribute("success", "Поставщик удалён");
        return "redirect:/suppliers";
    }
}
