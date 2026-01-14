package ru.kurs.inventory.admin.ai.dto;

/**
 * Строка для отображения результата прогноза в UI.
 */
import java.util.List;

public class AiForecastViewRow {

    private final Long productId;
    private final String sku;
    private final String name;
    private final String unit;

    private final long currentStock;
    private final int minStock;

    private final int forecastDays;
    private final long forecastTotalDemand;
    private final double forecastAvgDaily;

    private final long recommendedTargetStock;
    private final long recommendedPurchaseQty;

    private final int leadTimeDays;
    private final int safetyDays;

    /** Сколько нужно держать на складе, чтобы покрыть leadTimeDays+safetyDays */
    private final long demandForProtectionPeriod;

    /** true, если запас не покрывает lead time (или minStock) */
    private final boolean outOfStockRisk;

    /**
     * Сколько дней хватит запаса при forecastAvgDaily.
     * null, если forecastAvgDaily == 0.
     */
    private final Double daysOfCover;

    /** Оценочная дата (yyyy-MM-dd), когда запас закончится, или null */
    private final String stockoutDay;

    /**
     * Служебный рейтинг для сортировки (чем больше, тем выше в списке).
     */
    private final long priorityScore;

    /** Прогноз спроса по дням */
    private final List<DailyForecastViewPoint> daily;

    public AiForecastViewRow(Long productId,
                             String sku,
                             String name,
                             String unit,
                             long currentStock,
                             int minStock,
                             int forecastDays,
                             long forecastTotalDemand,
                             double forecastAvgDaily,
                             long recommendedTargetStock,
                             long recommendedPurchaseQty,
                             int leadTimeDays,
                             int safetyDays,
                             long demandForProtectionPeriod,
                             boolean outOfStockRisk,
                             Double daysOfCover,
                             String stockoutDay,
                             long priorityScore,
                             List<DailyForecastViewPoint> daily) {
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.unit = unit;
        this.currentStock = currentStock;
        this.minStock = minStock;
        this.forecastDays = forecastDays;
        this.forecastTotalDemand = forecastTotalDemand;
        this.forecastAvgDaily = forecastAvgDaily;
        this.recommendedTargetStock = recommendedTargetStock;
        this.recommendedPurchaseQty = recommendedPurchaseQty;
        this.leadTimeDays = leadTimeDays;
        this.safetyDays = safetyDays;
        this.demandForProtectionPeriod = demandForProtectionPeriod;
        this.outOfStockRisk = outOfStockRisk;
        this.daysOfCover = daysOfCover;
        this.stockoutDay = stockoutDay;
        this.priorityScore = priorityScore;
        this.daily = daily;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public long getCurrentStock() {
        return currentStock;
    }

    public int getMinStock() {
        return minStock;
    }

    public int getForecastDays() {
        return forecastDays;
    }

    public long getForecastTotalDemand() {
        return forecastTotalDemand;
    }

    public double getForecastAvgDaily() {
        return forecastAvgDaily;
    }

    public long getRecommendedTargetStock() {
        return recommendedTargetStock;
    }

    public long getRecommendedPurchaseQty() {
        return recommendedPurchaseQty;
    }

    public int getLeadTimeDays() {
        return leadTimeDays;
    }

    public int getSafetyDays() {
        return safetyDays;
    }

    public long getDemandForProtectionPeriod() {
        return demandForProtectionPeriod;
    }

    public boolean isOutOfStockRisk() {
        return outOfStockRisk;
    }

    public Double getDaysOfCover() {
        return daysOfCover;
    }

    public String getStockoutDay() {
        return stockoutDay;
    }

    public long getPriorityScore() {
        return priorityScore;
    }

    public List<DailyForecastViewPoint> getDaily() {
        return daily;
    }

    public static class DailyForecastViewPoint {
        private final String day;
        private final long demandQty;

        public DailyForecastViewPoint(String day, long demandQty) {
            this.day = day;
            this.demandQty = demandQty;
        }

        public String getDay() {
            return day;
        }

        public long getDemandQty() {
            return demandQty;
        }
    }
}
