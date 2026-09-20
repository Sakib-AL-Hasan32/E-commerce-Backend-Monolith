package com.db_migration.inventory.controller;

import com.db_migration.common.constants.ApiEndpoints;
import com.db_migration.common.response.ApiResponse;
import com.db_migration.common.response.PageResponse;
import com.db_migration.inventory.dto.request.InventoryAdjustRequest;
import com.db_migration.inventory.dto.request.InventoryQuantityRequest;
import com.db_migration.inventory.dto.response.InventoryResponse;
import com.db_migration.inventory.service.InventoryService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiEndpoints.Inventory.BASE)
public class InventoryController {
    private final InventoryService inventoryService;

    @Operation(
            summary = "Increase inventory quantity",
            description = "Increases the available quantity of a product in the inventory.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Product and quantity details for increasing inventory.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InventoryQuantityRequest.class),
                            examples = @ExampleObject(
                                    name = "Increase inventory",
                                    value = """
                                        {
                                          "productId": 1,
                                          "quantity": 10
                                        }
                                        """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Inventory quantity increased successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Increase Quantity",
                                            value = """
                                                    {
                                                        "data": {
                                                            "id": 1,
                                                            "productId": 1,
                                                            "productName": "Laptop",
                                                            "totalQuantity": 100,
                                                            "reservedQuantity": 0,
                                                            "availableQuantity": 100,
                                                            "createdAt": "2026-09-16T23:33:38",
                                                            "createdBy": "admin",
                                                            "updatedAt": "2026-09-20T22:01:08.0216099",
                                                            "updatedBy": "admin"
                                                        },
                                                        "message": "Inventory increased"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid inventory quantity request",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized or invalid access token",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Product or inventory not found",
                            content = @Content
                    )
            }
    )
    @PostMapping(ApiEndpoints.Inventory.INCREASE)
    public ResponseEntity<ApiResponse<InventoryResponse>> increase(@Valid @RequestBody InventoryQuantityRequest inventoryQuantityRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(inventoryService.increase(inventoryQuantityRequest));
    }

    @Operation(
            summary = "Decrease inventory quantity",
            description = "Decreases the available quantity of a product in the inventory.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Product and quantity details for decreasing inventory.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InventoryQuantityRequest.class),
                            examples = @ExampleObject(
                                    name = "Decrease inventory",
                                    value = """
                                        {
                                          "productId": 1,
                                          "quantity": 5
                                        }
                                        """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Inventory quantity decreased successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Decrease Quantity",
                                            value = """
                                                    {
                                                        "data": {
                                                            "id": 1,
                                                            "productId": 1,
                                                            "productName": "Laptop",
                                                            "totalQuantity": 95,
                                                            "reservedQuantity": 0,
                                                            "availableQuantity": 95,
                                                            "createdAt": "2026-09-16T23:33:38",
                                                            "createdBy": "admin",
                                                            "updatedAt": "2026-09-20T22:01:08.0216099",
                                                            "updatedBy": "admin"
                                                        },
                                                        "message": "Inventory decreased"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid inventory quantity request",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized or invalid access token",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Product or inventory not found",
                            content = @Content
                    )
            }
    )
    @PostMapping(ApiEndpoints.Inventory.DECREASE)
    public ResponseEntity<ApiResponse<InventoryResponse>> decrease(@Valid @RequestBody InventoryQuantityRequest inventoryQuantityRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(inventoryService.decrease(inventoryQuantityRequest));
    }

