package ru.kurs.inventory.admin.ai;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kurs.inventory.admin.ai.dto.AiForecastRequest;
import ru.kurs.inventory.admin.ai.dto.AiForecastResponse;
import ru.kurs.inventory.admin.ai.dto.AiForecastViewRow;
import ru.kurs.inventory.admin.dto.ProductDailyDemandRow;
import ru.kurs.inventory.catalog.Product;
import ru.kurs.inventory.catalog.ProductRepository;
import ru.kurs.inventory.stock.MovementType;
import ru.kurs.inventory.stock.StockItemRepository;
import ru.kurs.inventory.stock.StockMovementRepository;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AiForecastService {

    private static final DateTimeFormatter ISO_DAY = DateTimeFormatter.ISO_LOCAL_DATE;

    private final StockMovementRepository stockMovementRepository;
    private final StockItemRepository stockItemRepository;
    private final ProductRepository productRepository;
    private final AiForecastClient aiForecastClient;

    public AiForecastService(StockMovementRepository stockMovementRepository,
                             StockItemRepository stockItemRepository,
                             ProductRepository productRepository,
                             AiForecastClient aiForecastClient) {
        this.stockMovementRepository = stockMovementRepository;
        this.stockItemRepository = stockItemRepository;
        this.productRepository = productRepository;
        this.aiForecastClient = aiForecastClient;
    }

    @Transactional(readOnly = true)
    public List<AiForecastViewRow> forecast(int historyDays,
                                           int forecastDays,
                                           int leadTimeDays,
                                           int safetyDays,
                                           Long warehouseId) {

        historyDays = clamp(historyDays, 7, 365);
        forecastDays = clamp(forecastDays, 1, 120);
        leadTimeDays = clamp(leadTimeDays, 0, 60);
        safetyDays = clamp(safetyDays, 0, 60);

        Instant from = Instant.now().minus(historyDays, ChronoUnit.DAYS);

        // 1) Забираем агрегированную историю спроса (OUT) по дням
        List<ProductDailyDemandRow> raw = stockMovementRepository.dailyDemandByProduct(MovementType.OUT, from);

        // 2) Группируем по productId
        Map<Long, List<ProductDailyDemandRow>> byProduct = raw.stream()
                .collect(Collectors.groupingBy(ProductDailyDemandRow::getProductId));

        // 3) Подготовка списка продуктов (и защита от отсутствия движений)
        List<Product> activeProducts = productRepository.findByActiveTrueOrderByNameAsc();

        // Собираем request в AI
        List<AiForecastRequest.ProductSeries> series = new ArrayList<>();
        for (Product p : activeProducts) {
            List<ProductDailyDemandRow> rows = byProduct.getOrDefault(p.getId(), List.of());
            List<AiForecastRequest.DailyPoint> history = rows.stream()
                    .map(r -> new AiForecastRequest.DailyPoint(
                            r.getDay().format(ISO_DAY),
                            r.getDemandQty()
                    ))
                    .toList();

            series.add(new AiForecastRequest.ProductSeries(p.getId(), p.getSku(), p.getName(), history));
        }

        AiForecastRequest req = new AiForecastRequest(forecastDays, leadTimeDays, safetyDays, series);
        AiForecastResponse ai = aiForecastClient.forecast(req);

        Map<Long, AiForecastResponse.ProductForecast> aiMap = new HashMap<>();
        if (ai.getProducts() != null) {
            for (AiForecastResponse.ProductForecast pf : ai.getProducts()) {
                aiMap.put(pf.getProductId(), pf);
            }
        }

        // 4) Формируем выдачу
        List<AiForecastViewRow> result = new ArrayList<>();
        for (Product p : activeProducts) {
            long currentStock = warehouseId == null
                    ? stockItemRepository.sumQuantityByProductId(p.getId())
                    : stockItemRepository.sumQuantityByProductIdAndWarehouseId(p.getId(), warehouseId);

            AiForecastResponse.ProductForecast pf = aiMap.get(p.getId());
            long forecastTotal = pf == null ? 0 : Math.max(0, pf.getForecastTotalDemand());
            double forecastAvg = pf == null ? 0.0 : Math.max(0.0, pf.getForecastAvgDaily());

            // Политика «покрыть спрос на время поставки + safety»
            // demandForProtectionPeriod = avgDaily * (leadTimeDays + safetyDays)
            long demandForProtectionPeriod = Math.round(forecastAvg * (double) (leadTimeDays + safetyDays));

            // targetStock: учитываем минимальный запас товара и целевое покрытие
            long target = Math.max(p.getMinStock(), demandForProtectionPeriod);

            // recommendedPurchase: добиваем до target
            long purchase = Math.max(0, target - currentStock);

            // Доп. эвристика: если AI не вернул avgDaily, но вернул total — пересчитаем avg.
            if (forecastAvg <= 0.0 && forecastTotal > 0 && forecastDays > 0) {
                forecastAvg = (double) forecastTotal / (double) forecastDays;
            }

            // Риск дефицита: если запас не покрывает leadTimeDays
            long demandDuringLeadTime = Math.round(forecastAvg * (double) leadTimeDays);
            boolean outOfStockRisk = currentStock < Math.max(p.getMinStock(), demandDuringLeadTime);

            // daysOfCover: сколько дней продержимся (по прогнозному среднему спросу)
            Double daysOfCover = forecastAvg > 0.00001 ? (double) currentStock / forecastAvg : null;

            // ближайший день дефицита (грубая оценка) = ceil(daysOfCover)
            String stockoutDay = null;
            if (daysOfCover != null && Double.isFinite(daysOfCover)) {
                long days = (long) Math.floor(daysOfCover);
                stockoutDay = java.time.LocalDate.now().plusDays(days).toString();
            }

            // приоритет: чем больше рекомендуемая закупка и чем меньше покрытие, тем выше
            long priorityScore = purchase * 1000L + (daysOfCover == null ? 0L : Math.max(0L, (long) (1000 - Math.min(1000, daysOfCover))));

            result.add(new AiForecastViewRow(
                    p.getId(),
                    p.getSku(),
                    p.getName(),
                    p.getUnit(),
                    currentStock,
                    p.getMinStock(),
                    forecastDays,
                    forecastTotal,
                    forecastAvg,
                    target,
                    purchase,
                    leadTimeDays,
                    safetyDays,
                    demandForProtectionPeriod,
                    outOfStockRisk,
                    daysOfCover,
                    stockoutDay,
                    priorityScore,
                    pf == null ? List.of() : safeDaily(pf.getDaily())
            ));
        }

        // Сортируем по убыванию priorityScore, чтобы проблемные позиции были сверху
        result.sort(Comparator.comparingLong(AiForecastViewRow::getPriorityScore).reversed());
        return result;
    }

    private static List<AiForecastViewRow.DailyForecastViewPoint> safeDaily(List<AiForecastResponse.DailyForecastPoint> daily) {
        if (daily == null) return List.of();
        return daily.stream()
                .filter(Objects::nonNull)
                .map(p -> new AiForecastViewRow.DailyForecastViewPoint(p.getDay(), Math.max(0, p.getDemandQty())))
                .toList();
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
