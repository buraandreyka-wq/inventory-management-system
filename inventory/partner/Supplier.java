package ru.kurs.inventory.partner;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.kurs.inventory.common.BaseEntity;

@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @Email
    @Size(max = 120)
    @Column(name = "email", length = 120)
    private String email;

    @Size(max = 255)
    @Column(name = "address", length = 255)
    private String address;

    public Supplier() {
    }

    public Supplier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
