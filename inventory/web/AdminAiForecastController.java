package ru.kurs.inventory.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kurs.inventory.admin.ai.AiForecastService;

@Controller
@RequestMapping("/admin/ai")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAiForecastController {

    private final AiForecastService aiForecastService;

    public AdminAiForecastController(AiForecastService aiForecastService) {
        this.aiForecastService = aiForecastService;
    }

    @GetMapping
    public String dashboard(@RequestParam(name = "historyDays", defaultValue = "60") int historyDays,
                            @RequestParam(name = "forecastDays", defaultValue = "30") int forecastDays,
                            @RequestParam(name = "leadTimeDays", defaultValue = "7") int leadTimeDays,
                            @RequestParam(name = "safetyDays", defaultValue = "3") int safetyDays,
                            @RequestParam(name = "warehouseId", required = false) Long warehouseId,
                            Model model) {

        model.addAttribute("title", "Админ • ИИ-прогноз");
        model.addAttribute("historyDays", historyDays);
        model.addAttribute("forecastDays", forecastDays);
        model.addAttribute("leadTimeDays", leadTimeDays);
        model.addAttribute("safetyDays", safetyDays);
        model.addAttribute("warehouseId", warehouseId);

        try {
            model.addAttribute("rows", aiForecastService.forecast(historyDays, forecastDays, leadTimeDays, safetyDays, warehouseId));
        } catch (Exception ex) {
            model.addAttribute("rows", java.util.List.of());
            model.addAttribute("aiError", ex.getMessage());
        }

        return "admin/ai/dashboard";
    }
}
