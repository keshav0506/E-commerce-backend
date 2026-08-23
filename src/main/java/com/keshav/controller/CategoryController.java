package com.keshav.controller;

import com.keshav.dto.CategoryRequestDTO;
import com.keshav.dto.CategoryResponseDTO;
import com.keshav.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(
            CategoryService categoryService) {

        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>>
    getAllCategories() {

        return ResponseEntity.ok(
                categoryService.getAllCategories()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO>
    getCategoryById(@PathVariable Long id) {

        return ResponseEntity.ok(
                categoryService.getCategoryById(id)
        );
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO>
    createCategory(
            @Valid
            @RequestBody
            CategoryRequestDTO category) {

        CategoryResponseDTO savedCategory =
                categoryService.saveCategory(category);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO>
    updateCategory(
            @PathVariable Long id,
            @Valid
            @RequestBody
            CategoryRequestDTO category) {

        CategoryResponseDTO updatedCategory =
                categoryService.updateCategory(id, category);

        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteCategory(@PathVariable Long id) {

        categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }
}