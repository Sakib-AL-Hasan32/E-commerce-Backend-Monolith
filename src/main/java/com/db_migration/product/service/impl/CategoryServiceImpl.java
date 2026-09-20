package com.db_migration.product.service.impl;

import com.db_migration.common.constants.ApiMessages;
import com.db_migration.common.constants.PermissionNames;
import com.db_migration.common.exception.ResourceAlreadyExistsException;
import com.db_migration.common.exception.ResourceNotFound;
import com.db_migration.common.response.ApiResponse;
import com.db_migration.common.response.PageResponse;
import com.db_migration.product.dto.request.CategoryCreateRequest;
import com.db_migration.product.dto.request.CategoryUpdateRequest;
import com.db_migration.product.dto.response.CategoryResponse;
import com.db_migration.product.entity.Category;
import com.db_migration.product.repository.CategoryRepository;
import com.db_migration.product.service.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.CREATE_CATEGORY + "')")
    @CacheEvict(cacheNames = "categoryList", allEntries = true)
    public ApiResponse<CategoryResponse> create(CategoryCreateRequest categoryCreateRequest) {
        if(categoryRepository.existsByName(categoryCreateRequest.name())) {
            throw new ResourceAlreadyExistsException(ApiMessages.Error.CATEGORY_ALREADY_EXISTS);
        }
        Category category = Category.builder()
                .name(categoryCreateRequest.name())
                .description(categoryCreateRequest.description())
                .build();
        Category saved = categoryRepository.save(category);

        CategoryResponse response = new CategoryResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.isActive(),
                saved.getCreatedAt(),
                saved.getCreatedBy().getUsername(),
                saved.getUpdatedAt(),
                saved.getUpdatedBy().getUsername()
        );
        return ApiResponse.<CategoryResponse>builder()
                .data(response)
                .message(ApiMessages.Success.CATEGORY_CREATED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.VIEW_CATEGORY + "')")
    @Cacheable(cacheNames = "categoryList", key = "#pageable")
    public ApiResponse<PageResponse<CategoryResponse>> getAll(Pageable pageable) {
        Page<Category> page = categoryRepository.findAll(pageable);
        List<CategoryResponse> responses = new ArrayList<>();

        for(Category category : page.getContent()) {
            CategoryResponse response = new CategoryResponse(
                    category.getId(),
                    category.getName(),
                    category.getDescription(),
                    category.isActive(),
                    category.getCreatedAt(),
                    category.getCreatedBy().getUsername(),
                    category.getUpdatedAt(),
                    category.getUpdatedBy().getUsername()
            );
            responses.add(response);
        }
        PageResponse<CategoryResponse> pageResponse = PageResponse.<CategoryResponse>builder()
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
        return ApiResponse.<PageResponse<CategoryResponse>>builder()
                .data(pageResponse)
                .message(ApiMessages.Success.CATEGORY_FETCHED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_CATEGORY + "')")
    @Caching(
            put = {
                    @CachePut(cacheNames = "category", key = "#id")
            },
            evict = {
                    @CacheEvict(cacheNames = "categoryList", allEntries = true)
            })
    public ApiResponse<CategoryResponse> update(CategoryUpdateRequest categoryUpdateRequest, Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.CATEGORY_NOT_FOUND));
        category.setName(categoryUpdateRequest.name());
        category.setDescription(categoryUpdateRequest.description());
        Category saved = categoryRepository.save(category);
        CategoryResponse response = new CategoryResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.isActive(),
                saved.getCreatedAt(),
                saved.getCreatedBy().getUsername(),
                saved.getUpdatedAt(),
                saved.getUpdatedBy().getUsername()
        );
        return ApiResponse.<CategoryResponse>builder()
                .data(response)
                .message(ApiMessages.Success.CATEGORY_UPDATED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.DELETE_CATEGORY + "')")
    @Caching(evict = {
            @CacheEvict(cacheNames = "category", key = "#id"),
            @CacheEvict(cacheNames = "categoryList", allEntries = true)
    })
    public ApiResponse<Void> delete(Long id) {
        if(!categoryRepository.existsById(id)) {
            throw new ResourceNotFound(ApiMessages.Error.CATEGORY_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
        return ApiResponse.<Void>builder()
                .message(ApiMessages.Success.CATEGORY_DELETED)
                .build();
    }
}