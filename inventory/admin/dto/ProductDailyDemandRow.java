package ru.kurs.inventory.admin.dto;

import java.time.LocalDate;

/**
 * Агрегированная потребность/спрос по товару за день.
 *
 * demandQty = продажи + списания/расход (все движения OUT).
 */
public class ProductDailyDemandRow {

    private final Long productId;
    private final String sku;
    private final String name;
    private final LocalDate day;
    private final long demandQty;

    /**
     * Основной конструктор (удобен для Java-кода).
     */
    public ProductDailyDemandRow(Long productId,
                                String sku,
                                String name,
                                LocalDate day,
                                long demandQty) {
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.day = day;
        this.demandQty = demandQty;
    }

    /**
     * Конструктор для JPQL `select new ...`.
     *
     * В запросе используется function('date', m.createdAt), которая в зависимости от диалекта/драйвера
     * может материализоваться как:
     *  - java.sql.Date (типично для MySQL)
     *  - java.time.LocalDate (редко, но возможно)
     *  - java.util.Date / java.sql.Timestamp
     *
     * А sum(int) обычно возвращает Long.
     */
    public ProductDailyDemandRow(Long productId,
                                String sku,
                                String name,
                                java.sql.Date day,
                                Long demandQty) {
        this(productId,
                sku,
                name,
                day == null ? null : day.toLocalDate(),
                demandQty == null ? 0L : demandQty);
    }

    /**
     * Более "широкая" перегрузка для JPQL, если Hibernate подставляет не java.sql.Date.
     */
    public ProductDailyDemandRow(Long productId,
                                String sku,
                                String name,
                                Object day,
                                Number demandQty) {
        this(productId,
                sku,
                name,
                toLocalDate(day),
                demandQty == null ? 0L : demandQty.longValue());
    }

    private static LocalDate toLocalDate(Object day) {
        if (day == null) return null;
        if (day instanceof LocalDate ld) return ld;
        if (day instanceof java.sql.Date sd) return sd.toLocalDate();
        if (day instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        if (day instanceof java.util.Date d) {
            return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        // Последняя попытка: строка "YYYY-MM-DD"
        return LocalDate.parse(day.toString());
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

    public LocalDate getDay() {
        return day;
    }

    public long getDemandQty() {
        return demandQty;
    }
}
