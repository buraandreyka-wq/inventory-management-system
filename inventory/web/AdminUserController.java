package ru.kurs.inventory.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kurs.inventory.admin.SystemLogService;
import ru.kurs.inventory.common.SecurityUtils;
import ru.kurs.inventory.security.RoleName;
import ru.kurs.inventory.user.User;
import ru.kurs.inventory.user.UserService;
import ru.kurs.inventory.user.dto.UserForm;

import java.util.EnumSet;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final SystemLogService systemLogService;

    public AdminUserController(UserService userService, SystemLogService systemLogService) {
        this.userService = userService;
        this.systemLogService = systemLogService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new UserForm());
        model.addAttribute("mode", "create");
        return "admin/users/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") UserForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            return "admin/users/form";
        }
        try {
            Set<RoleName> roles = toRoles(form);
            User created = userService.create(form.getUsername(), form.getPassword(), roles, form.isEnabled());
            systemLogService.info("USER_CREATE", "Создан пользователь: " + created.getUsername(),
                    SecurityUtils.currentUsername(), "User", created.getId());
            ra.addFlashAttribute("success", "Пользователь создан");
            return "redirect:/admin/users";
        } catch (Exception ex) {
            ViewUtils.putError(model, ex);
            model.addAttribute("mode", "create");
            return "admin/users/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.getById(id);
        UserForm form = fromUser(user);
        model.addAttribute("form", form);
        model.addAttribute("userId", id);
        model.addAttribute("mode", "edit");
        return "admin/users/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") UserForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("mode", "edit");
            return "admin/users/form";
        }
        try {
            Set<RoleName> roles = toRoles(form);
            User updated = userService.update(id, form.getUsername(), form.getPassword(), roles, form.isEnabled());
            systemLogService.info("USER_UPDATE", "Обновлён пользователь: " + updated.getUsername(),
                    SecurityUtils.currentUsername(), "User", updated.getId());
            ra.addFlashAttribute("success", "Пользователь обновлён");
            return "redirect:/admin/users";
        } catch (Exception ex) {
            ViewUtils.putError(model, ex);
            model.addAttribute("userId", id);
            model.addAttribute("mode", "edit");
            return "admin/users/form";
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggleEnabled(@PathVariable Long id, RedirectAttributes ra) {
        User user = userService.getById(id);
        boolean newState = !user.isEnabled();
        userService.setEnabled(id, newState);
        systemLogService.warn("USER_TOGGLE", "Пользователь " + user.getUsername() + " enabled=" + newState,
                SecurityUtils.currentUsername(), "User", id);
        ra.addFlashAttribute("success", "Статус обновлён");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        User user = userService.getById(id);
        userService.delete(id);
        systemLogService.warn("USER_DELETE", "Удалён пользователь: " + user.getUsername(),
                SecurityUtils.currentUsername(), "User", id);
        ra.addFlashAttribute("success", "Пользователь удалён");
        return "redirect:/admin/users";
    }

    private static Set<RoleName> toRoles(UserForm form) {
        EnumSet<RoleName> roles = EnumSet.noneOf(RoleName.class);
        if (form.isRoleEmployee()) roles.add(RoleName.ROLE_EMPLOYEE);
        if (form.isRoleManager()) roles.add(RoleName.ROLE_MANAGER);
        if (form.isRoleAdmin()) roles.add(RoleName.ROLE_ADMIN);
        return roles;
    }

    private static UserForm fromUser(User user) {
        UserForm f = new UserForm();
        f.setUsername(user.getUsername());
        f.setEnabled(user.isEnabled());
        f.setRoleEmployee(user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_EMPLOYEE));
        f.setRoleManager(user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_MANAGER));
        f.setRoleAdmin(user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN));
        return f;
    }
}
