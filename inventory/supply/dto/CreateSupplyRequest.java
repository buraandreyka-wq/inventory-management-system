package ru.kurs.inventory.supply.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CreateSupplyRequest {

    @NotNull(message = "Склад обязателен")
    private Long warehouseId;

    @NotNull(message = "Поставщик обязателен")
    private Long supplierId;

    private String referenceNumber;

    @Valid
    @NotNull
    private List<CreateSupplyItemDto> items = new ArrayList<>();

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public List<CreateSupplyItemDto> getItems() {
        return items;
    }

    public void setItems(List<CreateSupplyItemDto> items) {
        this.items = items;
    }
}
