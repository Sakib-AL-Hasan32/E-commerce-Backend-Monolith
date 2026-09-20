package com.db_migration.inventory.service.impl;

import com.db_migration.common.constants.ApiMessages;
import com.db_migration.common.constants.PermissionNames;
import com.db_migration.common.exception.ResourceConflictException;
import com.db_migration.common.exception.ResourceNotFound;
import com.db_migration.common.response.ApiResponse;
import com.db_migration.common.response.PageResponse;
import com.db_migration.inventory.dto.request.InventoryAdjustRequest;
import com.db_migration.inventory.dto.request.InventoryQuantityRequest;
import com.db_migration.inventory.dto.response.InventoryResponse;
import com.db_migration.inventory.entity.Inventory;
import com.db_migration.inventory.repository.InventoryRepository;
import com.db_migration.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
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
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.INCREASE_INVENTORY + "')")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "product",  allEntries = true),
                    @CacheEvict(cacheNames = "productList", allEntries = true)
            }
    )
    public ApiResponse<InventoryResponse> increase(InventoryQuantityRequest inventoryQuantityRequest) {

        Inventory inventory = inventoryRepository.findByProductId(inventoryQuantityRequest.productId()).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.INVENTORY_NOT_FOUND));

        inventory.setTotalQuantity(inventory.getTotalQuantity() + inventoryQuantityRequest.quantity());
        Inventory saved = inventoryRepository.save(inventory);

        InventoryResponse response = mapToResponse(saved);
        return ApiResponse.<InventoryResponse>builder()
                .data(response)
                .message(ApiMessages.Success.INVENTORY_INCREASED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.DECREASE_INVENTORY + "')")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "product",  allEntries = true),
                    @CacheEvict(cacheNames = "productList", allEntries = true)
            }
    )
    public ApiResponse<InventoryResponse> decrease(InventoryQuantityRequest inventoryQuantityRequest) {

        Inventory inventory = inventoryRepository.findByProductId(inventoryQuantityRequest.productId()).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.INVENTORY_NOT_FOUND));

        int newTotalQuantity = inventory.getTotalQuantity() - inventoryQuantityRequest.quantity();

        if (newTotalQuantity < inventory.getReservedQuantity()) {
            throw new ResourceConflictException(ApiMessages.Error.INVENTORY_DECREASED_FAILED);
        }

        inventory.setTotalQuantity(newTotalQuantity);
        Inventory saved = inventoryRepository.save(inventory);

        InventoryResponse response = mapToResponse(saved);
        return ApiResponse.<InventoryResponse>builder()
                .data(response)
                .message(ApiMessages.Success.INVENTORY_DECREASED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.RESERVE_INVENTORY + "')")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "product",  allEntries = true),
                    @CacheEvict(cacheNames = "productList", allEntries = true)
            }
    )
    public ApiResponse<InventoryResponse> reserve(InventoryQuantityRequest inventoryQuantityRequest) {

        Inventory inventory = inventoryRepository.findByProductId(inventoryQuantityRequest.productId()).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.INVENTORY_NOT_FOUND));

        if(inventoryQuantityRequest.quantity() > inventory.getAvailableQuantity()) {
            throw new ResourceConflictException(ApiMessages.Error.INVENTORY_RESERVED_FAILED);
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + inventoryQuantityRequest.quantity());
        Inventory saved = inventoryRepository.save(inventory);

        InventoryResponse response = mapToResponse(saved);
        return ApiResponse.<InventoryResponse>builder()
                .data(response)
                .message(ApiMessages.Success.INVENTORY_RESERVED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.RELEASE_INVENTORY + "')")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "product",  allEntries = true),
                    @CacheEvict(cacheNames = "productList", allEntries = true)
            }
    )
    public ApiResponse<InventoryResponse> release(InventoryQuantityRequest inventoryQuantityRequest) {

        Inventory inventory = inventoryRepository.findByProductId(inventoryQuantityRequest.productId()).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.INVENTORY_NOT_FOUND));

        if(inventoryQuantityRequest.quantity() > inventory.getReservedQuantity()) {
            throw new ResourceConflictException(ApiMessages.Error.INVENTORY_RELEASED_FAILED);
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - inventoryQuantityRequest.quantity());
        Inventory saved = inventoryRepository.save(inventory);
        InventoryResponse response = mapToResponse(saved);

        return ApiResponse.<InventoryResponse>builder()
                .data(response)
                .message(ApiMessages.Success.INVENTORY_RELEASED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.VIEW_INVENTORY + "')")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "product",  allEntries = true),
                    @CacheEvict(cacheNames = "productList", allEntries = true)
            }
    )
    public ApiResponse<PageResponse<InventoryResponse>> getAll(Pageable pageable) {
        Page<Inventory> page = inventoryRepository.findAll(pageable);
        List<InventoryResponse> responses = new ArrayList<>();

        for(Inventory inventory : page.getContent()) {
            InventoryResponse response = mapToResponse(inventory);
            responses.add(response);
        }

        PageResponse<InventoryResponse> pageResponse = PageResponse.<InventoryResponse>builder()
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
        return ApiResponse.<PageResponse<InventoryResponse>>builder()
                .data(pageResponse)
                .message(ApiMessages.Success.INVENTORY_FETCHED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.ADJUST_INVENTORY + "')")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "product",  allEntries = true),
                    @CacheEvict(cacheNames = "productList", allEntries = true)
            }
    )
    public ApiResponse<InventoryResponse> adjust(InventoryAdjustRequest inventoryAdjustRequest) {

        Inventory inventory = inventoryRepository.findByProductId(inventoryAdjustRequest.productId()).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.INVENTORY_NOT_FOUND));

        inventory.setTotalQuantity(inventoryAdjustRequest.quantity());
        Inventory saved = inventoryRepository.save(inventory);
        InventoryResponse response = mapToResponse(saved);

        return ApiResponse.<InventoryResponse>builder()
                .data(response)
                .message(ApiMessages.Success.INVENTORY_ADJUSTED)
                .build();
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getProduct().getName(),
                inventory.getTotalQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getCreatedAt(),
                inventory.getCreatedBy().getUsername(),
                inventory.getUpdatedAt(),
                inventory.getUpdatedBy().getUsername()
        );
    }
}
