package com.keshav.controller;

import com.keshav.dto.ProductRequestDTO;
import com.keshav.dto.ProductResponseDTO;
import com.keshav.service.IProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> saveProduct(
            @Valid @RequestBody ProductRequestDTO productDTO) {

        ProductResponseDTO savedProduct = productService.saveProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean featured,
            @PageableDefault(size = 200) Pageable pageable) {

        return ResponseEntity.ok(
                productService.getAllProducts(search, categoryId, featured, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponseDTO> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 200) Pageable pageable) {

        return ResponseEntity.ok(
                productService.getProductsByCategory(category, pageable)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchProducts(
            @RequestParam(required = false, name = "query") String query,
            @RequestParam(required = false, name = "search") String search,
            @PageableDefault(size = 200) Pageable pageable) {

        String searchTerm = query != null ? query : search;
        return ResponseEntity.ok(
                productService.searchProducts(searchTerm, pageable)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO productDTO) {

        return ResponseEntity.ok(
                productService.updateProduct(id, productDTO)
        );
    }

    @GetMapping("/{id}/emi-plans")
    public ResponseEntity<com.keshav.dto.EmiPlanDTO> getEmiPlans(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        double price = product.getPrice();

        java.util.List<com.keshav.dto.EmiPlanDTO.EmiOptionDTO> plans = new java.util.ArrayList<>();
        String[] banks = {"HDFC Bank", "ICICI Bank", "State Bank of India (SBI)", "Axis Bank", "Kotak Mahindra Bank"};

        for (String bank : banks) {
            // 3-Month No Cost EMI
            double emi3 = Math.round((price / 3.0) * 100.0) / 100.0;
            plans.add(com.keshav.dto.EmiPlanDTO.EmiOptionDTO.builder()
                    .bankName(bank)
                    .cardType("Credit Card")
                    .tenureMonths(3)
                    .interestRate(0.0)
                    .monthlyInstallment(emi3)
                    .totalPayable(price)
                    .processingFee(0.0)
                    .isNoCost(true)
                    .build());

            // 6-Month Plan (No cost if price >= 2000, else 13.5%)
            boolean is6NoCost = price >= 2000;
            double rate6 = is6NoCost ? 0.0 : 13.5;
            double total6 = is6NoCost ? price : Math.round(price * (1 + (rate6 / 100.0) * (6.0 / 12.0)));
            double emi6 = Math.round((total6 / 6.0) * 100.0) / 100.0;
            plans.add(com.keshav.dto.EmiPlanDTO.EmiOptionDTO.builder()
                    .bankName(bank)
                    .cardType("Credit Card")
                    .tenureMonths(6)
                    .interestRate(rate6)
                    .monthlyInstallment(emi6)
                    .totalPayable(total6)
                    .processingFee(is6NoCost ? 0.0 : 99.0)
                    .isNoCost(is6NoCost)
                    .build());

            // 9-Month Plan
            double rate9 = 14.5;
            double total9 = Math.round(price * (1 + (rate9 / 100.0) * (9.0 / 12.0)));
            double emi9 = Math.round((total9 / 9.0) * 100.0) / 100.0;
            plans.add(com.keshav.dto.EmiPlanDTO.EmiOptionDTO.builder()
                    .bankName(bank)
                    .cardType("Credit Card")
                    .tenureMonths(9)
                    .interestRate(rate9)
                    .monthlyInstallment(emi9)
                    .totalPayable(total9)
                    .processingFee(149.0)
                    .isNoCost(false)
                    .build());

            // 12-Month Plan
            double rate12 = 15.0;
            double total12 = Math.round(price * (1 + (rate12 / 100.0) * (12.0 / 12.0)));
            double emi12 = Math.round((total12 / 12.0) * 100.0) / 100.0;
            plans.add(com.keshav.dto.EmiPlanDTO.EmiOptionDTO.builder()
                    .bankName(bank)
                    .cardType("Credit Card")
                    .tenureMonths(12)
                    .interestRate(rate12)
                    .monthlyInstallment(emi12)
                    .totalPayable(total12)
                    .processingFee(199.0)
                    .isNoCost(false)
                    .build());
        }

        double minEmi = plans.stream()
                .mapToDouble(opt -> opt.getMonthlyInstallment())
                .min()
                .orElse(Math.round(price / 12.0));

        com.keshav.dto.EmiPlanDTO result = com.keshav.dto.EmiPlanDTO.builder()
                .productPrice(price)
                .minEmiAmount(minEmi)
                .bestTenureText("From ₹" + Math.round(minEmi) + "/month • No Cost EMI Available")
                .plans(plans)
                .build();

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}