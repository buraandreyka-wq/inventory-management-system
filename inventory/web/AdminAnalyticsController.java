package ru.kurs.inventory.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    // Раздел "Аналитика" убран по требованиям.
    // URL оставлен, чтобы не ломать старые закладки: делаем редирект на админ-раздел.

    @GetMapping
    public String disabled() {
        return "redirect:/admin/users";
    }
}
