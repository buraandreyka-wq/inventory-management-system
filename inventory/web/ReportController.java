package ru.kurs.inventory.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kurs.inventory.stock.StockMovement;
import ru.kurs.inventory.stock.StockMovementRepository;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class ReportController {

    private final StockMovementRepository stockMovementRepository;

    public ReportController(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    @GetMapping("/movements")
    public String movements(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "25") int size,
                            Model model) {
        Page<StockMovement> movements = stockMovementRepository.findAllDetailed(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        model.addAttribute("movements", movements);
        return "reports/movements";
    }
}
