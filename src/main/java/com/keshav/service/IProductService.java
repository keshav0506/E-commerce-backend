package com.keshav.service;

import com.keshav.dto.ProductRequestDTO;
import com.keshav.dto.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {

    ProductResponseDTO saveProduct(ProductRequestDTO productDTO);

    Page<ProductResponseDTO> getAllProducts(
            String search,
            Long categoryId,
            Boolean featured,
            Pageable pageable
    );

    Page<ProductResponseDTO> getProductsByCategory(String categoryOrId, Pageable pageable);

    Page<ProductResponseDTO> searchProducts(String query, Pageable pageable);

    ProductResponseDTO getProductById(Long id);

    ProductResponseDTO getProductBySlug(String slug);

    ProductResponseDTO updateProduct(
            Long id,
            ProductRequestDTO productDTO
    );

    void deleteProduct(Long id);
}