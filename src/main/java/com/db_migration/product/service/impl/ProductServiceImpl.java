package com.db_migration.product.service.impl;

import com.db_migration.common.constants.ApiMessages;
import com.db_migration.common.constants.PermissionNames;
import com.db_migration.common.exception.ResourceAlreadyExistsException;
import com.db_migration.common.exception.ResourceNotFound;
import com.db_migration.common.response.ApiResponse;
import com.db_migration.common.response.PageResponse;
import com.db_migration.inventory.entity.Inventory;
import com.db_migration.inventory.repository.InventoryRepository;
import com.db_migration.product.dto.request.ProductCreateRequest;
import com.db_migration.product.dto.request.ProductUpdateRequest;
import com.db_migration.product.dto.response.ProductResponse;
import com.db_migration.product.entity.Category;
import com.db_migration.product.entity.Product;
import com.db_migration.product.repository.CategoryRepository;
import com.db_migration.product.repository.ProductRepository;
import com.db_migration.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.CREATE_PRODUCT + "')")
    @CacheEvict(cacheNames = "productList", allEntries = true)
    public ApiResponse<ProductResponse> create(ProductCreateRequest productCreateRequest) {

        if(productRepository.existsBySku(productCreateRequest.sku())) {
            throw new ResourceAlreadyExistsException(ApiMessages.Error.PRODUCT_ALREADY_EXISTS);
        }

        Category category = categoryRepository.findById(productCreateRequest.categoryId()).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.CATEGORY_NOT_FOUND));

        Product product = Product.builder()
                .name(productCreateRequest.name())
                .description(productCreateRequest.description())
                .price(productCreateRequest.price())
                .sku(productCreateRequest.sku())
                .active(productCreateRequest.active())
                .category(category)
                .build();
        Product saved = productRepository.save(product);

        Inventory inventory = Inventory.builder()
                .product(saved)
                .totalQuantity(0)
                .reservedQuantity(0)
                .build();
        inventoryRepository.save(inventory);

        ProductResponse response = mapToResponse(saved);

        return ApiResponse.<ProductResponse>builder()
                .data(response)
                .message(ApiMessages.Success.PRODUCT_CREATED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.VIEW_PRODUCT + "')")
    @Cacheable(cacheNames = "productList", key = "#pageable")
    public ApiResponse<PageResponse<ProductResponse>> getAll(Pageable pageable) {

        Page<Product> page = productRepository.findAll(pageable);
        List<ProductResponse> responses = new ArrayList<>();

        for(Product product : page.getContent()) {
            ProductResponse productResponses = mapToResponse(product);
            responses.add(productResponses);
        }
        PageResponse<ProductResponse> pageResponse = PageResponse.<ProductResponse>builder()
                .content(responses)
                .page(page.getTotalPages())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .prev(page.hasPrevious())
                .next(page.hasNext())
                .build();
        return ApiResponse.<PageResponse<ProductResponse>>builder()
                .data(pageResponse)
                .message(ApiMessages.Success.PRODUCT_FETCHED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_PRODUCT + "')")
    @Caching(
            put = {
                    @CachePut(cacheNames = "product", key = "#id")
    },
            evict = {
                    @CacheEvict(cacheNames = "productList", allEntries = true)
    })
    public ApiResponse<ProductResponse> update(ProductUpdateRequest productUpdateRequest, Long id) {

        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.PRODUCT_NOT_FOUND));

        Category category = categoryRepository.findById(productUpdateRequest.categoryId()).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.CATEGORY_NOT_FOUND));

        product.setName(productUpdateRequest.name());
        product.setDescription(productUpdateRequest.description());
        product.setPrice(productUpdateRequest.price());
        product.setSku(productUpdateRequest.sku());
        product.setActive(productUpdateRequest.active());
        product.setCategory(category);
        Product saved = productRepository.save(product);

        ProductResponse response = mapToResponse(saved);
        return ApiResponse.<ProductResponse>builder()
                .data(response)
                .message(ApiMessages.Success.PRODUCT_UPDATED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.DELETE_PRODUCT + "')")
    @Caching(evict = {
            @CacheEvict(cacheNames = "product", key = "#id"),
            @CacheEvict(cacheNames = "productList", allEntries = true)
    })
    public ApiResponse<Void> delete(Long id) {
        if(!productRepository.existsById(id)) {
            throw new ResourceNotFound(ApiMessages.Error.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(id);
        return ApiResponse.<Void>builder()
                .message(ApiMessages.Success.PRODUCT_DELETED)
                .build();
    }

    private ProductResponse mapToResponse(Product product) {
        Inventory inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.INVENTORY_NOT_FOUND));

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                inventory.getAvailableQuantity(),
                product.getSku(),
                product.isActive(),
                product.getCategory().getName()
        );
    }
}