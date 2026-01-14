package ru.kurs.inventory.supply.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateSupplyItemDto {

    @NotBlank(message = "SKU обязателен")
    private String sku;

    @Min(value = 1, message = "Количество должно быть >= 1")
    private int quantity;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
