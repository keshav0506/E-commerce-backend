package com.keshav.service;

import com.keshav.dto.ProductRequestDTO;
import com.keshav.dto.ProductResponseDTO;
import com.keshav.entity.Category;
import com.keshav.entity.Product;
import com.keshav.exception.CategoryNotFoundException;
import com.keshav.exception.ProductNotFoundException;
import com.keshav.repository.CategoryRepository;
import com.keshav.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ProductResponseDTO saveProduct(ProductRequestDTO productDTO) {

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found with id: "
                                        + productDTO.getCategoryId()
                        )
                );

        Product product = new Product();

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setImage(productDTO.getImage());
        product.setStatus(productDTO.getStatus());
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        return convertToResponseDTO(savedProduct);
    }

    @Override
    public Page<ProductResponseDTO> getAllProducts(
            String search,
            Long categoryId,
            Pageable pageable) {

        Page<Product> products;

        if (search != null && !search.isBlank() && categoryId != null) {

            products = productRepository
                    .findByNameContainingIgnoreCaseAndCategoryId(
                            search,
                            categoryId,
                            pageable
                    );

        } else if (search != null && !search.isBlank()) {

            products = productRepository
                    .findByNameContainingIgnoreCase(
                            search,
                            pageable
                    );

        } else if (categoryId != null) {

            products = productRepository
                    .findByCategoryId(
                            categoryId,
                            pageable
                    );

        } else {

            products = productRepository.findAll(pageable);
        }

        return products.map(this::convertToResponseDTO);
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        )
                );

        return convertToResponseDTO(product);
    }

    @Override
    public ProductResponseDTO updateProduct(
            Long id,
            ProductRequestDTO productDTO) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        )
                );

        Category category = categoryRepository.findById(
                productDTO.getCategoryId()
        ).orElseThrow(() ->
                new CategoryNotFoundException(
                        "Category not found with id: "
                                + productDTO.getCategoryId()
                )
        );

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());
        existingProduct.setImage(productDTO.getImage());
        existingProduct.setStatus(productDTO.getStatus());
        existingProduct.setCategory(category);

        Product updatedProduct =
                productRepository.save(existingProduct);

        return convertToResponseDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(
                    "Product not found with id: " + id
            );
        }

        productRepository.deleteById(id);
    }

    private ProductResponseDTO convertToResponseDTO(Product product) {

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImage(),
                product.getStatus(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }
}