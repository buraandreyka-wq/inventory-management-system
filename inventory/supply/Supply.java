package ru.kurs.inventory.supply;

import jakarta.persistence.*;
import ru.kurs.inventory.partner.Supplier;
import ru.kurs.inventory.user.User;
import ru.kurs.inventory.warehouse.Warehouse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supplies")
public class Supply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "reference_number", length = 60, unique = true)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SupplyStatus status = SupplyStatus.RECEIVED;

    @Column(name = "supplied_at", nullable = false)
    private Instant suppliedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "supply", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplyItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public SupplyStatus getStatus() {
        return status;
    }

    public void setStatus(SupplyStatus status) {
        this.status = status;
    }

    public Instant getSuppliedAt() {
        return suppliedAt;
    }

    public void setSuppliedAt(Instant suppliedAt) {
        this.suppliedAt = suppliedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<SupplyItem> getItems() {
        return items;
    }

    public void setItems(List<SupplyItem> items) {
        this.items = items;
    }
}
