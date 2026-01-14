package ru.kurs.inventory.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CreateSalesOrderRequest {

    @NotNull
    private Long warehouseId;

    private String customerName;

    @Valid
    private List<CreateSalesOrderItemDto> items = new ArrayList<>();

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<CreateSalesOrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<CreateSalesOrderItemDto> items) {
        this.items = items;
    }
}
