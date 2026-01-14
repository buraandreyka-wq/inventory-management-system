package ru.kurs.inventory.warehouse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.kurs.inventory.common.BaseEntity;

@Entity
@Table(name = "warehouses")
public class Warehouse extends BaseEntity {

    @NotBlank
    @Size(max = 120)
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Size(max = 255)
    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Warehouse() {
    }

    public Warehouse(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
