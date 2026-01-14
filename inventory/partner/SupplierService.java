package ru.kurs.inventory.partner;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Supplier getById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Поставщик не найден: id=" + id));
    }

    public Supplier create(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public Supplier update(Long id, Supplier payload) {
        Supplier existing = getById(id);
        existing.setName(payload.getName());
        existing.setPhone(payload.getPhone());
        existing.setEmail(payload.getEmail());
        existing.setAddress(payload.getAddress());
        return supplierRepository.save(existing);
    }

    public void delete(Long id) {
        supplierRepository.deleteById(id);
    }
}
