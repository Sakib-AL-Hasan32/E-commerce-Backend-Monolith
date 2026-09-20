package com.db_migration.order.service.impl;

import com.db_migration.auth.entity.User;
import com.db_migration.auth.repository.UserRepository;
import com.db_migration.cart.entity.Cart;
import com.db_migration.cart.entity.CartItem;
import com.db_migration.cart.repository.CartItemRepository;
import com.db_migration.cart.repository.CartRepository;
import com.db_migration.common.constants.ApiMessages;
import com.db_migration.common.constants.PermissionNames;
import com.db_migration.common.exception.ResourceNotFound;
import com.db_migration.common.response.ApiResponse;
import com.db_migration.inventory.entity.Inventory;
import com.db_migration.inventory.repository.InventoryRepository;
import com.db_migration.order.dto.request.OrderCreateRequest;
import com.db_migration.order.dto.response.OrderItemResponse;
import com.db_migration.order.dto.response.OrderResponse;
import com.db_migration.order.entity.Order;
import com.db_migration.order.entity.OrderItem;
import com.db_migration.order.entity.OrderStatus;
import com.db_migration.order.repository.OrderItemRepository;
import com.db_migration.order.repository.OrderRepository;
import com.db_migration.order.service.OrderService;
import com.db_migration.product.entity.Product;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('" + PermissionNames.PLACE_ORDER + "')")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "product",  allEntries = true),
                    @CacheEvict(cacheNames = "productList", allEntries = true)
            }
    )
    public ApiResponse<OrderResponse> placeOrder(UserDetails userDetails, OrderCreateRequest request) {

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException(ApiMessages.Error.USER_NOT_FOUND));

        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.CART_NOT_FOUND));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        // Validation Check
        if(cartItems.isEmpty()) {
            throw new IllegalArgumentException(ApiMessages.Error.CART_EMPTY);
        }

        for(CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if(!product.isActive()) {
                throw new IllegalArgumentException(ApiMessages.Error.PRODUCT_NOT_ACTIVATED);
            }
            Inventory inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.INVENTORY_NOT_FOUND));

            if(inventory.getAvailableQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(ApiMessages.Error.INSUFFICIENT_QUANTITY);
            }
        }

        // Total Amount Calculated
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            totalAmount = totalAmount.add(
                    cartItem.getProduct()
                            .getPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }

        // Order Object Created
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingAddress(request.shippingAddress())
                .paymentMethod(request.paymentMethod())
                .build();
        orderRepository.save(order);

        // OrderItem Object Created
        // OrderItemResponse Created
        // Inventory Updated
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            orderItemRepository.save(orderItem);

            Inventory inventory = inventoryRepository.findByProductId(cartItem.getProduct().getId()).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.INVENTORY_NOT_FOUND));

            inventory.setReservedQuantity(inventory.getReservedQuantity() + cartItem.getQuantity());

            inventoryRepository.save(inventory);
        }

        // Clear CartItems [Not the cart]
        cartItemRepository.deleteAll(cartItems);

        OrderResponse response = mapToResponse(order);

        return ApiResponse.<OrderResponse>builder()
                .data(response)
                .message(ApiMessages.Success.ORDER_CONFIRMED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.CANCEL_ORDER + "')")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "product",  allEntries = true),
                    @CacheEvict(cacheNames = "productList", allEntries = true)
            }
    )
    public ApiResponse<Void> cancelOrder(UserDetails userDetails, Long orderId) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(ApiMessages.Error.USER_NOT_FOUND));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(ApiMessages.Error.ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(ApiMessages.Error.ORDER_CANNOT_BE_CANCELLED);
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

        for (OrderItem orderItem : orderItems) {
            Inventory inventory = inventoryRepository.findByProductId(orderItem.getProduct().getId()).orElseThrow(() -> new ResourceNotFound(ApiMessages.Error.INVENTORY_NOT_FOUND));

            inventory.setReservedQuantity(inventory.getReservedQuantity() - orderItem.getQuantity());
            inventoryRepository.save(inventory);
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return ApiResponse.<Void>builder()
                .message(ApiMessages.Success.ORDER_CANCELLED)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionNames.VIEW_ORDER + "')")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "product",  allEntries = true),
                    @CacheEvict(cacheNames = "productList", allEntries = true)
            }
    )
    public ApiResponse<List<OrderResponse>> getAll(UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException(ApiMessages.Error.USER_NOT_FOUND));

        List<Order> orders = orderRepository.findByUser(user);


        List<OrderResponse> orderResponseList = new ArrayList<>();
        for(Order order : orders) {
            OrderResponse orderResponse = mapToResponse(order);
            orderResponseList.add(orderResponse);
        }

        return ApiResponse.<List<OrderResponse>>builder()
                .data(orderResponseList)
                .message(ApiMessages.Success.ORDER_FETCHED)
                .build();
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        List<OrderItemResponse> orderItemResponseList = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            BigDecimal subTotal = orderItem.getProduct()
                    .getPrice()
                    .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

            OrderItemResponse orderItemResponse = new OrderItemResponse(
                    orderItem.getId(),
                    orderItem.getProduct().getId(),
                    orderItem.getProduct().getName(),
                    orderItem.getProduct().getPrice(),
                    orderItem.getQuantity(),
                    subTotal
            );
            orderItemResponseList.add(orderItemResponse);
        }

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getPaymentMethod(),
                orderItemResponseList,
                order.getCreatedAt()
        );
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}
