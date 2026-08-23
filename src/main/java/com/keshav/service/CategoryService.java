package com.keshav.service;

import com.keshav.dto.CategoryRequestDTO;
import com.keshav.dto.CategoryResponseDTO;
import com.keshav.entity.Category;
import com.keshav.exception.CategoryNotFoundException;
import com.keshav.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryResponseDTO saveCategory(CategoryRequestDTO dto) {

        Category category = new Category();

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setImage(dto.getImage());
        category.setStatus(dto.getStatus());

        Category savedCategory = categoryRepository.save(category);

        return convertToResponseDTO(savedCategory);
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found with id: " + id
                        ));

        return convertToResponseDTO(category);
    }

    @Override
    public CategoryResponseDTO updateCategory(
            Long id,
            CategoryRequestDTO dto) {

        Category existingCategory =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new CategoryNotFoundException(
                                        "Category not found with id: " + id
                                ));

        existingCategory.setName(dto.getName());
        existingCategory.setDescription(dto.getDescription());
        existingCategory.setImage(dto.getImage());
        existingCategory.setStatus(dto.getStatus());

        Category updatedCategory =
                categoryRepository.save(existingCategory);

        return convertToResponseDTO(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found with id: " + id
                        ));

        categoryRepository.delete(category);
    }

    private CategoryResponseDTO convertToResponseDTO(
            Category category) {

        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getImage(),
                category.getStatus()
        );
    }
}