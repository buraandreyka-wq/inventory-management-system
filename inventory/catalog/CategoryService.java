package ru.kurs.inventory.catalog;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: id=" + id));
    }

    public Category create(Category category) {
        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Категория с таким именем уже существует");
        }
    }

    public Category update(Long id, Category payload) {
        Category existing = getById(id);
        existing.setName(payload.getName());
        try {
            return categoryRepository.save(existing);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Категория с таким именем уже существует");
        }
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
