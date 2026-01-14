package ru.kurs.inventory.admin.dto;

import java.math.BigDecimal;

public class ProductSalesRow {

    private final String sku;
    private final String name;
    private final long qty;
    private final BigDecimal revenue;

    public ProductSalesRow(String sku, String name, long qty, BigDecimal revenue) {
        this.sku = sku;
        this.name = name;
        this.qty = qty;
        this.revenue = revenue;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public long getQty() {
        return qty;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}
