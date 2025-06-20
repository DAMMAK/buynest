package dev.dammak.productservice.service;



import dev.dammak.productservice.dto.CategoryDto;
import dev.dammak.productservice.entity.Category;
import dev.dammak.productservice.exception.ProductException;
import dev.dammak.productservice.mapper.CategoryMapper;
import dev.dammak.productservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Cacheable(value = "categories", key = "#id")
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ProductException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }

    @Cacheable(value = "categories")
    public List<CategoryDto> getAllActiveCategories() {
        return categoryMapper.toDtoList(categoryRepository.findByActiveTrue());
    }

    public Page<CategoryDto> getAllCategories(Pageable pageable) {
        return categoryRepository.findByActiveTrue(pageable)
                .map(categoryMapper::toDto);
    }

    public List<CategoryDto> getCategoriesByParent(Long parentId) {
        return categoryMapper.toDtoList(categoryRepository.findByParentIdOrderByDisplayOrder(parentId));
    }

    public Page<CategoryDto> searchCategories(String keyword, Pageable pageable) {
        return categoryRepository.searchCategories(keyword, pageable)
                .map(categoryMapper::toDto);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto createCategory(CategoryDto categoryDto) {
        if (categoryRepository.existsByNameIgnoreCase(categoryDto.getName())) {
            throw new ProductException("Category with name '" + categoryDto.getName() + "' already exists");
        }

        Category category = categoryMapper.toEntity(categoryDto);
        category = categoryRepository.save(category);
        log.info("Created new category with id: {}", category.getId());
       return categoryMapper.toDto(category);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ProductException("Category not found with id: " + id));

        if (!existingCategory.getName().equalsIgnoreCase(categoryDto.getName()) &&
                categoryRepository.existsByNameIgnoreCase(categoryDto.getName())) {
            throw new ProductException("Category with name '" + categoryDto.getName() + "' already exists");
        }

        existingCategory.setName(categoryDto.getName());
        existingCategory.setDescription(categoryDto.getDescription());
        existingCategory.setParentId(categoryDto.getParentId());
        existingCategory.setDisplayOrder(categoryDto.getDisplayOrder());
        existingCategory.setActive(categoryDto.getActive());

        existingCategory = categoryRepository.save(existingCategory);
        log.info("Updated category with id: {}", existingCategory.getId());

        return categoryMapper.toDto(existingCategory);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ProductException("Category not found with id: " + id));

        category.setActive(false);
        categoryRepository.save(category);
        log.info("Soft deleted category with id: {}", id);
    }
}