package ru.kurs.inventory.admin.ai.dto;

import java.util.List;

/**
 * Запрос во внешний AI/ML сервис.
 *
 * Мы отправляем историю спроса (продажи/списания) по дням для каждого товара,
 * и просим спрогнозировать будущий спрос на горизонте forecastDays.
 */
public class AiForecastRequest {

    private int forecastDays;

    /**
     * На сколько дней вперёд мы хотим «защититься» по запасам.
     * Например, если поставщик везёт 7 дней — нам важно обеспечить спрос на эти 7 дней.
     */
    private int leadTimeDays;

    /**
     * Дополнительный страховочный запас (в днях спроса), поверх lead time.
     * Например, safetyDays=3 => держим +3 дня спроса сверх времени поставки.
     */
    private int safetyDays;

    private List<ProductSeries> products;

    public AiForecastRequest() {
    }

    public AiForecastRequest(int forecastDays,
                             int leadTimeDays,
                             int safetyDays,
                             List<ProductSeries> products) {
        this.forecastDays = forecastDays;
        this.leadTimeDays = leadTimeDays;
        this.safetyDays = safetyDays;
        this.products = products;
    }

    public int getForecastDays() {
        return forecastDays;
    }

    public void setForecastDays(int forecastDays) {
        this.forecastDays = forecastDays;
    }

    public int getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(int leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public int getSafetyDays() {
        return safetyDays;
    }

    public void setSafetyDays(int safetyDays) {
        this.safetyDays = safetyDays;
    }

    public List<ProductSeries> getProducts() {
        return products;
    }

    public void setProducts(List<ProductSeries> products) {
        this.products = products;
    }

    public static class ProductSeries {
        private Long productId;
        private String sku;
        private String name;
        private List<DailyPoint> history;

        public ProductSeries() {
        }

        public ProductSeries(Long productId, String sku, String name, List<DailyPoint> history) {
            this.productId = productId;
            this.sku = sku;
            this.name = name;
            this.history = history;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<DailyPoint> getHistory() {
            return history;
        }

        public void setHistory(List<DailyPoint> history) {
            this.history = history;
        }
    }

    public static class DailyPoint {
        /** ISO date: yyyy-MM-dd */
        private String day;
        private long demandQty;

        public DailyPoint() {
        }

        public DailyPoint(String day, long demandQty) {
            this.day = day;
            this.demandQty = demandQty;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public long getDemandQty() {
            return demandQty;
        }

        public void setDemandQty(long demandQty) {
            this.demandQty = demandQty;
        }
    }
}
