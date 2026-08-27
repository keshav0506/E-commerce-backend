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

import java.util.Locale;

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
                                "Category not found with id: " + productDTO.getCategoryId()
                        )
                );

        Product product = new Product();
        mapDtoToEntity(productDTO, product, category);

        Product savedProduct = productRepository.save(product);
        return convertToResponseDTO(savedProduct);
    }

    @Override
    public Page<ProductResponseDTO> getAllProducts(
            String search,
            Long categoryId,
            Boolean featured,
            Pageable pageable) {

        Page<Product> products;

        if (featured != null && featured) {
            products = productRepository.findByFeaturedTrue(pageable);
        } else if (search != null && !search.isBlank() && categoryId != null) {
            products = productRepository.findByNameContainingIgnoreCaseAndCategoryId(
                    search.trim(),
                    categoryId,
                    pageable
            );
        } else if (search != null && !search.isBlank()) {
            products = productRepository.searchAcrossFields(search.trim(), pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products.map(this::convertToResponseDTO);
    }

    @Override
    public Page<ProductResponseDTO> getProductsByCategory(String categoryOrId, Pageable pageable) {
        if (categoryOrId == null || categoryOrId.isBlank() || categoryOrId.equalsIgnoreCase("all")) {
            return productRepository.findAll(pageable).map(this::convertToResponseDTO);
        }

        if (categoryOrId.equalsIgnoreCase("for-you") || categoryOrId.equalsIgnoreCase("foryou")) {
            return productRepository.findByFeaturedTrue(pageable).map(this::convertToResponseDTO);
        }

        // Try numeric ID first
        try {
            Long catId = Long.parseLong(categoryOrId.trim());
            return productRepository.findByCategoryId(catId, pageable).map(this::convertToResponseDTO);
        } catch (NumberFormatException e) {
            // Match by normalized category name (e.g. personal-care -> Personal Care)
            String normalized = categoryOrId.trim().replace('-', ' ');
            Page<Product> prods = productRepository.findByCategoryNameIgnoreCase(normalized, pageable);
            if (prods.isEmpty()) {
                prods = productRepository.findByCategoryNameIgnoreCase(categoryOrId.trim(), pageable);
            }
            return prods.map(this::convertToResponseDTO);
        }
    }

    @Override
    public Page<ProductResponseDTO> searchProducts(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return productRepository.findAll(pageable).map(this::convertToResponseDTO);
        }
        return productRepository.searchAcrossFields(query.trim(), pageable).map(this::convertToResponseDTO);
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
    public ProductResponseDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with slug: " + slug
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

        Category category = existingProduct.getCategory();
        if (productDTO.getCategoryId() != null) {
            category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() ->
                            new CategoryNotFoundException(
                                    "Category not found with id: " + productDTO.getCategoryId()
                            )
                    );
        }

        mapDtoToEntity(productDTO, existingProduct, category);

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToResponseDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        )
                );

        productRepository.delete(product);
    }

    private void mapDtoToEntity(ProductRequestDTO dto, Product entity, Category category) {
        if (dto.getSku() != null && !dto.getSku().isBlank()) {
            entity.setSku(dto.getSku().trim());
        } else if (entity.getSku() == null) {
            entity.setSku("SKU-" + System.currentTimeMillis());
        }

        entity.setName(dto.getName());

        if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
            entity.setSlug(dto.getSlug().trim().toLowerCase(Locale.ROOT));
        } else if (entity.getSlug() == null && dto.getName() != null) {
            entity.setSlug(dto.getName().trim().toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-|-$", ""));
        }

        entity.setDescription(dto.getDescription());
        entity.setShortDescription(dto.getShortDescription());
        entity.setBrand(dto.getBrand());
        entity.setPrice(dto.getPrice());
        entity.setDiscountPrice(dto.getDiscountPrice());
        entity.setStock(dto.getStock());
        if (dto.getLowStockThreshold() != null) {
            entity.setLowStockThreshold(dto.getLowStockThreshold());
        }
        if (dto.getImage() != null) {
            entity.setImage(dto.getImage());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getRating() != null) {
            entity.setRating(dto.getRating());
        }
        if (dto.getReviewCount() != null) {
            entity.setReviewCount(dto.getReviewCount());
        }
        if (dto.getFeatured() != null) {
            entity.setFeatured(dto.getFeatured());
        }
        entity.setCategory(category);
    }

    public ProductResponseDTO convertToResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setDescription(product.getDescription());
        dto.setShortDescription(product.getShortDescription());
        dto.setBrand(product.getBrand());
        dto.setPrice(product.getPrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setStock(product.getStock());
        dto.setLowStockThreshold(product.getLowStockThreshold());
        dto.setImage(product.getImage());
        dto.setStatus(product.getStatus());
        dto.setRating(product.getRating());
        dto.setReviewCount(product.getReviewCount());
        dto.setFeatured(product.getFeatured());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
            dto.setCategorySlug(product.getCategory().getName().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-"));
        }

        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        return dto;
    }
}