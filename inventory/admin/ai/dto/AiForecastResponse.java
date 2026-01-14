package ru.kurs.inventory.admin.ai.dto;

import java.util.List;

/**
 * Ответ внешнего AI/ML сервиса.
 */
public class AiForecastResponse {

    private int forecastDays;

    /**
     * Прогноз спроса по товарам.
     */
    private List<ProductForecast> products;

    public AiForecastResponse() {
    }

    public int getForecastDays() {
        return forecastDays;
    }

    public void setForecastDays(int forecastDays) {
        this.forecastDays = forecastDays;
    }

    public List<ProductForecast> getProducts() {
        return products;
    }

    public void setProducts(List<ProductForecast> products) {
        this.products = products;
    }

    public static class ProductForecast {
        private Long productId;

        /** Суммарный прогноз спроса на forecastDays */
        private long forecastTotalDemand;

        /** Среднесуточный спрос (полезен для рекомендаций) */
        private double forecastAvgDaily;

        /**
         * Прогноз по дням. День = ISO yyyy-MM-dd.
         * Можно отдавать не все дни, но желательно forecastDays точек.
         */
        private List<DailyForecastPoint> daily;

        public ProductForecast() {
        }

        public ProductForecast(Long productId, long forecastTotalDemand, double forecastAvgDaily) {
            this.productId = productId;
            this.forecastTotalDemand = forecastTotalDemand;
            this.forecastAvgDaily = forecastAvgDaily;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public long getForecastTotalDemand() {
            return forecastTotalDemand;
        }

        public void setForecastTotalDemand(long forecastTotalDemand) {
            this.forecastTotalDemand = forecastTotalDemand;
        }

        public double getForecastAvgDaily() {
            return forecastAvgDaily;
        }

        public void setForecastAvgDaily(double forecastAvgDaily) {
            this.forecastAvgDaily = forecastAvgDaily;
        }

        public List<DailyForecastPoint> getDaily() {
            return daily;
        }

        public void setDaily(List<DailyForecastPoint> daily) {
            this.daily = daily;
        }
    }

    public static class DailyForecastPoint {
        /** ISO date: yyyy-MM-dd */
        private String day;

        /** Прогноз спроса на день */
        private long demandQty;

        public DailyForecastPoint() {
        }

        public DailyForecastPoint(String day, long demandQty) {
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
