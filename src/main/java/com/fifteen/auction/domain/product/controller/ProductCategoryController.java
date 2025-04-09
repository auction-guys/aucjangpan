package com.fifteen.auction.domain.product.controller;

import com.fifteen.auction.domain.product.dto.request.ProductCategoryCreateRequest;
import com.fifteen.auction.domain.product.dto.request.ProductCategoryTreeResponse;
import com.fifteen.auction.domain.product.dto.request.ProductCategoryUpdateRequest;
import com.fifteen.auction.domain.product.dto.response.ProductCategoryResponse;
import com.fifteen.auction.domain.product.service.ProductCategoryService;
import com.fifteen.auction.global.dto.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @PostMapping
    public ResponseEntity<Response<Long>> createCategory(@RequestBody @Valid ProductCategoryCreateRequest request) {
        Long id = categoryService.createCategory(request);
        return ResponseEntity.ok(Response.of(id));
    }

    @GetMapping
    public ResponseEntity<Response<List<ProductCategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(Response.of(categoryService.findAll()));
    }

    @GetMapping("/{parentId}")
    public ResponseEntity<Response<List<ProductCategoryResponse>>> getByParentId(@PathVariable Long parentId) {
        return ResponseEntity.ok(Response.of(categoryService.findByParentId(parentId)));
    }

    @GetMapping("/tree")
    public ResponseEntity<Response<List<ProductCategoryTreeResponse>>> getCategoryTree() {
        return ResponseEntity.ok(Response.of(categoryService.getCategoryTree()));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody @Valid ProductCategoryUpdateRequest request
    ) {
        categoryService.updateCategory(categoryId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.softDelete(categoryId);
        return ResponseEntity.noContent().build();
    }
}