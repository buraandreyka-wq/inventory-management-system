package ru.kurs.inventory.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySkuIgnoreCase(String sku);

    /**
     * Подгружаем category сразу, т.к. open-in-view выключен и view (Thymeleaf) не должен триггерить lazy loading.
     */
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Pageable pageable);

    /**
     * Подгружаем category сразу, т.к. open-in-view выключен и view (Thymeleaf) не должен триггерить lazy loading.
     */
    @EntityGraph(attributePaths = "category")
    Page<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku, Pageable pageable);

    List<Product> findByActiveTrueOrderByNameAsc();
}
