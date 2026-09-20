package com.db_migration.cart.controller;

import com.db_migration.cart.dto.request.CartItemCreateRequest;
import com.db_migration.cart.dto.request.CartItemUpdateRequest;
import com.db_migration.cart.dto.response.CartResponse;
import com.db_migration.cart.service.CartService;
import com.db_migration.common.constants.ApiEndpoints;
import com.db_migration.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiEndpoints.Cart.BASE)
public class CartController {
    private final CartService cartService;

    @Operation(
            summary = "Add item to cart",
            description = "Adds a product to the authenticated user's cart with the specified quantity.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Product and quantity to add to the cart.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CartItemCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "Add item to cart",
                                    value = """
                                        {
                                          "productId": 1,
                                          "quantity": 2
                                        }
                                        """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Item added to cart successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples =  @ExampleObject(
                                            name = "Add to Cart",
                                            value = """
                                                    {
                                                        "data": {
                                                            "cartId": 1,
                                                            "items": [
                                                                {
                                                                    "id": 1,
                                                                    "productId": 1,
                                                                    "productName": "Laptop",
                                                                    "unitPrice": 229.99,
                                                                    "quantity": 2,
                                                                    "subTotal": 459.98
                                                                }
                                                            ],
                                                            "totalPrice": 459.98
                                                        },
                                                        "message": "Item added"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid cart item request",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required or the access token is invalid",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Product or user cart not found",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Product is inactive or requested quantity exceeds available stock",
                            content = @Content
                    )
            }
    )
    @PostMapping(ApiEndpoints.Cart.ADD_ITEM)
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody CartItemCreateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.addItem(request, userDetails));
    }

    @Operation(
            summary = "Remove item from cart",
            description = "Removes a specific item from the authenticated user's cart.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Cart item removed successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Remove Item from Cart",
                                            value = """
                                                    {
                                                        "data": {
                                                            "cartId": 1,
                                                            "items": [],
                                                            "totalPrice": 0
                                                        },
                                                        "message": "Item deleted"
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
                            description = "Cart item or cart not found",
                            content = @Content
                    )
            }
    )
    @DeleteMapping(ApiEndpoints.Cart.REMOVE_ITEM)
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.removeItem(userDetails, id));
    }

    @Operation(
            summary = "Get cart",
            description = "Retrieves the cart and its items for the authenticated user.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Cart retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "View Cart",
                                            value = """
                                                    {
                                                        "data": {
                                                            "cartId": 1,
                                                            "items": [
                                                                {
                                                                    "id": 4,
                                                                    "productId": 1,
                                                                    "productName": "Wireless Mouse",
                                                                    "unitPrice": 29.99,
                                                                    "quantity": 3,
                                                                    "subTotal": 89.97
                                                                }
                                                            ],
                                                            "totalPrice": 89.97
                                                        },
                                                        "message": "Cart fetched"
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
                            description = "User or cart not found",
                            content = @Content
                    )
            }
    )
    @GetMapping(ApiEndpoints.Cart.GET_ALL)
    public ResponseEntity<ApiResponse<CartResponse>> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.getAll(userDetails));
    }

    @Operation(
            summary = "Increase cart item quantity",
            description = "Increases the quantity of a specific item in the authenticated user's cart.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Quantity to add to the existing cart item quantity.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CartItemUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "Increase quantity",
                                    value = """
                                        {
                                          "quantity": 2
                                        }
                                        """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Cart item quantity increased successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples =  @ExampleObject(
                                            name = "Increase Cart Item Quantity",
                                            value = """
                                                    {
                                                        "data": {
                                                            "cartId": 1,
                                                            "items": [
                                                                {
                                                                    "id": 5,
                                                                    "productId": 1,
                                                                    "productName": "Wireless Mouse",
                                                                    "unitPrice": 29.99,
                                                                    "quantity": 5,
                                                                    "subTotal": 149.95
                                                                }
                                                            ],
                                                            "totalPrice": 149.95
                                                        },
                                                        "message": "Cart updated"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid quantity or request data",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required or the access token is invalid",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Cart item or cart not found",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Requested quantity exceeds available stock",
                            content = @Content
                    )
            }
    )
    @PutMapping(ApiEndpoints.Cart.INCREASE_QUANTITY)
    public ResponseEntity<ApiResponse<CartResponse>> increaseQuantity(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @Valid @RequestBody CartItemUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.increaseQuantity(userDetails, id, request));
    }

    @Operation(
            summary = "Decrease cart item quantity",
            description = "Decreases the quantity of a specific item in the authenticated user's cart.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Quantity to subtract from the existing cart item quantity.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CartItemUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "Decrease quantity",
                                    value = """
                                        {
                                          "quantity": 1
                                        }
                                        """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Cart item quantity decreased successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples =  @ExampleObject(
                                            name = "Increase Cart Item Quantity",
                                            value = """
                                                    {
                                                        "data": {
                                                            "cartId": 1,
                                                            "items": [
                                                                {
                                                                    "id": 5,
                                                                    "productId": 1,
                                                                    "productName": "Wireless Mouse",
                                                                    "unitPrice": 29.99,
                                                                    "quantity": 4,
                                                                    "subTotal": 149.95
                                                                }
                                                            ],
                                                            "totalPrice": 149.95
                                                        },
                                                        "message": "Cart updated"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid quantity or request data",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required or the access token is invalid",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Cart item or cart not found",
                            content = @Content
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Requested quantity cannot be decreased further",
                            content = @Content
                    )
            }
    )
    @PutMapping(ApiEndpoints.Cart.DECREASE_QUANTITY)
    public ResponseEntity<ApiResponse<CartResponse>> decreaseQuantity(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @Valid @RequestBody CartItemUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.decreaseQuantity(userDetails, id, request));
    }

    @Operation(
            summary = "Clear cart",
            description = "Removes all items from the authenticated user's cart.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Cart cleared successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Clear Cart",
                                            value = """
                                                    {
                                                        "message": "Cart deleted"
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
                            description = "User or cart not found",
                            content = @Content
                    )
            }
    )
    @DeleteMapping(ApiEndpoints.Cart.CLEAR_CART)
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.clearCart(userDetails));
    }
}
