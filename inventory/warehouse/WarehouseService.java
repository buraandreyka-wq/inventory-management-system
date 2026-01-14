package ru.kurs.inventory.warehouse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public List<Warehouse> findAll() {
        return warehouseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Warehouse> findAllActive() {
        return warehouseRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Warehouse getById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Склад не найден: id=" + id));
    }

    public Warehouse create(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    public Warehouse update(Long id, Warehouse payload) {
        Warehouse existing = getById(id);
        existing.setName(payload.getName());
        existing.setAddress(payload.getAddress());
        existing.setActive(payload.isActive());
        return warehouseRepository.save(existing);
    }

    public void delete(Long id) {
        warehouseRepository.deleteById(id);
    }
}
