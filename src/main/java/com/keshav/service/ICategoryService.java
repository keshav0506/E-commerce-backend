package com.keshav.service;

import com.keshav.dto.CategoryRequestDTO;
import com.keshav.dto.CategoryResponseDTO;

import java.util.List;

public interface ICategoryService {

    CategoryResponseDTO saveCategory(CategoryRequestDTO category);

    List<CategoryResponseDTO> getAllCategories();

    CategoryResponseDTO getCategoryById(Long id);

    CategoryResponseDTO updateCategory(Long id,
                                       CategoryRequestDTO category);

    void deleteCategory(Long id);
}