    @Operation(
            summary = "Reserve inventory quantity",
            description = "Reserves a specified quantity of a product in the inventory.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Product and quantity details for reserving inventory.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InventoryQuantityRequest.class),
                            examples = @ExampleObject(
                                    name = "Reserve inventory",
                                    value = """
                                        {
                                          "productId": 1,
                                          "quantity": 10
                                        }
                                        """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Inventory quantity reserved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Reserved Quantity",
                                            value = """
                                                    {
                                                        "data": {
                                                            "id": 1,
                                                            "productId": 1,
                                                            "productName": "Laptop",
                                                            "totalQuantity": 95,
                                                            "reservedQuantity": 0,
                                                            "availableQuantity": 85,
                                                            "createdAt": "2026-09-16T23:33:38",
                                                            "createdBy": "admin",
                                                            "updatedAt": "2026-09-20T22:01:08.0216099",
                                                            "updatedBy": "admin"
                                                        },
                                                        "message": "Inventory reserved"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid inventory reservation request",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized or invalid access token",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Product or inventory not found",
                            content = @Content
                    )
            }
    )
    @PostMapping(ApiEndpoints.Inventory.RESERVE)
    public ResponseEntity<ApiResponse<InventoryResponse>> reserve(@Valid @RequestBody InventoryQuantityRequest inventoryQuantityRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(inventoryService.reserve(inventoryQuantityRequest));
    }

    @Operation(
            summary = "Release reserved inventory",
            description = "Releases a previously reserved quantity of a product in the inventory.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Product and quantity details for releasing reserved inventory.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InventoryQuantityRequest.class),
                            examples = @ExampleObject(
                                    name = "Release inventory",
                                    value = """
                                        {
                                          "productId": 1,
                                          "quantity": 10
                                        }
                                        """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Reserved inventory quantity released successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Release Quantity",
                                            value = """
                                                    {
                                                        "data": {
                                                            "id": 1,
                                                            "productId": 1,
                                                            "productName": "Laptop",
                                                            "totalQuantity": 95,
                                                            "reservedQuantity": 0,
                                                            "availableQuantity": 95,
                                                            "createdAt": "2026-09-16T23:33:38",
                                                            "createdBy": "admin",
                                                            "updatedAt": "2026-09-20T22:01:08.0216099",
                                                            "updatedBy": "admin"
                                                        },
                                                        "message": "Inventory released"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid inventory release request",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized or invalid access token",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Product or inventory not found",
                            content = @Content
                    )
            }
    )
    @PostMapping(ApiEndpoints.Inventory.RELEASE)
    public ResponseEntity<ApiResponse<InventoryResponse>> release(@Valid @RequestBody InventoryQuantityRequest inventoryQuantityRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(inventoryService.release(inventoryQuantityRequest));
    }

    @Operation(
            summary = "Get all inventory",
            description = "Retrieves a paginated list of all inventory records.",
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
                            description = "Number of inventory records per page.",
                            in = ParameterIn.QUERY,
                            example = "5"
                    )
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Inventory records retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Get All Inventory",
                                            value = """
                                                    {
                                                        "data": {
                                                            "content": [
                                                                {
                                                                    "id": 1,
                                                                    "productId": 1,
                                                                    "productName": "Laptop",
                                                                    "totalQuantity": 95,
                                                                    "reservedQuantity": 10,
                                                                    "availableQuantity": 95,
                                                                    "createdAt": "2026-09-16T23:33:38",
                                                                    "createdBy": "admin",
                                                                    "updatedAt": "2026-09-20T22:13:16",
                                                                    "updatedBy": "admin"
                                                                }
                                                            ],
                                                            "page": 1,
                                                            "size": 5,
                                                            "totalElements": 1,
                                                            "totalPages": 1,
                                                            "first": true,
                                                            "last": true,
                                                            "prev": false,
                                                            "next": false
                                                        },
                                                        "message": "Inventory fetched"
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
    @GetMapping(ApiEndpoints.Inventory.GET_ALL)
    public ResponseEntity<ApiResponse<PageResponse<InventoryResponse>>> getAll(@Parameter(hidden = true) @PageableDefault(size = 5, sort = "id") Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(inventoryService.getAll(pageable));
    }

    @Operation(
            summary = "Adjust inventory quantity",
            description = "Adjusts the inventory quantity of a product based on the provided adjustment details.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Inventory adjustment details.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InventoryAdjustRequest.class),
                            examples = @ExampleObject(
                                    name = "Adjust inventory",
                                    value = """
                                        {
                                          "productId": 1,
                                          "quantity": 50
                                        }
                                        """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Inventory adjusted successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Adjust Inventory",
                                            value = """
                                                    {
                                                        "data": {
                                                            "id": 1,
                                                            "productId": 1,
                                                            "productName": "Laptop",
                                                            "totalQuantity": 50,
                                                            "reservedQuantity": 0,
                                                            "availableQuantity": 50,
                                                            "createdAt": "2026-09-16T23:33:38",
                                                            "createdBy": "admin",
                                                            "updatedAt": "2026-09-20T22:20:40.8468054",
                                                            "updatedBy": "admin"
                                                        },
                                                        "message": "Inventory adjusted"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid inventory adjustment request",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized or invalid access token",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Product or inventory not found",
                            content = @Content
                    )
            }
    )
    @PostMapping(ApiEndpoints.Inventory.ADJUST)
    public ResponseEntity<ApiResponse<InventoryResponse>> adjust(@Valid @RequestBody InventoryAdjustRequest inventoryAdjustRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(inventoryService.adjust(inventoryAdjustRequest));
    }
}
