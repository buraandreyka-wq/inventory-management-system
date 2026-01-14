package ru.kurs.inventory.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogController {
    // Страница админских системных логов отключена.
    // Внутренний аудит (SystemLogService) оставлен для модуля пользователей.

    @RequestMapping
    public String disabled() {
        return "redirect:/admin/users";
    }
}
