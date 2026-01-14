package ru.kurs.inventory.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.kurs.inventory.common.BaseEntity;

@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @NotBlank
    @Size(max = 120)
    @Column(name = "name", nullable = false, unique = true, length = 120)
    private String name;

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
