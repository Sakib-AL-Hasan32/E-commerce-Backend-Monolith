package com.db_migration.order.controller;

import com.db_migration.common.constants.ApiEndpoints;
import com.db_migration.common.response.ApiResponse;
import com.db_migration.order.dto.request.OrderCreateRequest;
import com.db_migration.order.dto.response.OrderResponse;
import com.db_migration.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiEndpoints.Order.BASE)
public class OrderController {
    private final OrderService orderService;

    @Operation(
            summary = "Place order",
            description = "Creates a new order for the authenticated user using the items currently in their cart.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Order details required to place the order.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "Place order",
                                    value = """
                                        {
                                          "shippingAddress": "Dhaka, Bangladesh",
                                          "paymentMethod": "CASH_ON_DELIVERY"
                                        }
                                        """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Order placed successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Place Order",
                                            value = """
                                                    {
                                                        "data": {
                                                            "id": 1,
                                                            "orderNumber": "ORD-D466953F",
                                                            "status": "PENDING",
                                                            "totalAmount": 89.97,
                                                            "shippingAddress": "Gazipur, Dhaka",
                                                            "paymentMethod": "CASH_ON_DELIVERY",
                                                            "items": [
                                                                {
                                                                    "id": 1,
                                                                    "productId": 1,
                                                                    "productName": "Wireless Mouse",
                                                                    "unitPrice": 29.99,
                                                                    "quantity": 3,
                                                                    "subTotal": 89.97
                                                                }
                                                            ],
                                                            "createdAt": "2026-09-21T01:16:50.6770896"
                                                        },
                                                        "message": "Order confirmed"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid order request",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required or the access token is invalid",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "User, cart, or product not found",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Cart is empty, product is inactive, or requested quantity is unavailable",
                            content = @Content
                    )
            }
    )
    @PostMapping(ApiEndpoints.Order.PLACE_ORDER)
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.placeOrder(userDetails, request));
    }

    @Operation(
            summary = "Cancel order",
            description = "Cancels an existing order belonging to the authenticated user.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Order cancelled successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Cancel Order",
                                            value = """
                                                    {
                                                        "message": "Order cancelled"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required or the access token is invalid",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Order not found",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Order cannot be cancelled in its current status",
                            content = @Content
                    )
            }
    )
    @PatchMapping(ApiEndpoints.Order.CANCEL_ORDER)
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.cancelOrder(userDetails, orderId));
    }

    @Operation(
            summary = "Get all orders",
            description = "Retrieves all orders placed by the authenticated user.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Orders retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Get All Order",
                                            value = """
                                                    {
                                                        "data": [
                                                            {
                                                                "id": 1,
                                                                "orderNumber": "ORD-D466953F",
                                                                "status": "PENDING",
                                                                "totalAmount": 89.97,
                                                                "shippingAddress": "Gazipur, Dhaka",
                                                                "paymentMethod": "CASH_ON_DELIVERY",
                                                                "items": [
                                                                    {
                                                                        "id": 1,
                                                                        "productId": 1,
                                                                        "productName": "Wireless Mouse",
                                                                        "unitPrice": 29.99,
                                                                        "quantity": 3,
                                                                        "subTotal": 89.97
                                                                    }
                                                                ],
                                                                "createdAt": "2026-09-21T01:16:51"
                                                            }
                                                        ],
                                                        "message": "Order fetched"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required or the access token is invalid",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content
                    )
            }
    )
    @GetMapping(ApiEndpoints.Order.GET_ALL)
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getAll(userDetails));
    }
}
