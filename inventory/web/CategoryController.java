package ru.kurs.inventory.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kurs.inventory.catalog.Category;
import ru.kurs.inventory.catalog.CategoryService;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "categories/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String create(@Valid @ModelAttribute("category") Category category,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "categories/form";
        }
        try {
            categoryService.create(category);
            ra.addFlashAttribute("success", "Категория создана");
            return "redirect:/categories";
        } catch (Exception ex) {
            ViewUtils.putError(model, ex);
            return "categories/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getById(id));
        return "categories/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("category") Category category,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "categories/form";
        }
        try {
            categoryService.update(id, category);
            ra.addFlashAttribute("success", "Категория обновлена");
            return "redirect:/categories";
        } catch (Exception ex) {
            ViewUtils.putError(model, ex);
            return "categories/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.delete(id);
        ra.addFlashAttribute("success", "Категория удалена");
        return "redirect:/categories";
    }
}
