package ru.kurs.inventory.web;

import org.springframework.ui.Model;

public final class ViewUtils {

    private ViewUtils() {
    }

    public static void putError(Model model, Exception ex) {
        if (ex == null) {
            return;
        }
        model.addAttribute("error", ex.getMessage());
    }

    public static void putSuccess(Model model, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        model.addAttribute("success", message);
    }
}
