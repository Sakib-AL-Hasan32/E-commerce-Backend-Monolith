package com.db_migration.product.controller;

import com.db_migration.common.constants.ApiEndpoints;
import com.db_migration.common.response.ApiResponse;
import com.db_migration.common.response.PageResponse;
import com.db_migration.product.dto.request.ProductCreateRequest;
import com.db_migration.product.dto.request.ProductUpdateRequest;
import com.db_migration.product.dto.response.ProductResponse;
import com.db_migration.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiEndpoints.Product.BASE)
public class ProductController {
    private final ProductService productService;

    @Operation(
            summary = "Create product",
            description = "Creates a new product with the provided product details.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Product creation details.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "Create product",
                                    value = """
                                           {
                                              "name": "Laptop",
                                              "description": "High performance laptop",
                                              "price": 259.99,
                                              "sku": "KH-002-TLK",
                                              "active": true,
                                              "categoryId": 1
                                           }
                                        """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "201",
                            description = "Product created successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Product Create",
                                            value = """
                                                    {
                                                        "data": {
                                                            "id": 2,
                                                            "name": "Laptop",
                                                            "description": "High performance laptop",
                                                            "price": 259.99,
                                                            "quantity": 0,
                                                            "sku": "KH-002-TLK",
                                                            "active": true,
                                                            "categoryName": "Electronics"
                                                        },
                                                        "message": "Product created!"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid product creation request",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized or invalid access token",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Product already exists",
                            content = @Content
                    )
            }
    )
    @PostMapping(ApiEndpoints.Product.CREATE)
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductCreateRequest productCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(productCreateRequest));
    }

    @Operation(
            summary = "Get all products",
            description = "Retrieves a paginated list of all products.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Page number, starting from 0.",
                            in = ParameterIn.QUERY,
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Number of products per page.",
                            in = ParameterIn.QUERY,
                            example = "5"
                    )
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Products retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Get All Product",
                                            value = """
                                                    {
                                                        "data": {
                                                            "content": [
                                                                {
                                                                    "id": 1,
                                                                    "name": "Wireless Mouse 2",
                                                                    "description": "Ergonomic wireless mouse with Bluetooth connectivity.",
                                                                    "price": 29.99,
                                                                    "quantity": 0,
                                                                    "sku": "WM-001-BLK",
                                                                    "active": true,
                                                                    "categoryName": "Electronics"
                                                                },
                                                                {
                                                                    "id": 2,
                                                                    "name": "Laptop",
                                                                    "description": "High performance laptop",
                                                                    "price": 259.99,
                                                                    "quantity": 0,
                                                                    "sku": "KH-002-TLK",
                                                                    "active": true,
                                                                    "categoryName": "Electronics"
                                                                }
                                                            ],
                                                            "page": 1,
                                                            "size": 5,
                                                            "totalElements": 2,
                                                            "totalPages": 1,
                                                            "first": true,
                                                            "last": true,
                                                            "prev": false,
                                                            "next": false
                                                        },
                                                        "message": "Product fetched!"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized or invalid access token",
                            content = @Content
                    )
            }
    )
    @GetMapping(ApiEndpoints.Product.GET_ALL)
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAll(@Parameter(hidden = true) @PageableDefault(size = 5, sort = "id") Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getAll(pageable));
    }

    @Operation(
            summary = "Update product",
            description = "Updates an existing product with the provided product details.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Updated product details.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "Update product",
                                    value = """
                                           {
                                              "name": "Laptop",
                                              "description": "High Performance Laptop",
                                              "price": 229.99,
                                              "sku": "KM-001-TLK",
                                              "active": true,
                                              "categoryId": 1
                                           }
                                        """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Product updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples =  @ExampleObject(
                                            name = "Update Product",
                                            value = """
                                                    {
                                                       "data": {
                                                          "id": 2,
                                                          "name": "Laptop",
                                                          "description": "High Performance Laptop",
                                                          "price": 229.99,
                                                          "quantity": 0,
                                                          "sku": "KM-001-TLK",
                                                          "active": true,
                                                          "categoryName": "Electronics"
                                                       },
                                                       "message": "Product updated!"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid product update request",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized or invalid access token",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Product not found",
                            content = @Content
                    )
            }
    )
    @PutMapping(ApiEndpoints.Product.UPDATE)
    public ResponseEntity<ApiResponse<ProductResponse>> update(@Valid @RequestBody ProductUpdateRequest productUpdateRequest, @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.update(productUpdateRequest, id));
    }

    @Operation(
            summary = "Delete product",
            description = "Deletes an existing product by its ID.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Product deleted successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Delete Product",
                                            value = """
                                                    {
                                                        "message": "Product deleted!"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized or invalid access token",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Product not found",
                            content = @Content
                    )
            }
    )
    @DeleteMapping(ApiEndpoints.Product.DELETE)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.delete(id));
    }
}