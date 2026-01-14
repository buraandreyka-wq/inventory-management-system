package ru.kurs.inventory.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {
    // Страница админских системных настроек отключена.
    // SystemSettingService оставлен для внутреннего использования (например, аналитика).

    @RequestMapping
    public String disabled() {
        return "redirect:/admin/users";
    }
}
