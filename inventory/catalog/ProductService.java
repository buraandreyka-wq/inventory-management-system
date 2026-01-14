package ru.kurs.inventory.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<Product> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(5, size));
        if (q == null || q.isBlank()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(q, q, pageable);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ru.kurs.inventory.common.EntityNotFoundException("Товар не найден: id=" + id));
    }

    public Product create(Product product, Long categoryId) {
        if (categoryId != null) {
            product.setCategory(categoryRepository.findById(categoryId).orElseThrow(
                    () -> new IllegalArgumentException("Категория не найдена: id=" + categoryId)
            ));
        } else {
            product.setCategory(null);
        }
        return productRepository.save(product);
    }

    public Product update(Long id, Product payload, Long categoryId) {
        Product existing = getById(id);
        existing.setSku(payload.getSku());
        existing.setName(payload.getName());
        existing.setDescription(payload.getDescription());
        existing.setUnit(payload.getUnit());
        existing.setMinStock(payload.getMinStock());
        existing.setPrice(payload.getPrice());
        existing.setActive(payload.isActive());

        if (categoryId != null) {
            existing.setCategory(categoryRepository.findById(categoryId).orElseThrow(
                    () -> new IllegalArgumentException("Категория не найдена: id=" + categoryId)
            ));
        } else {
            existing.setCategory(null);
        }

        return productRepository.save(existing);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySkuIgnoreCase(sku);
    }

    @Transactional(readOnly = true)
    public java.util.List<Product> findAllActive() {
        return productRepository.findByActiveTrueOrderByNameAsc();
    }
}
