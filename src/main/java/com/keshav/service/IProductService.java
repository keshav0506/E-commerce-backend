package com.keshav.service;

import com.keshav.entity.Product;

import java.util.List;

public interface IProductService
{

    Product saveProduct(Product product);

    List<Product> getAllProducts();

    Product getProductById(Long id);

    Product updateProduct(Long id, Product product);

    void deleteProduct(Long id);
}
