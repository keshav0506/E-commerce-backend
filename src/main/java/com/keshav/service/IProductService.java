package com.keshav.service;

import com.keshav.dto.ProductRequestDTO;
import com.keshav.dto.ProductResponseDTO;

import java.util.List;

public interface IProductService {

    ProductResponseDTO saveProduct(ProductRequestDTO productDTO);

    List<ProductResponseDTO> getAllProducts();

    ProductResponseDTO getProductById(Long id);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO productDTO);

    void deleteProduct(Long id);
}